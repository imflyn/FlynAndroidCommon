package com.flyn.net.wifi.direct;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.Handler;

import com.flyn.telephone.TelephoneMgr;
import com.flyn.util.Logger;
import com.flyn.util.MathUtil;
import com.flyn.util.StringUtil;

public abstract class RemoteCallback implements Runnable
{
    Context                appContext  = null;
    private Selector       selector    = null;
    private List<Runnable> runnables   = new LinkedList();
    private long           lastRunTime = 0L;
    private Handler        handler     = new Handler();

    public RemoteCallback(Context context)
    {
        this.appContext = context.getApplicationContext();
    }

    void bindSelector(Selector selector)
    {
        if (selector == null)
            throw new NullPointerException();
        if (this.selector != null)
            throw new UnsupportedOperationException("bindSelector(Selector) can be called only once.");
        this.selector = selector;
    }

    void post(Runnable runnable)
    {
        synchronized (this.runnables)
        {
            this.runnables.add(runnable);
        }
    }

    public final void run()
    {
        while (true)
        {
            if (this.selector == null)
                return;
            long curTime = System.currentTimeMillis();
            if (curTime - this.lastRunTime >= 300L)
            {
                this.lastRunTime = curTime;
                List<Runnable> curRunnables = new LinkedList<Runnable>();
                Iterator<Runnable> iterator;
                synchronized (this.runnables)
                {
                    iterator = this.runnables.iterator();
                    while (iterator.hasNext())
                    {
                        curRunnables.add((Runnable) iterator.next());
                        iterator.remove();
                    }
                }
                for (Runnable runnable : curRunnables)
                {
                    runnable.run();
                }
            }
            int readyCount = 0;
            try
            {
                readyCount = this.selector.select(100L);
            } catch (IOException e)
            {
                Logger.logW(RemoteCallback.class, "running has been stopped.", e);
                return;
            } catch (ClosedSelectorException e)
            {
                Logger.logW(RemoteCallback.class, "running has been stopped.", e);
                return;
            }
            if (readyCount <= 0)
                continue;
            Set keys = null;
            try
            {
                keys = this.selector.selectedKeys();
            } catch (ClosedSelectorException e)
            {
                Logger.logW(RemoteCallback.class, "running has been stopped.", e);
                return;
            }
            Iterator iter = keys.iterator();
            while (iter.hasNext())
            {
                try
                {
                    SelectionKey key = (SelectionKey) iter.next();
                    iter.remove();
                    if (key.isAcceptable())
                    {
                        Object[] objs = (Object[]) key.attachment();
                        SocketChannel sc = null;
                        try
                        {
                            sc = ((ServerSocketChannel) key.channel()).accept();
                            sc.configureBlocking(false);
                            sc.register(this.selector, 1, new Object[] { 0, "length", ByteBuffer.allocate(4), objs[0] });
                        } catch (Exception e)
                        {
                            try
                            {
                                if (sc != null)
                                    sc.close();
                            } catch (IOException e1)
                            {
                                Logger.logE(RemoteCallback.class, "close socket channel failed.", e1);
                            }
                            Logger.logE(RemoteCallback.class, "handle accept socket channel failed.", e);
                        }
                    } else if (key.isConnectable())
                    {
                        Object[] objs = (Object[]) key.attachment();
                        if (objs[1].equals("connect"))
                        {
                            final RemoteUser user = (RemoteUser) objs[0];
                            SocketChannel sc = null;
                            try
                            {
                                sc = (SocketChannel) key.channel();
                                if (sc.isConnectionPending())
                                    sc.finishConnect();
                            } catch (final IOException e)
                            {
                                try
                                {
                                    key.cancel();
                                    if (sc != null)
                                        sc.close();
                                } catch (IOException e1)
                                {
                                    Logger.logE(RemoteCallback.class, "close socket channel failed.", e1);
                                }
                                user.state = 1;
                                this.handler.post(new Runnable()
                                {
                                    public void run()
                                    {
                                        RemoteCallback.this.onConnectedFailed(user, e);
                                    }
                                });
                                continue;
                            }
                            key.attach(new Object[] { user, "info_send", objs[2] });
                            key.interestOps(4);
                        } else
                        {
                            if (!objs[1].equals("transfer_connect"))
                                continue;
                            RemoteUser user = (RemoteUser) objs[0];
                            final TransferEntity transfer = (TransferEntity) objs[2];
                            SocketChannel sc = null;
                            try
                            {
                                sc = (SocketChannel) key.channel();
                                if (sc.isConnectionPending())
                                    sc.finishConnect();
                            } catch (final IOException e)
                            {
                                try
                                {
                                    user.getTransfers().remove(transfer);
                                    key.cancel();
                                    if (sc != null)
                                        sc.close();
                                } catch (IOException e1)
                                {
                                    Logger.logE(RemoteCallback.class, "close socket channel failed.", e1);
                                }
                                transfer.state = 1;
                                this.handler.post(new Runnable()
                                {
                                    public void run()
                                    {
                                        RemoteCallback.this.onTransferFailed(transfer, e);
                                    }
                                });
                                continue;
                            }
                            key.attach(new Object[] { user, "transfer_send", transfer });
                            key.interestOps(4);
                        }
                    } else if (key.isReadable())
                    {
                        SelectableChannel keyChannel = key.channel();
                        if ((keyChannel instanceof DatagramChannel))
                        {
                            DatagramChannel dc = (DatagramChannel) keyChannel;
                            Object[] objs = (Object[]) key.attachment();
                            ByteBuffer buff = null;
                            SocketAddress addr = null;
                            try
                            {
                                buff = ByteBuffer.allocate(18);
                                addr = dc.receive(buff);
                            } catch (IOException e)
                            {
                                Logger.logE(RemoteCallback.class, "receiving remote failed.", e);
                                continue;
                            }
                            if (dc.socket().getLocalPort() != 7002)
                                continue;
                            String ip = ((InetSocketAddress) addr).getAddress().getHostAddress();
                            List localIps = getLocalIpAddress();
                            if (localIps.contains(ip))
                                continue;
                            buff.flip();
                            String name = null;
                            try
                            {
                                name = Charset.forName("UTF-8").newDecoder().decode(buff).toString();
                            } catch (CharacterCodingException e)
                            {
                                Logger.logE(RemoteCallback.class, "decode remote name failed.", e);
                                continue;
                            }
                            RemoteUser curUser = new RemoteUser(name);
                            curUser.setIp(ip);
                            curUser.setRefreshTime(System.currentTimeMillis());
                            curUser.state = 1;
                            List users = ((User) objs[0]).scanUsers;
                            synchronized (users)
                            {
                                users.remove(curUser);
                                users.add(curUser);
                            }
                        }

                        SocketChannel sc = (SocketChannel) keyChannel;
                        Object[] objs = (Object[]) key.attachment();
                        if ((objs[2] instanceof ByteBuffer))
                        {
                            final RemoteUser remoteUser = (RemoteUser) objs[0];
                            ByteBuffer bb = (ByteBuffer) objs[2];
                            User user = (User) objs[3];
                            int len = 0;
                            try
                            {
                                len = sc.read(bb);
                            } catch (IOException e)
                            {
                                Logger.logE(RemoteCallback.class, "reading remote failed.", e);
                            }
                            if (len == -1)
                            {
                                try
                                {
                                    key.cancel();
                                    sc.close();
                                } catch (IOException e)
                                {
                                    Logger.logE(RemoteCallback.class, "close remote user failed when remote is close.", e);
                                }
                                if (remoteUser == null)
                                    continue;
                                remoteUser.state = 1;
                                user.connUsers.remove(remoteUser);
                                this.handler.post(new Runnable()
                                {
                                    public void run()
                                    {
                                        RemoteCallback.this.onDisconnected(remoteUser);
                                    }
                                });
                            } else
                            {
                                if (bb.hasRemaining())
                                    continue;
                                bb.flip();
                                if (objs[1].equals("length"))
                                {
                                    key.attach(new Object[] { remoteUser, "content", ByteBuffer.allocate(bb.getInt()), user });
                                } else
                                {
                                    if (!objs[1].equals("content"))
                                        continue;
                                    String content = null;
                                    try
                                    {
                                        content = Charset.forName("UTF-8").newDecoder().decode(bb).toString();
                                    } catch (CharacterCodingException e)
                                    {
                                        Logger.logE(RemoteCallback.class, "decode remote content failed.", e);
                                        key.attach(new Object[] { remoteUser, "length", ByteBuffer.allocate(4), user });
                                        continue;
                                    }
                                    String[] contentArr = StringUtil.parseFromCSV(content);
                                    if (contentArr[0].equals("info_send"))
                                    {
                                        final RemoteUser remote = new RemoteUser(contentArr[1]);
                                        remote.setIp(((InetSocketAddress) sc.socket().getRemoteSocketAddress()).getAddress().getHostAddress());
                                        remote.setKey(key);
                                        remote.state = 2;
                                        int index = user.connUsers.indexOf(remote);
                                        if (index != -1)
                                        {
                                            RemoteUser old = (RemoteUser) user.connUsers.get(index);
                                            user.disconnectUser(old);
                                        }
                                        user.connUsers.add(remote);
                                        this.handler.post(new Runnable()
                                        {
                                            public void run()
                                            {
                                                RemoteCallback.this.onConnected(remote);
                                            }
                                        });
                                        key.attach(new Object[] { remote, "length", ByteBuffer.allocate(4), user });
                                    } else if (contentArr[0].equals("transfer_request"))
                                    {
                                        if (remoteUser != null)
                                        {
                                            final String description = contentArr[1];
                                            this.handler.post(new Runnable()
                                            {
                                                public void run()
                                                {
                                                    RemoteCallback.this.onTransferRequest(remoteUser, description);
                                                }
                                            });
                                        }
                                        key.attach(new Object[] { remoteUser, "length", ByteBuffer.allocate(4), user });
                                    } else if (contentArr[0].equals("transfer_reply"))
                                    {
                                        if (remoteUser != null)
                                        {
                                            final boolean allow = Boolean.parseBoolean(contentArr[1]);
                                            final String description = contentArr[2];
                                            this.handler.post(new Runnable()
                                            {
                                                public void run()
                                                {
                                                    RemoteCallback.this.onTransferReply(remoteUser, allow, description);
                                                }
                                            });
                                        }
                                        key.attach(new Object[] { remoteUser, "length", ByteBuffer.allocate(4), user });
                                    } else
                                    {
                                        if (!contentArr[0].equals("transfer_send"))
                                            continue;
                                        String addr = ((InetSocketAddress) sc.socket().getRemoteSocketAddress()).getAddress().getHostAddress();
                                        RemoteUser queryUser = new RemoteUser("test");
                                        queryUser.setIp(addr);
                                        queryUser.state = 1;
                                        int index = user.connUsers.indexOf(queryUser);
                                        if (index == -1)
                                        {
                                            try
                                            {
                                                key.cancel();
                                                sc.close();
                                            } catch (IOException e)
                                            {
                                                Logger.logE(RemoteCallback.class, "close socket channel failed.", e);
                                            }
                                        } else
                                        {
                                            queryUser = (RemoteUser) user.connUsers.get(index);
                                            final TransferEntity transfer = new TransferEntity();
                                            transfer.setRemoteUser(queryUser);
                                            transfer.setSendPath(contentArr[1]);
                                            transfer.setSize(Long.parseLong(contentArr[2]));
                                            transfer.setSender(false);
                                            if (contentArr.length == 3)
                                                transfer.setExtraDescription(null);
                                            else
                                                transfer.setExtraDescription(contentArr[3]);
                                            transfer.setSavingPath(onGetSavingPathInBackground(queryUser, transfer.getSendPath(), transfer.getSize(), transfer.getExtraDescription()));
                                            transfer.setSelectionKey(key);
                                            transfer.state = 0;
                                            queryUser.getTransfers().add(transfer);
                                            this.handler.post(new Runnable()
                                            {
                                                public void run()
                                                {
                                                    RemoteCallback.this.onTransferProgress(transfer, 0);
                                                }
                                            });
                                            try
                                            {
                                                File file = new File(transfer.getSavingPath());
                                                File parentPath = file.getParentFile();
                                                if (parentPath != null)
                                                {
                                                    if ((!parentPath.exists()) && (!parentPath.mkdirs()))
                                                        throw new IOException("can not create saving path.");
                                                    if (TelephoneMgr.getFileStorageAvailableSize(parentPath) < transfer.getSize())
                                                        throw new SpaceNotEnoughException();
                                                }
                                                key.attach(new Object[] { queryUser, "transfer_progress", transfer, ByteBuffer.allocate(2048), new FileOutputStream(file).getChannel(),
                                                        Integer.valueOf(0), Integer.valueOf(0) });
                                            } catch (final IOException e)
                                            {
                                                try
                                                {
                                                    queryUser.getTransfers().remove(transfer);
                                                    key.cancel();
                                                    sc.close();
                                                } catch (IOException e1)
                                                {
                                                    Logger.logE(RemoteCallback.class, "close socket channel failed.", e1);
                                                }
                                                transfer.state = 1;
                                                this.handler.post(new Runnable()
                                                {
                                                    public void run()
                                                    {
                                                        RemoteCallback.this.onTransferFailed(transfer, e);
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            }
                        } else
                        {
                            if (!(objs[2] instanceof TransferEntity))
                                continue;
                            if (!objs[1].equals("transfer_progress"))
                                continue;
                            final TransferEntity transfer = (TransferEntity) objs[2];
                            ByteBuffer cache = (ByteBuffer) objs[3];
                            FileChannel channel = (FileChannel) objs[4];
                            try
                            {
                                int len = sc.read(cache);
                                long curSize = Long.parseLong(String.valueOf(objs[5]));
                                curSize += cache.position();
                                if (curSize >= transfer.getSize())
                                {
                                    cache.flip();
                                    channel.write(cache);
                                    try
                                    {
                                        channel.close();
                                    } catch (IOException e)
                                    {
                                        Logger.logE(RemoteCallback.class, " close file channel failed.", e);
                                    }
                                    try
                                    {
                                        transfer.getRemoteUser().getTransfers().remove(transfer);
                                        key.cancel();
                                        sc.close();
                                    } catch (IOException e)
                                    {
                                        Logger.logE(RemoteCallback.class, "close socket channel failed.", e);
                                    }
                                    transfer.state = 1;
                                    this.handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            RemoteCallback.this.onTransferProgress(transfer, 100);
                                        }
                                    });
                                    continue;
                                }
                                if (len == -1)
                                {
                                    try
                                    {
                                        channel.close();
                                    } catch (IOException e)
                                    {
                                        Logger.logE(RemoteCallback.class, "close file channel failed.", e);
                                    }
                                    try
                                    {
                                        transfer.getRemoteUser().getTransfers().remove(transfer);
                                        key.cancel();
                                        sc.close();
                                    } catch (IOException e)
                                    {
                                        Logger.logE(RemoteCallback.class, "close socket channel failed.", e);
                                    }
                                    transfer.state = 1;
                                    this.handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            RemoteCallback.this.onTransferFailed(transfer, new RuntimeException("remote is closed."));
                                        }
                                    });
                                    continue;
                                }
                                if (cache.hasRemaining())
                                    continue;
                                cache.flip();
                                channel.write(cache);
                                int lastPublishProgress = Integer.parseInt(String.valueOf(objs[6]));
                                final int curProgress = (int) MathUtil.mul(MathUtil.div(curSize, transfer.getSize(), 2), 100.0D);
                                if (curProgress < 100)
                                {
                                    if (curProgress - lastPublishProgress >= 5)
                                    {
                                        lastPublishProgress = curProgress;
                                        this.handler.post(new Runnable()
                                        {
                                            public void run()
                                            {
                                                RemoteCallback.this.onTransferProgress(transfer, curProgress);
                                            }
                                        });
                                    }
                                }
                                key.attach(new Object[] { objs[0], "transfer_progress", transfer, ByteBuffer.allocate(2048), channel, Long.valueOf(curSize), Integer.valueOf(lastPublishProgress) });
                            } catch (final IOException e)
                            {
                                try
                                {
                                    channel.close();
                                } catch (IOException e1)
                                {
                                    Logger.logE(RemoteCallback.class, " close file channel failed.", e1);
                                }
                                try
                                {
                                    transfer.getRemoteUser().getTransfers().remove(transfer);
                                    key.cancel();
                                    sc.close();
                                } catch (IOException e1)
                                {
                                    Logger.logE(RemoteCallback.class, "close socket channel failed.", e1);
                                }
                                transfer.state = 1;
                                this.handler.post(new Runnable()
                                {
                                    public void run()
                                    {
                                        RemoteCallback.this.onTransferFailed(transfer, e);
                                    }
                                });
                            }
                        }
                    } else
                    {
                        if (!key.isWritable())
                            continue;
                        SelectableChannel keyChannel = key.channel();
                        if ((keyChannel instanceof DatagramChannel))
                        {
                            DatagramChannel dc = (DatagramChannel) keyChannel;
                            Object[] objs = (Object[]) key.attachment();
                            User user = (User) objs[0];
                            try
                            {
                                ByteBuffer buff = ByteBuffer.allocate(18);
                                buff.put(user.getName().getBytes("UTF-8"));
                                buff.flip();
                                dc.send(buff, new InetSocketAddress(InetAddress.getByName("255.255.255.255"), 7002));
                            } catch (IOException e)
                            {
                                Logger.logE(RemoteCallback.class, "send udp message for scanning user failed.", e);
                            } finally
                            {
                                key.interestOps(1);
                            }
                        } else
                        {
                            SocketChannel sc = (SocketChannel) keyChannel;
                            Object[] objs = (Object[]) key.attachment();
                            if (objs[1].equals("info_send"))
                            {
                                final RemoteUser remoteUser = (RemoteUser) objs[0];
                                try
                                {
                                    ByteBuffer sendBuff = null;
                                    User user = null;
                                    if ((objs[2] instanceof ByteBuffer))
                                    {
                                        sendBuff = (ByteBuffer) objs[2];
                                        user = (User) objs[3];
                                    } else
                                    {
                                        user = (User) objs[2];
                                        String msg = StringUtil.concatByCSV(new String[] { "info_send", user.getName() });
                                        byte[] msgByte = msg.getBytes("UTF-8");
                                        sendBuff = ByteBuffer.allocate(4 + msgByte.length);
                                        sendBuff.putInt(msgByte.length);
                                        sendBuff.put(msgByte);
                                        sendBuff.flip();
                                    }
                                    sc.write(sendBuff);
                                    if (sendBuff.hasRemaining())
                                    {
                                        key.attach(new Object[] { objs[0], "info_send", sendBuff, user });
                                        continue;
                                    }
                                    key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4), user });
                                    key.interestOps(1);
                                    remoteUser.setKey(key);
                                    remoteUser.state = 2;
                                    user.connUsers.add(remoteUser);
                                    this.handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            RemoteCallback.this.onConnected(remoteUser);
                                        }
                                    });
                                } catch (final IOException e)
                                {
                                    try
                                    {
                                        key.cancel();
                                        sc.close();
                                    } catch (IOException e1)
                                    {
                                        Logger.logE(RemoteCallback.class, "close socket channel failed.", e1);
                                    }
                                    remoteUser.state = 1;
                                    this.handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            RemoteCallback.this.onConnectedFailed(remoteUser, e);
                                        }
                                    });
                                }
                            } else if (objs[1].equals("transfer_request"))
                            {
                                try
                                {
                                    ByteBuffer sendBuff = null;
                                    if ((objs[2] instanceof ByteBuffer))
                                    {
                                        sendBuff = (ByteBuffer) objs[2];
                                    } else
                                    {
                                        String description = String.valueOf(objs[2]);
                                        String msg = StringUtil.concatByCSV(new String[] { "transfer_request", description });
                                        byte[] msgByte = msg.getBytes("UTF-8");
                                        sendBuff = ByteBuffer.allocate(4 + msgByte.length);
                                        sendBuff.putInt(msgByte.length);
                                        sendBuff.put(msgByte);
                                        sendBuff.flip();
                                    }
                                    sc.write(sendBuff);
                                    if (sendBuff.hasRemaining())
                                    {
                                        key.attach(new Object[] { objs[0], "transfer_request", sendBuff });
                                        continue;
                                    }
                                    key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4), objs[3] });
                                    key.interestOps(1);
                                } catch (IOException e)
                                {
                                    Logger.logE(RemoteCallback.class, "transfer request failed.", e);
                                    key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4), objs[3] });
                                    key.interestOps(1);
                                }
                            } else if (objs[1].equals("transfer_reply"))
                            {
                                try
                                {
                                    ByteBuffer sendBuff = null;
                                    if ((objs[2] instanceof ByteBuffer))
                                    {
                                        sendBuff = (ByteBuffer) objs[2];
                                    } else
                                    {
                                        String msg = StringUtil.concatByCSV(new String[] { "transfer_reply", String.valueOf(objs[2]), String.valueOf(objs[3]) });
                                        byte[] msgByte = msg.getBytes("UTF-8");
                                        sendBuff = ByteBuffer.allocate(4 + msgByte.length);
                                        sendBuff.putInt(msgByte.length);
                                        sendBuff.put(msgByte);
                                        sendBuff.flip();
                                    }
                                    sc.write(sendBuff);
                                    if (sendBuff.hasRemaining())
                                    {
                                        key.attach(new Object[] { objs[0], "transfer_reply", sendBuff });
                                        continue;
                                    }
                                    key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4), objs[4] });
                                    key.interestOps(1);
                                } catch (IOException e)
                                {
                                    Logger.logE(RemoteCallback.class, "transfer reply failed.", e);
                                    key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4), objs[4] });
                                    key.interestOps(1);
                                }
                            } else if (objs[1].equals("transfer_send"))
                            {
                                TransferEntity transfer = null;
                                try
                                {
                                    ByteBuffer sendBuff = null;
                                    if ((objs[2] instanceof ByteBuffer))
                                    {
                                        sendBuff = (ByteBuffer) objs[2];
                                        transfer = (TransferEntity) objs[3];
                                    } else
                                    {
                                        transfer = (TransferEntity) objs[2];
                                        String msg = null;
                                        String extraDescription = transfer.getExtraDescription();
                                        if (extraDescription == null)
                                            msg = StringUtil.concatByCSV(new String[] { "transfer_send", transfer.getSendPath(), String.valueOf(transfer.getSize()) });
                                        else
                                            msg = StringUtil.concatByCSV(new String[] { "transfer_send", transfer.getSendPath(), String.valueOf(transfer.getSize()), extraDescription });
                                        byte[] msgByte = msg.getBytes("UTF-8");
                                        sendBuff = ByteBuffer.allocate(4 + msgByte.length);
                                        sendBuff.putInt(msgByte.length);
                                        sendBuff.put(msgByte);
                                        sendBuff.flip();
                                    }
                                    sc.write(sendBuff);
                                    if (sendBuff.hasRemaining())
                                    {
                                        key.attach(new Object[] { objs[0], "transfer_send", sendBuff, transfer });
                                        continue;
                                    }
                                    key.attach(new Object[] { objs[0], "transfer_progress", transfer, new FileInputStream(transfer.getSendPath()).getChannel(), Integer.valueOf(0), Integer.valueOf(0) });
                                } catch (final IOException e)
                                {
                                    try
                                    {
                                        transfer.getRemoteUser().getTransfers().remove(transfer);
                                        key.cancel();
                                        sc.close();
                                    } catch (IOException e1)
                                    {
                                        Logger.logE(RemoteCallback.class, "close socket channel failed.", e1);
                                    }
                                    final TransferEntity transferPoint = transfer;
                                    transferPoint.state = 1;
                                    this.handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            RemoteCallback.this.onTransferFailed(transferPoint, e);
                                        }
                                    });
                                }
                            } else
                            {
                                if (!objs[1].equals("transfer_progress"))
                                    continue;
                                TransferEntity transfer = null;
                                FileChannel channel = null;
                                try
                                {
                                    ByteBuffer sendBuff = null;
                                    int lastPublishProgress;
                                    long curSize;
                                    if ((objs[2] instanceof ByteBuffer))
                                    {
                                        sendBuff = (ByteBuffer) objs[2];
                                        transfer = (TransferEntity) objs[3];
                                        channel = (FileChannel) objs[4];
                                        curSize = Long.parseLong(String.valueOf(objs[5]));
                                        lastPublishProgress = Integer.parseInt(String.valueOf(objs[6]));
                                    } else
                                    {
                                        transfer = (TransferEntity) objs[2];
                                        channel = (FileChannel) objs[3];
                                        curSize = Long.parseLong(String.valueOf(objs[4]));
                                        lastPublishProgress = Integer.parseInt(String.valueOf(objs[5]));
                                        sendBuff = ByteBuffer.allocate(2048);
                                        int len = channel.read(sendBuff);
                                        if (len == -1)
                                        {
                                            try
                                            {
                                                transfer.getRemoteUser().getTransfers().remove(transfer);
                                                key.cancel();
                                                sc.close();
                                            } catch (IOException e)
                                            {
                                                Logger.logE(RemoteCallback.class, "close socket channel failed.", e);
                                            }
                                            try
                                            {
                                                channel.close();
                                            } catch (IOException e)
                                            {
                                                Logger.logE(RemoteCallback.class, "close file channel failed.", e);
                                            }
                                            final TransferEntity transferPoint = transfer;
                                            transferPoint.state = 1;
                                            this.handler.post(new Runnable()
                                            {
                                                public void run()
                                                {
                                                    RemoteCallback.this.onTransferProgress(transferPoint, 100);
                                                }
                                            });
                                            continue;
                                        }
                                        sendBuff.flip();
                                    }
                                    sc.write(sendBuff);
                                    if (sendBuff.hasRemaining())
                                    {
                                        key.attach(new Object[] { objs[0], "transfer_progress", sendBuff, transfer, channel, Long.valueOf(curSize), Integer.valueOf(lastPublishProgress) });
                                    } else
                                    {
                                        curSize += sendBuff.position();
                                        final int curProgress = (int) MathUtil.mul(MathUtil.div(curSize, transfer.getSize(), 2), 100.0D);
                                        if (curProgress < 100)
                                        {
                                            if (curProgress - lastPublishProgress >= 5)
                                            {
                                                lastPublishProgress = curProgress;
                                                final TransferEntity transferPoint = transfer;
                                                this.handler.post(new Runnable()
                                                {
                                                    public void run()
                                                    {
                                                        RemoteCallback.this.onTransferProgress(transferPoint, curProgress);
                                                    }
                                                });
                                            }
                                        }
                                        key.attach(new Object[] { objs[0], "transfer_progress", transfer, channel, Long.valueOf(curSize), Integer.valueOf(lastPublishProgress) });
                                    }
                                } catch (final IOException e)
                                {
                                    try
                                    {
                                        transfer.getRemoteUser().getTransfers().remove(transfer);
                                        key.cancel();
                                        sc.close();
                                    } catch (IOException e1)
                                    {
                                        Logger.logE(RemoteCallback.class, "close socket channel failed.", e1);
                                    }
                                    try
                                    {
                                        channel.close();
                                    } catch (IOException e1)
                                    {
                                        Logger.logE(RemoteCallback.class, "close file channel failed.", e1);
                                    }
                                    final TransferEntity transferPoint = transfer;
                                    transferPoint.state = 1;
                                    this.handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            RemoteCallback.this.onTransferFailed(transferPoint, e);
                                        }
                                    });
                                }
                            }
                        }
                    }
                } catch (RuntimeException e)
                {
                    Logger.logE(RemoteCallback.class, "deal cur selection key failed,try next one...", e);
                }
            }
        }
    }

    private List<String> getLocalIpAddress()
    {
        List<String> returnVal = new LinkedList<String>();
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (inetAddress.isSiteLocalAddress())
                        returnVal.add(inetAddress.getHostAddress());
                }
            }
        } catch (SocketException ex)
        {
            Logger.logE(RemoteCallback.class, "get local ip failed.", ex);
        }
        return returnVal;
    }

    public abstract void onConnected(RemoteUser paramRemoteUser);

    public abstract void onConnectedFailed(RemoteUser paramRemoteUser, Exception paramException);

    public abstract void onDisconnected(RemoteUser paramRemoteUser);

    public abstract void onDisconnectedFailed(RemoteUser paramRemoteUser, Exception paramException);

    public abstract void onTransferRequest(RemoteUser paramRemoteUser, String paramString);

    public abstract void onTransferReply(RemoteUser paramRemoteUser, boolean paramBoolean, String paramString);

    public abstract String onGetSavingPathInBackground(RemoteUser paramRemoteUser, String paramString1, long paramLong, String paramString2);

    public abstract void onTransferProgress(TransferEntity paramTransferEntity, int paramInt);

    public abstract void onTransferFailed(TransferEntity paramTransferEntity, Exception paramException);
}
