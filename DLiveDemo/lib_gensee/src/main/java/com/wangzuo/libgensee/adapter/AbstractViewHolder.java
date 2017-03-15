package com.wangzuo.libgensee.adapter;

import android.view.View;

public abstract class AbstractViewHolder
{
    public AbstractViewHolder(View view)
    {
        initView(view);
    }

    public abstract void initView(View paramView);

    public abstract void initValue(int paramInt);
}