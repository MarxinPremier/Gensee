package com.wangzuo.libgensee.service;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.wangzuo.libgensee.util.LogcatHelper;

public class LogCatService extends Service
{
    public void onCreate()
    {
        LogcatHelper.getInstance(this).start();
        super.onCreate();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy()
    {
        LogcatHelper.getInstance(this).stop();
        super.onDestroy();
    }
}
