package com.flyn.telephone;

import android.telephony.SmsMessage;

public abstract interface SmsFilter
{
    public abstract boolean accept(SmsMessage paramSmsMessage);
}