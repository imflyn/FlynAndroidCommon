package com.greatwall.ui;

public interface ControllerListener
{
    <T extends BaseEvent> void onEvent(T event);
}