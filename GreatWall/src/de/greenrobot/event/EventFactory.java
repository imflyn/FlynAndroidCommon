package de.greenrobot.event;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;

public class EventFactory
{

    private static EventFactory                                                                          eventFactory;

    private ConcurrentHashMap<Class<? extends BaseEvent>, ArrayList<EventListener<? extends BaseEvent>>> listeners = new ConcurrentHashMap<Class<? extends BaseEvent>, ArrayList<EventListener<? extends BaseEvent>>>();

    public static EventFactory getInstance()
    {
        if (null == eventFactory)
        {
            synchronized (EventFactory.class)
            {
                if (null == eventFactory)
                    eventFactory = new EventFactory();
            }
        }
        return eventFactory;
    }

    public void put(Class<? extends BaseEvent> clz, EventListener<? extends BaseEvent> listener)
    {
        if (null == listener)
            throw new IllegalStateException("EventListener can not be null");

        ArrayList<EventListener<? extends BaseEvent>> listenerList = this.listeners.get(clz);
        if (listenerList == null)
        {
            listenerList = new ArrayList<EventListener<? extends BaseEvent>>();
        }
        listenerList.add(listener);

        this.listeners.put(clz, listenerList);
    }

    public void remove(Class<? extends BaseEvent> clz, EventListener<? extends BaseEvent> listener)
    {
        ArrayList<EventListener<? extends BaseEvent>> eventListeners = this.listeners.get(clz);
        if (eventListeners == null)
            return;

        eventListeners.remove(listener);
    }

    public void post(BaseEvent event)
    {
        EventBus.getDefault().post(event);
    }

    private EventFactory()
    {
        EventBus.getDefault().register(this);
    }

    @SuppressWarnings("unchecked")
    public void onEventMainThread(BaseEvent event)
    {
        Log.i("aaaa", "onEventMainThread:" + event.getClass());

        Class<?> clz = event.getClass();
        ArrayList<EventListener<? extends BaseEvent>> listenerList = listeners.get(clz);
        if (listenerList != null)
        {
            EventListener<BaseEvent> listener;
            for (int i = 0, size = listenerList.size(); i < size; i++)
            {
                listener = (EventListener<BaseEvent>) listenerList.get(i);
                listener.onEventMainThread(event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void onEventBackgroundThread(BaseEvent event)
    {
        Log.i("aaaa", "onEventBackgroundThread:" + event.getClass());
        Class<?> clz = event.getClass();
        ArrayList<EventListener<? extends BaseEvent>> listenerList = listeners.get(clz);
        if (listenerList != null)
        {
            EventListener<BaseEvent> listener;
            for (int i = 0, size = listenerList.size(); i < size; i++)
            {
                listener = (EventListener<BaseEvent>) listenerList.get(i);
                listener.onEventBackgroundThread(event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void onEvent(BaseEvent event)
    {
        Log.i("aaaa", "onEvent:" + event.getClass());
        Class<?> clz = event.getClass();
        ArrayList<EventListener<? extends BaseEvent>> listenerList = listeners.get(clz);
        if (listenerList != null)
        {
            EventListener<BaseEvent> listener;
            for (int i = 0, size = listenerList.size(); i < size; i++)
            {
                listener = (EventListener<BaseEvent>) listenerList.get(i);
                listener.onEvent(event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void onEventAsync(BaseEvent event)
    {
        Log.i("aaaa", "onEventAsync:" + event.getClass());
        Class<?> clz = event.getClass();
        ArrayList<EventListener<? extends BaseEvent>> listenerList = listeners.get(clz);
        if (listenerList != null)
        {
            EventListener<BaseEvent> listener;
            for (int i = 0, size = listenerList.size(); i < size; i++)
            {
                listener = (EventListener<BaseEvent>) listenerList.get(i);
                listener.onEventAsync(event);
            }
        }
    }

    public static abstract class EventListener<T extends BaseEvent>
    {

        public void onEventMainThread(T event)
        {
        }

        public void onEventBackgroundThread(T event)
        {
        }

        public void onEvent(T event)
        {
        }

        public void onEventAsync(T event)
        {
        }
    }

}
