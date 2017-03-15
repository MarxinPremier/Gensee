package com.wangzuo.libgensee.ui.holder.chat;

import com.wangzuo.libgensee.ui.holder.chat.impl.RTChatImpl;

public abstract class AbsChatImpl
{
    public abstract void sendPublicMsg(String paramString1, String paramString2);

    public abstract int getChatMode();

    public abstract void setOnChatModeChangeListener(RTChatImpl.OnChatModeChangeListener paramOnChatModeChangeListener);

    public abstract boolean getChatEnable();

    public abstract void release();
}
