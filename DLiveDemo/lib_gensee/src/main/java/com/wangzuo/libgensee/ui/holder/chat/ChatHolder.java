package com.wangzuo.libgensee.ui.holder.chat;


import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import com.wangzuo.libgensee.adapter.AbstractAdapter;
import com.wangzuo.libgensee.entity.chat.AbsChatMessage;
import com.wangzuo.libgensee.ui.holder.LvHolder;
import com.wangzuo.libgensee.ui.view.xlistview.XListView;
import com.wangzuo.libgensee.ui.view.xlistview.XListView.IXListViewListener;
import java.util.List;

public abstract class ChatHolder extends LvHolder
        implements XListView.IXListViewListener, AbsListView.OnScrollListener
{
    protected String TAG = getClass().getSimpleName();
    protected AbsChatImpl chatImpl;
    protected AbstractAdapter adapter;
    private OnCalcLVHeightAfterNewMsgListener calcLVListener;

    public ChatHolder(View rootView, Object value)
    {
        super(rootView, value);
        this.chatImpl = ((AbsChatImpl)value);
    }

    public void setCalcLVHeightAfterNewMsgListener(OnCalcLVHeightAfterNewMsgListener l) {
        this.calcLVListener = l;
    }

    protected void notifyData(final List<AbsChatMessage> msgList)
    {
        if (this.adapter != null) {
            this.adapter.notifyData(msgList);
            postDelayed(new Runnable()
                        {
                            public void run()
                            {
                                if (ChatHolder.this.getLvBottom())
                                {
                                    ChatHolder.this.lvChat.setSelection(msgList.size());
                                }
                            }
                        }
                    , 200L);
        }
    }

    public void onMessage(int what, Object obj, Bundle bundle)
    {
        super.onMessage(what, obj, bundle);
        switch (what) {
            case 10003:
            case 10006:
            case 30003:
                this.lvChat.stopLoadMore();
                notifyData((List)obj);
                boolean bLatest = bundle.getBoolean("LATEST");
                updateXListViewUi(bLatest);
                this.bRefreshing = false;
                break;
            case 10000:
            case 10004:
            case 30000:
                boolean bLatestNewMsg = bundle.getBoolean("LATEST");
                if (this.calcLVListener != null)
                {
                    this.calcLVListener.calcLVHeightAfterNewMsg();
                }
                notifyData((List)obj);
                updateXListViewUi(bLatestNewMsg);
                break;
            case 10002:
            case 10005:
            case 30002:
                boolean bLatest1 = bundle.getBoolean("LATEST");
                updateXListViewUi(bLatest1);
                notifyData((List)obj);
                onLvReLoad();
                this.bRefreshing = false;
        }
    }

    public static abstract interface OnCalcLVHeightAfterNewMsgListener
    {
        public abstract void calcLVHeightAfterNewMsg();
    }
}
