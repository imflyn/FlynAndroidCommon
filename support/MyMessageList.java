package com.talkingoa.android.app.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.talkingoa.android.app.bean.message.ImageMessageBean;
import com.talkingoa.android.app.bean.message.MessageBean;
import com.talkingoa.android.app.bean.message.MessageStatus;
import com.talkingoa.android.app.bean.message.TextMessageBean;
import com.talkingoa.android.app.bean.message.VoiceMessageBean;
import com.talkingoa.android.app.ui.adapterview.DataHolder;
import com.talkingoa.android.app.ui.chat.ChatFromImageDataHolder;
import com.talkingoa.android.app.ui.chat.ChatFromTextDataHolder;
import com.talkingoa.android.app.ui.chat.ChatFromVoiceDataHolder;
import com.talkingoa.android.app.ui.chat.ChatToImageDataHolder;
import com.talkingoa.android.app.ui.chat.ChatToTextDataHolder;
import com.talkingoa.android.app.ui.chat.ChatToVoiceDataHolder;
import com.talkingoa.android.app.ui.chat.ChattingAdapter2.MessageClickListener;

public class MyMessageList<T> extends ArrayList<MessageBean> implements Comparator<MessageBean>
{

    private static final long     serialVersionUID = 1L;

    private ArrayList<String>     timeList         = new ArrayList<String>();
    private ArrayList<DataHolder> dataHolder       = new ArrayList<DataHolder>();

    private MessageClickListener  clickListener;

    public MyMessageList( MessageClickListener listener)
    {
        super();
        this.clickListener = listener;
    }

    public final ArrayList<DataHolder> getDataHolder()
    {
        return this.dataHolder;
    }

    public boolean add(MessageBean msgBean, boolean isSort)
    {
        if (this.timeList.contains(msgBean.getTime()))
            return false;

        this.timeList.add(msgBean.getTime());
        this.dataHolder.add(getDataHolder(msgBean));
        boolean bol = add(msgBean);
        if (isSort)
        {
            Collections.sort(this, this);
            Collections.sort(this.timeList, this.comparator);
            Collections.sort(this.dataHolder, this.dataHolderComparator);
        }
        return bol;
    }

    @Override
    public void clear()
    {
        this.timeList.clear();
        this.dataHolder.clear();
        super.clear();
    }

    @Override
    public MessageBean remove(int index)
    {
        try
        {
            this.dataHolder.remove(this.timeList.indexOf(get(index).getTime()));
            this.timeList.remove(this.timeList.indexOf(get(index).getTime()));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return super.remove(index);
    }

    /** 替换消息
     * 
     * @param messageId
     * @param msgBean */
    public void replaceMessageBean(String messageId, MessageBean msgBean)
    {
        if (size() < 0)
            return;
        try
        {
            int index = this.timeList.indexOf(messageId);
            set(index, msgBean);
            this.dataHolder.set(index, getDataHolder(msgBean));
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private DataHolder getDataHolder(MessageBean msgBean)
    {
        DataHolder holder = null;
        switch (msgBean.getDirection())
        {
            case MessageStatus.MESSAGE_FROM:
                if (msgBean instanceof TextMessageBean)
                {
                    holder = new ChatFromTextDataHolder(msgBean, 0, this.clickListener);
                } else if (msgBean instanceof ImageMessageBean)
                {
                    holder = new ChatFromImageDataHolder(msgBean, 0, this.clickListener);
                } else if (msgBean instanceof VoiceMessageBean)
                {
                    holder = new ChatFromVoiceDataHolder( msgBean, 0, this.clickListener);
                }
                break;
            case MessageStatus.MESSAGE_TO:
                if (msgBean instanceof TextMessageBean)
                {
                    holder = new ChatToTextDataHolder(msgBean, 0, this.clickListener);
                } else if (msgBean instanceof ImageMessageBean)
                {
                    holder = new ChatToImageDataHolder(msgBean, 0, this.clickListener);
                } else if (msgBean instanceof VoiceMessageBean)
                {
                    holder = new ChatToVoiceDataHolder( msgBean, 0, this.clickListener);
                }
                break;
            default:
                break;
        }
        return holder;
    }

    @Override
    public int compare(MessageBean lhs, MessageBean rhs)
    {

        if (Long.valueOf(lhs.getTime()) > Long.valueOf(rhs.getTime()))
            return 1;

        return -1;
    }

    private Comparator<String>     comparator           = new Comparator<String>()
                                                        {

                                                            @Override
                                                            public int compare(String lhs, String rhs)
                                                            {
                                                                if (Long.valueOf(lhs) > Long.valueOf(rhs))
                                                                    return 1;

                                                                return -1;
                                                            }
                                                        };

    private Comparator<DataHolder> dataHolderComparator = new Comparator<DataHolder>()
                                                        {
                                                            @Override
                                                            public int compare(DataHolder lhs, DataHolder rhs)
                                                            {
                                                                if (Long.valueOf(((MessageBean) (lhs.getData())).getTime()) > Long.valueOf(((MessageBean) (rhs.getData())).getTime()))
                                                                    return 1;

                                                                return -1;
                                                            }
                                                        };

}
