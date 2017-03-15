package com.wangzuo.libgensee.ui.holder.chat;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import com.wangzuo.libgensee.adapter.GridViewAvatarAdapter.SelectAvatarInterface;
import com.wangzuo.libgensee.core.RTLive;
import com.wangzuo.libgensee.core.RTLive.OnHostStatusChangeListener;
import com.wangzuo.libgensee.entity.chat.AbsChatMessage;
import com.wangzuo.libgensee.ui.holder.chat.impl.MsgQueue;
import com.wangzuo.libgensee.ui.holder.chat.impl.MsgQueue.OnPublicChatHolderListener;
import com.wangzuo.libgensee.ui.holder.chat.impl.PlayerChatAdapter;
import com.wangzuo.libgensee.ui.holder.chat.impl.RTChatAdapter;
import com.wangzuo.libgensee.ui.view.xlistview.XListView;
import com.gensee.utils.ThreadPool;
import com.wangzuo.libgensee.adapter.GridViewAvatarAdapter;

import java.util.List;

public class SimpleChatHolder extends ChatHolder
        implements MsgQueue.OnPublicChatHolderListener, ViewPager.OnPageChangeListener, GridViewAvatarAdapter.SelectAvatarInterface, RTLive.OnHostStatusChangeListener
{
    public SimpleChatHolder(View rootView, Object value)
    {
        super(rootView, value);
        MsgQueue.getIns().setOnPublicChatHolderListener(this);
    }

    protected void initComp(Object value)
    {
        super.initComp(value);

        if (RTLive.getIns().isPublishMode())
            this.adapter = new RTChatAdapter();
        else {
            this.adapter = new PlayerChatAdapter();
        }
        this.lvChat.setAdapter(this.adapter);
        RTLive.getIns().setOnHostStatusChangeListener(this);
    }

    protected void initData(Object value)
    {
    }

    public void onClick(View v)
    {
    }

    private void refreshMsg(int what, List<AbsChatMessage> msgList, boolean bLatest)
    {
        Message message = new Message();
        message.obj = msgList;
        message.what = what;
        Bundle bundle = new Bundle();
        bundle.putBoolean("LATEST", bLatest);
        message.setData(bundle);
        sendMessage(message);
    }

    public void sendPublicMsg(String text, String rich) {
        if (this.chatImpl != null) {
            this.chatImpl.sendPublicMsg(text, rich);
        }

        if ((!getLvBottom()) || (!MsgQueue.getIns().isPublicLatest()))
            ThreadPool.getInstance().execute(new Runnable()
            {
                public void run()
                {
                    SimpleChatHolder.this.setLvBottom(true);
                    MsgQueue.getIns().getLatestMsgsList();
                }
            });
    }

    public boolean isLvBottom()
    {
        return getLvBottom();
    }

    public boolean isSelfLvBottom()
    {
        return false;
    }

    public void refreshSelfMsg(List<AbsChatMessage> msgList, boolean bLatest)
    {
    }

    public void onPullSelfMsg(List<AbsChatMessage> msgList, boolean bLatest)
    {
    }

    public void onLoadSelfMsg(List<AbsChatMessage> msgList, boolean bLatest)
    {
    }

    public void refreshMsg(List<AbsChatMessage> msgList, boolean bLatest)
    {
        refreshMsg(10000, msgList, bLatest);
    }

    public void onPullMsg(List<AbsChatMessage> msgList, boolean bLatest)
    {
        refreshMsg(10002, msgList, bLatest);
    }

    public void onLoadMsg(List<AbsChatMessage> msgList, boolean bLatest)
    {
        refreshMsg(10003, msgList, bLatest);
    }

    protected void refresh()
    {
        ThreadPool.getInstance().execute(new Runnable()
        {
            public void run()
            {
                MsgQueue.getIns().onMessageFresh();
            }
        });
    }

    protected void loadMore()
    {
        ThreadPool.getInstance().execute(new Runnable()
        {
            public void run()
            {
                MsgQueue.getIns().onMessageLoadMore();
            }
        });
    }

    public void onPageScrollStateChanged(int arg0)
    {
    }

    public void onPageScrolled(int arg0, float arg1, int arg2)
    {
    }

    public void onPageSelected(int arg0)
    {
    }

    public void selectAvatar(String sAvatar, Drawable drawable)
    {
    }

    public void onHostJoin(String hostName)
    {
    }

    public void onHostLeave(String hostName)
    {
    }
}
