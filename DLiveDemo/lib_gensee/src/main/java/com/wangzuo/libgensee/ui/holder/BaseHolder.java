package com.wangzuo.libgensee.ui.holder;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

public abstract class BaseHolder extends Handler
        implements View.OnClickListener
{
    protected View rootView;

    public BaseHolder(View rootView, Object value)
    {
        this.rootView = rootView;
        initData(value);
        initComp(value);
    }

    public BaseHolder(LayoutInflater inflater, int layoutID, Object value) {
        this(inflater.inflate(layoutID, null), value);
    }

    public void sendMessage(int what, Object object) {
        sendMessage(obtainMessage(what, object));
    }

    public final void handleMessage(Message msg)
    {
        onMessage(msg.what, msg.obj, msg.getData());
    }

    public void onMessage(int what, Object obj, Bundle bundle) {
    }

    protected View findViewById(int resId) {
        return this.rootView.findViewById(resId);
    }
    protected abstract void initData(Object paramObject);

    protected abstract void initComp(Object paramObject);

    public void layout(Bundle saveInstance) {
    }

    protected Context getContext() {
        return this.rootView.getContext();
    }

    public View getRootView() {
        return this.rootView;
    }

    public String getString(int resId) {
        return getContext().getResources().getString(resId);
    }

    public void show(boolean isShow) {
        if (isShow) {
            if (this.rootView.getVisibility() != 0) {
                this.rootView.setVisibility(0);
            }
        }
        else if (8 != this.rootView.getVisibility())
            this.rootView.setVisibility(8);
    }

    public boolean isShow()
    {
        return this.rootView.getVisibility() == 0;
    }

    protected void startIntent(Class<?> activityClass, Bundle bundle) {
        Intent intent = new Intent(getContext(), activityClass);
        intent.putExtras(bundle);
        getContext().startActivity(intent);
    }

    protected void startIntent(Class<?> activityClass) {
        Intent intent = new Intent(getContext(), activityClass);
        getContext().startActivity(intent);
    }

    public void onSaveInstence(Bundle outState)
    {
    }

    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
    }
}