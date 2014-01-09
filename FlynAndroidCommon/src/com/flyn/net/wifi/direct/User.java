package com.flyn.net.wifi.direct;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;

import com.flyn.net.wifi.WifiCallback;
import com.flyn.net.wifi.WifiUtils;
import com.flyn.util.Logger;
import com.flyn.util.StringUtil;

public class User
{
    private static final int      WIFI_TIMEOUT             = 20000;
    private static final int      CONNECT_TIMEOUT          = 20000;
    static final int              MAX_NAME_LENGTH          = 6;
    private static final int      LISTENING_PORT           = 7001;
    static final int              LISTENING_PORT_UDP       = 7002;
    private static final int      LISTENING_PORT_UDP_OWNER = 7003;
    private String                name                     = null;
    private RemoteCallback        callback                 = null;
    private PowerManager.WakeLock wakeLock                 = null;
    private Selector              selector                 = null;

    private WifiConfiguration     preApConfig              = null;
    private int                   preWifiStaticIp          = -1;
    private boolean               preWifiEnabled           = false;

    LinkedList<RemoteUser>        connUsers                = new LinkedList();
    LinkedList<RemoteUser>        scanUsers                = new LinkedList();

    private Handler               handler                  = new Handler();

    public User(String name, RemoteCallback callback) throws NameOutOfRangeException, IOException
    {
        if ((name == null) || (callback == null))
            throw new NullPointerException();
        if (name.length() > 6)
            throw new NameOutOfRangeException("name length can not great than 6.");
        this.name = name;
        this.callback = callback;
        PowerManager powerManager = (PowerManager) callback.appContext.getSystemService("power");
        this.wakeLock = powerManager.newWakeLock(536870913, getClass().getName());
        this.wakeLock.acquire();
        try
        {
            this.selector = Selector.open();
        } catch (IOException e)
        {
            if (this.wakeLock.isHeld())
                this.wakeLock.release();
            throw e;
        }
        try
        {
            callback.bindSelector(this.selector);
        } catch (RuntimeException e)
        {
            if (this.wakeLock.isHeld())
                this.wakeLock.release();
            this.selector.close();
            throw e;
        }
        SelectionKey serverKey = null;
        ServerSocketChannel serverChannel = null;
        try
        {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(7001));
            serverKey = serverChannel.register(this.selector, 16, new Object[] { this });
        } catch (IOException e)
        {
            try
            {
                if (this.wakeLock.isHeld())
                    this.wakeLock.release();
                this.selector.close();
            } finally
            {
                if (serverChannel != null)
                    serverChannel.close();
            }
            throw e;
        }
        SelectionKey key = null;
        DatagramChannel channel = null;
        try
        {
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(7002));
            key = channel.register(this.selector, 1, new Object[] { this });
        } catch (IOException e)
        {
            try
            {
                if (this.wakeLock.isHeld())
                    this.wakeLock.release();
                this.selector.close();
            } finally
            {
                try
                {
                    serverKey.cancel();
                    serverChannel.close();
                } finally
                {
                    if (channel != null)
                        channel.close();
                }
            }
            throw e;
        }
        SelectionKey ownerKey = null;
        DatagramChannel ownerChannel = null;
        try
        {
            ownerChannel = DatagramChannel.open();
            ownerChannel.configureBlocking(false);
            ownerChannel.socket().bind(new InetSocketAddress(7003));
            ownerKey = ownerChannel.register(this.selector, 1, new Object[] { this });
        } catch (IOException e)
        {
            try
            {
                if (this.wakeLock.isHeld())
                    this.wakeLock.release();
                this.selector.close();
            } finally
            {
                try
                {
                    serverKey.cancel();
                    serverChannel.close();
                } finally
                {
                    try
                    {
                        key.cancel();
                        channel.close();
                    } finally
                    {
                        if (ownerChannel != null)
                            ownerChannel.close();
                    }
                }
            }
            throw e;
        }
        final SelectionKey ownerKeyPoint = ownerKey;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        ownerKeyPoint.interestOps(4);
                    } catch (CancelledKeyException localCancelledKeyException)
                    {
                        break;
                    }
                    long curTime = System.currentTimeMillis();
                    synchronized (User.this.scanUsers)
                    {
                        Iterator users = User.this.scanUsers.iterator();
                        while (users.hasNext())
                        {
                            RemoteUser curUser = (RemoteUser) users.next();
                            if (curTime - curUser.getRefreshTime() > 20000L)
                                users.remove();
                        }
                    }
                    try
                    {
                        Thread.sleep(4000L);
                    } catch (InterruptedException localInterruptedException)
                    {
                    }
                }
            }
        }).start();
        new Thread(callback).start();
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name) throws NameOutOfRangeException, StateNotAllowException
    {
        if (name == null)
            throw new NullPointerException();
        if (name.length() > 6)
            throw new NameOutOfRangeException("name length can not great than 6.");
        if (this.preApConfig != null)
            throw new StateNotAllowException("can not set name while listening.");
        this.name = name;
    }

    public void openDirectAp(Context context, final OpenDirectApCallback callback)
    {
        WifiUtils wifiUtils = new WifiUtils(context);
        final boolean isFirst = this.preApConfig == null;
        try
        {
            if (isFirst)
            {
                this.preApConfig = wifiUtils.getWifiApConfiguration();
                try
                {
                    this.preWifiStaticIp = Settings.System.getInt(context.getContentResolver(), "wifi_static_ip");
                } catch (Settings.SettingNotFoundException localSettingNotFoundException)
                {
                }
                this.preWifiEnabled = wifiUtils.isWifiEnabled();
            }
            wifiUtils.setWifiApEnabled(createDirectApConfig(context), true, new WifiCallback(context)
            {
                @Override
                public void onWifiApEnabled()
                {
                    super.onWifiApEnabled();
                    callback.onOpen();
                }

                @Override
                public void onTimeout()
                {
                    super.onTimeout();
                    if (isFirst)
                    {
                        User.this.preApConfig = null;
                        User.this.preWifiStaticIp = -1;
                        User.this.preWifiEnabled = false;
                    }
                    callback.onError(new RuntimeException("open ap failed by 'WifiCallback.onTimeout()'."));
                }

                @Override
                public void onWifiApFailed()
                {
                    super.onWifiApFailed();
                    if (isFirst)
                    {
                        User.this.preApConfig = null;
                        User.this.preWifiStaticIp = -1;
                        User.this.preWifiEnabled = false;
                    }
                    callback.onError(new RuntimeException("open ap failed by 'WifiCallback.onWifiApFailed()'."));
                }
            }, 20000);
        } catch (RuntimeException e)
        {
            if (isFirst)
            {
                this.preApConfig = null;
                this.preWifiStaticIp = -1;
                this.preWifiEnabled = false;
            }
            callback.onError(e);
        }
    }

    private WifiConfiguration createDirectApConfig(Context context) throws RuntimeException
    {
        WifiUtils wifiUtils = new WifiUtils(context);
        WifiConfiguration apconfig = new WifiConfiguration();
        String apName = null;
        try
        {
            apName = "MYFY" + StringUtil.bytesToHexString(this.name.getBytes("UTF-16"));
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        String apPassword = null;
        apconfig.SSID = apName;
        apconfig.BSSID = wifiUtils.getConnectionInfo().getMacAddress();
        apconfig.preSharedKey = apPassword;
        apconfig.allowedAuthAlgorithms.clear();
        apconfig.allowedAuthAlgorithms.set(0);
        apconfig.allowedProtocols.clear();
        apconfig.allowedProtocols.set(1);
        apconfig.allowedProtocols.set(0);
        apconfig.allowedKeyManagement.clear();
        if (apPassword == null)
            apconfig.allowedKeyManagement.set(0);
        else
            apconfig.allowedKeyManagement.set(1);
        apconfig.allowedPairwiseCiphers.clear();
        apconfig.allowedPairwiseCiphers.set(2);
        apconfig.allowedPairwiseCiphers.set(1);
        apconfig.allowedGroupCiphers.clear();
        apconfig.allowedGroupCiphers.set(3);
        apconfig.allowedGroupCiphers.set(2);
        apconfig.hiddenSSID = false;
        Field apProfileField = null;
        try
        {
            apProfileField = WifiConfiguration.class.getDeclaredField("mWifiApProfile");
        } catch (NoSuchFieldException localNoSuchFieldException1)
        {
        }
        if (apProfileField != null)
        {
            try
            {
                apProfileField.setAccessible(true);
                Object apProfile = apProfileField.get(apconfig);
                if (apProfile != null)
                {
                    Field ssidField = apProfile.getClass().getField("SSID");
                    ssidField.setAccessible(true);
                    ssidField.set(apProfile, apconfig.SSID);
                    Field bssidField = apProfile.getClass().getField("BSSID");
                    bssidField.setAccessible(true);
                    bssidField.set(apProfile, apconfig.BSSID);
                    Field typeField = apProfile.getClass().getField("secureType");
                    typeField.setAccessible(true);
                    typeField.set(apProfile, "open");
                    Field dhcpField = apProfile.getClass().getField("dhcpEnable");
                    dhcpField.setAccessible(true);
                    dhcpField.set(apProfile, Integer.valueOf(1));
                }
            } catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e)
            {
                throw new RuntimeException(e);
            }
        }
        return apconfig;
    }

    public void closeDirectAp(final Context context, final CloseDirectApCallback callback)
    {
        final WifiUtils wifiUtils = new WifiUtils(context);
        try
        {
            wifiUtils.setWifiApEnabled(null, false, new WifiCallback(context)
            {
                @Override
                public void onWifiApDisabled()
                {
                    super.onWifiApDisabled();
                    if (User.this.preApConfig != null)
                    {
                        try
                        {
                            wifiUtils.setWifiApConfiguration(User.this.preApConfig);
                        } catch (RuntimeException e)
                        {
                            Logger.logE(User.class, "restore ap config failed.", e);
                        } catch (NoSuchMethodException e)
                        {
                            Logger.logE(User.class, "restore ap config failed.", e);
                        } catch (IllegalAccessException e)
                        {
                            Logger.logE(User.class, "restore ap config failed.", e);
                        } catch (InvocationTargetException e)
                        {
                            Logger.logE(User.class, "restore ap config failed.", e);
                        }
                        if (User.this.preWifiStaticIp != -1)
                            Settings.System.putInt(context.getContentResolver(), "wifi_static_ip", User.this.preWifiStaticIp);
                        if (User.this.preWifiEnabled)
                            wifiUtils.setWifiEnabled(true, null, 20000);
                        User.this.preApConfig = null;
                        User.this.preWifiStaticIp = -1;
                        User.this.preWifiEnabled = false;
                    }
                    callback.onClosed();
                }

                @Override
                public void onTimeout()
                {
                    super.onTimeout();
                    callback.onError(new RuntimeException("close ap failed by 'WifiCallback.onTimeout()'."));
                }

                @Override
                public void onWifiApFailed()
                {
                    super.onWifiApFailed();
                    callback.onError(new RuntimeException("close ap failed by 'WifiCallback.onWifiApFailed()'."));
                }
            }, 20000);
        } catch (RuntimeException e)
        {
            callback.onError(e);
        }
    }

    public void scanDirectAps(final Context context, final ScanDirectApsCallback callback)
    {
        final WifiUtils wifiUtils = new WifiUtils(context);
        wifiUtils.setWifiEnabled(true, new WifiCallback(context)
        {
            @Override
            public void onWifiEnabled()
            {
                super.onWifiEnabled();
                wifiUtils.startScan(new WifiCallback(context)
                {
                    @Override
                    public void onScanResults(List<ScanResult> scanResults)
                    {
                        super.onScanResults(scanResults);
                        List callbackVal = new ArrayList();
                        for (ScanResult result : scanResults)
                        {
                            String ssid = result.SSID;
                            String name = null;
                            if (ssid.startsWith("MYFY"))
                            {
                                String userStr = ssid.substring(4);
                                try
                                {
                                    name = new String(StringUtil.hexStringToBytes(userStr), "UTF-16");
                                } catch (Exception e)
                                {
                                    Logger.logW(User.class, "decode scanned ap name failed.", e);
                                }
                            }
                            if (name == null)
                                continue;
                            DirectAp ap = new DirectAp(name);
                            ap.setScanResult(result);
                            callbackVal.add(ap);
                        }

                        callback.onScanned(callbackVal);
                    }

                    @Override
                    public void onTimeout()
                    {
                        super.onTimeout();
                        callback.onError();
                    }

                    @Override
                    public void onScanFailed()
                    {
                        super.onScanFailed();
                        callback.onError();
                    }
                }, 20000);
            }

            @Override
            public void onWifiFailed()
            {
                super.onWifiFailed();
                callback.onError();
            }

            @Override
            public void onTimeout()
            {
                super.onTimeout();
                callback.onError();
            }
        }, 20000);
    }

    public void connectToDirectAp(final Context context, final DirectAp ap, final ConnectToDirectApCallback callback)
    {
        final WifiUtils wifiUtils = new WifiUtils(context);
        wifiUtils.setWifiEnabled(true, new WifiCallback(context)
        {
            @Override
            public void onWifiEnabled()
            {
                super.onWifiEnabled();
                wifiUtils.connect(ap.getScanResult(), null, new WifiCallback(context)
                {
                    @Override
                    public void onNetworkConnected(WifiInfo wifiInfo)
                    {
                        super.onNetworkConnected(wifiInfo);
                        RemoteUser user = new RemoteUser(ap.getName());
                        int apIp = wifiUtils.getWifiManager().getDhcpInfo().serverAddress;
                        String apIpStr = String.format("%d.%d.%d.%d",
                                new Object[] { Integer.valueOf(apIp & 0xFF), Integer.valueOf(apIp >> 8 & 0xFF), Integer.valueOf(apIp >> 16 & 0xFF), Integer.valueOf(apIp >> 24 & 0xFF) });
                        user.setIp(apIpStr);
                        user.state = 1;
                        callback.onConnected(ap, user);
                    }

                    @Override
                    public void onNetworkDisconnected(WifiInfo wifiInfo)
                    {
                        super.onNetworkDisconnected(wifiInfo);
                        callback.onError(ap);
                    }

                    @Override
                    public void onTimeout()
                    {
                        super.onTimeout();
                        callback.onError(ap);
                    }

                    @Override
                    public void onNetworkFailed(WifiInfo wifiInfo)
                    {
                        super.onNetworkFailed(wifiInfo);
                        callback.onError(ap);
                    }
                }, 20000);
            }

            @Override
            public void onWifiFailed()
            {
                super.onWifiFailed();
                callback.onError(ap);
            }

            @Override
            public void onTimeout()
            {
                super.onTimeout();
                callback.onError(ap);
            }
        }, 20000);
    }

    public void disconnectDirectAp(Context context, DirectAp ap, DisconnectDirectApCallback callback)
    {
        try
        {
            ScanResult sr = ap.getScanResult();
            WifiUtils wifiUtils = new WifiUtils(context);
            List wcList = wifiUtils.getConfiguration(sr, true);
            if ((wcList != null) && (wcList.size() != 0))
            {
                WifiManager wm = wifiUtils.getWifiManager();
                wm.removeNetwork(((WifiConfiguration) wcList.get(0)).networkId);
                wm.saveConfiguration();
            }
        } catch (RuntimeException e)
        {
            callback.onError(ap, e);
            return;
        }
        callback.onDisconnected(ap);
    }

    public void scanUsers(final ScanUsersCallback scanCallback)
    {
        this.handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                LinkedList users = null;
                synchronized (User.this.scanUsers)
                {
                    users = (LinkedList) User.this.scanUsers.clone();
                }
                scanCallback.onScanned(users);
            }
        }, 6000L);
    }

    public void connectToUser(final RemoteUser user)
    {
        this.callback.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (user.state != 1)
                    return;
                user.state = 0;
                SocketChannel sc = null;
                try
                {
                    if (User.this.connUsers.contains(user))
                        throw new DuplicateAlreadyConnectedException();
                    sc = SocketChannel.open();
                    sc.configureBlocking(false);
                    sc.connect(new InetSocketAddress(user.getIp(), 7001));
                    final SelectionKey key = sc.register(User.this.selector, 8, new Object[] { user, "connect", User.this });
                    final SocketChannel scPoint = sc;
                    User.this.handler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            User.this.callback.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if (user.state == 0)
                                    {
                                        try
                                        {
                                            key.cancel();
                                            scPoint.close();
                                        } catch (IOException e)
                                        {
                                            Logger.logD(User.class, "close socket channel failed.", e);
                                        }
                                        user.state = 1;
                                        User.this.handler.post(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                User.this.callback.onConnectedFailed(user, new SocketTimeoutException("connect time out."));
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }, 20000L);
                } catch (final IOException e)
                {
                    try
                    {
                        if (sc != null)
                            sc.close();
                    } catch (IOException e1)
                    {
                        Logger.logE(User.class, "close socket channel failed.", e1);
                    }
                    user.state = 1;
                    User.this.handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            User.this.callback.onConnectedFailed(user, e);
                        }
                    });
                }
            }
        });
    }

    public void disconnectUser(final RemoteUser user)
    {
        this.callback.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (user.state != 2)
                    return;
                Iterator transfers = user.getTransfers().iterator();
                while (transfers.hasNext())
                {
                    try
                    {
                        final TransferEntity transfer = (TransferEntity) transfers.next();
                        SelectionKey key = transfer.getSelectionKey();
                        key.cancel();
                        key.channel().close();
                        transfers.remove();
                        transfer.state = 1;
                        User.this.handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                User.this.callback.onTransferFailed(transfer, new RuntimeException("transfer is cancelled."));
                            }
                        });
                    } catch (IOException e)
                    {
                        Logger.logE(User.class, "close socket channel failed.", e);
                    }
                }
                try
                {
                    SelectionKey userKey = user.getKey();
                    userKey.cancel();
                    userKey.channel().close();
                    user.state = 1;
                    User.this.connUsers.remove(user);
                    User.this.handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            User.this.callback.onDisconnected(user);
                        }
                    });
                } catch (final IOException e)
                {
                    User.this.handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            User.this.callback.onDisconnectedFailed(user, e);
                        }
                    });
                }
            }
        });
    }

    public void getConnectedUsers(final GetConnectedUsersCallback callback)
    {
        this.callback.post(new Runnable()
        {
            @Override
            public void run()
            {
                final LinkedList users = (LinkedList) User.this.connUsers.clone();
                User.this.handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        callback.onGet(users);
                    }
                });
            }
        });
    }

    public void sendTransferRequest(RemoteUser user, String description)
    {
        if (user.state != 2)
            throw new IllegalStateException("the input user has not been connected already.");
        SelectionKey key = user.getKey();
        Object[] objs = (Object[]) key.attachment();
        key.attach(new Object[] { objs[0], "transfer_request", description, this });
        key.interestOps(4);
    }

    public void replyTransferRequest(RemoteUser user, boolean allow, String description)
    {
        if (user.state != 2)
            throw new IllegalStateException("the input user has not been connected already.");
        SelectionKey key = user.getKey();
        Object[] objs = (Object[]) key.attachment();
        key.attach(new Object[] { objs[0], "transfer_reply", Boolean.valueOf(allow), description, this });
        key.interestOps(4);
    }

    public void sendTransfer(final RemoteUser user, final File file, final String extraDescription)
    {
        this.callback.post(new Runnable()
        {
            @Override
            public void run()
            {
                final TransferEntity transfer = new TransferEntity();
                transfer.setRemoteUser(user);
                transfer.setSendPath(file.getAbsolutePath());
                transfer.setSize(file.length());
                transfer.setSender(true);
                transfer.setExtraDescription(extraDescription);
                transfer.state = 0;
                user.getTransfers().add(transfer);
                SocketChannel sc = null;
                try
                {
                    if (user.state != 2)
                        throw new IOException("the input user has not been connected already.");
                    if (!file.isFile())
                        throw new FileNotFoundException("the input file is invalid.");
                    sc = SocketChannel.open();
                    sc.configureBlocking(false);
                    sc.connect(new InetSocketAddress(user.getIp(), 7001));
                    SelectionKey key = sc.register(User.this.selector, 8, new Object[] { user, "transfer_connect", transfer });
                    transfer.setSelectionKey(key);
                    User.this.handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            User.this.callback.onTransferProgress(transfer, 0);
                        }
                    });
                } catch (final IOException e)
                {
                    final SocketChannel scPoint = sc;
                    User.this.handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            User.this.callback.onTransferProgress(transfer, 0);
                            User.this.callback.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        transfer.state = 1;
                                        user.getTransfers().remove(transfer);
                                        if (scPoint != null)
                                            scPoint.close();
                                    } catch (IOException e)
                                    {
                                        Logger.logE(User.class, "close socket channel failed.", e);
                                    }
                                    User.this.handler.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            User.this.callback.onTransferFailed(transfer, e);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    public void cancelTransfer(final TransferEntity transfer)
    {
        this.callback.post(new Runnable()
        {
            @Override
            public void run()
            {
                SelectionKey key = transfer.getSelectionKey();
                if (key == null)
                    return;
                if (transfer.state == 1)
                    return;
                try
                {
                    transfer.state = 1;
                    transfer.getRemoteUser().getTransfers().remove(transfer);
                    key.cancel();
                    key.channel().close();
                } catch (IOException e)
                {
                    Logger.logE(User.class, "close socket channel failed.", e);
                }
                User.this.handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        User.this.callback.onTransferFailed(transfer, new RuntimeException("transfer is cancelled."));
                    }
                });
            }
        });
    }

    public void close(final Context context, final CloseCallback callback)
    {
        this.callback.post(new Runnable()
        {
            @Override
            public void run()
            {
                Exception firstExcep = null;
                try
                {
                    Set<SelectionKey> skeys = User.this.selector.keys();
                    for (SelectionKey curKey : skeys)
                    {
                        try
                        {
                            curKey.cancel();
                            curKey.channel().close();
                        } catch (IOException e)
                        {
                            if (firstExcep == null)
                                firstExcep = e;
                        }
                    }
                } catch (ClosedSelectorException e)
                {
                    if (firstExcep == null)
                        firstExcep = e;
                }
                try
                {
                    User.this.selector.close();
                } catch (IOException e)
                {
                    if (firstExcep == null)
                        firstExcep = e;
                }
                final Exception firstExcepPoint = firstExcep;
                User.this.handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (User.this.wakeLock.isHeld())
                            User.this.wakeLock.release();
                        WifiUtils wifiUtils = new WifiUtils(context);
                        WifiManager wm = wifiUtils.getWifiManager();
                        List<WifiConfiguration> wcs = wifiUtils.getConfigurations();
                        if (wcs != null)
                        {
                            for (WifiConfiguration wc : wcs)
                            {
                                String ssid = wc.SSID;
                                if ((ssid == null) || (!ssid.startsWith("\"MYFY")))
                                    continue;
                                wm.removeNetwork(wc.networkId);
                            }

                            wm.saveConfiguration();
                        }
                        User.this.closeDirectAp(context, new CloseDirectApCallback()
                        {
                            @Override
                            public void onClosed()
                            {
                                if (firstExcepPoint == null)
                                    callback.onClosed();
                                else
                                    callback.onError(firstExcepPoint);
                            }

                            @Override
                            public void onError(Exception e)
                            {
                                if (firstExcepPoint == null)
                                    callback.onError(e);
                                else
                                    callback.onError(firstExcepPoint);
                            }
                        });
                    }
                }, 1000L);
            }
        });
    }
}
