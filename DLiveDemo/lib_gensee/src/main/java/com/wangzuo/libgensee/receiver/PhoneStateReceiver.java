package com.wangzuo.libgensee.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import com.gensee.utils.GenseeLog;

public class PhoneStateReceiver extends BroadcastReceiver
{
    public static final String B_PHONE_STATE = "android.intent.action.PHONE_STATE";
    private OnPhoneStateListener onPhoneStateListener;

    public void setOnPhoneStateListener(OnPhoneStateListener onPhoneStateListener)
    {
        this.onPhoneStateListener = onPhoneStateListener;
    }

    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();

        if (action.equals("android.intent.action.PHONE_STATE"))
            doReceivePhone(context, intent);
    }

    public void doReceivePhone(Context context, Intent intent)
    {
        String phoneNumber = intent
                .getStringExtra("incoming_number");
        TelephonyManager telephony = (TelephonyManager)context
                .getSystemService("phone");
        int state = telephony.getCallState();
        switch (state) {
            case 1:
                if (this.onPhoneStateListener != null)
                {
                    this.onPhoneStateListener.callRinging();
                }
                GenseeLog.i("[Broadcast]callringing等待接电话=" + phoneNumber);
                break;
            case 0:
                GenseeLog.i("[Broadcast]callringing电话挂断=" + phoneNumber);
                if (this.onPhoneStateListener != null)
                {
                    this.onPhoneStateListener.callOffHook();
                }
                break;
            case 2:
                GenseeLog.i("[Broadcast]callringing通话中=" + phoneNumber);
                if (this.onPhoneStateListener != null)
                {
                    this.onPhoneStateListener.callRinging();
                }
                break;
        }
    }

    public static abstract interface OnPhoneStateListener
    {
        public abstract void callRinging();

        public abstract void callOffHook();
    }
}
