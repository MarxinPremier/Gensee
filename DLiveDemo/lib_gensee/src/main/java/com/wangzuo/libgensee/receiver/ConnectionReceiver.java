package com.wangzuo.libgensee.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionReceiver extends BroadcastReceiver
{
    private OnNetSwitchListener listener;

    public void setOnNetSwitchListener(OnNetSwitchListener l)
    {
        this.listener = l;
    }

    public IntentFilter getFilter() {
        return new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    }

    public void onReceive(Context context, Intent intent)
    {
        onNetChanged(context);
    }

    public static int getNetType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context
                .getSystemService("connectivity");

        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if ((info == null) || (!info.isConnected())) {
            return 5;
        }
        int retType = 4;
        switch (info.getType()) {
            case 1:
            case 7:
            case 9:
                retType = 4;
                break;
            case 0:
            case 2:
            case 3:
            case 4:
            case 5:
                int subType = info.getSubtype();
                retType = getClassType(subType);
                break;
            case 6:
            case 8:
        }

        return retType;
    }

    public void onNetChanged(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context
                .getSystemService("connectivity");
        NetworkInfo mobNetInfo = connectivityManager
                .getNetworkInfo(0);
        boolean isMobile = mobNetInfo != null ? mobNetInfo.isConnected() :
                false;
        NetworkInfo wifiNetInfo = connectivityManager
                .getNetworkInfo(1);
        boolean isWifi = wifiNetInfo != null ? wifiNetInfo.isConnected() :
                false;

        OnNetSwitchListener l = this.listener;
        if ((l == null) &&
                ((context instanceof OnNetSwitchListener))) {
            l = (OnNetSwitchListener)context;
        }

        if (l == null) {
            return;
        }

        if ((!isWifi) && (!isMobile))
        {
            l.onSwitchMobile(5);
        }
        else if (isWifi)
        {
            l.onSwitchMobile(4);
        }
        else
        {
            int subType = mobNetInfo.getSubtype();
            l.onSwitchMobile(getClassType(subType));
        }
    }

    private static int getClassType(int subType)
    {
        switch (subType) {
            case 1:
            case 2:
            case 4:
            case 7:
            case 11:
                return 1;
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case 15:
                return 2;
            case 13:
                return 3;
        }

        return 0;
    }

    public static abstract interface OnNetSwitchListener
    {
        public static final int NETWORK_CLASS_UNKNOWN = 0;
        public static final int NETWORK_CLASS_2_G = 1;
        public static final int NETWORK_CLASS_3_G = 2;
        public static final int NETWORK_CLASS_4_G = 3;
        public static final int NETWORK_CLASS_WIFI = 4;
        public static final int NETWORK_CLASS_NO = 5;

        public abstract void onSwitchMobile(int paramInt);
    }
}