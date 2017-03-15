package com.wangzuo.libgensee.ui;


import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import com.wangzuo.libgensee.ui.holder.chat.SimpleChatHolder;

public class BaseFragment extends Fragment
{
    protected String TAG = getClass().getSimpleName();
    protected SimpleChatHolder mChatHolder;
    protected View rootView;

    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    protected void onRoomJoinSuccess()
    {
    }

    protected void onRoomReconnect()
    {
    }

    protected void updateTitle(String title)
    {
    }
}
