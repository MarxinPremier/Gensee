package com.wangzuo.libgensee.ui.holder;


import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.PopupWindow;
import com.wangzuo.libgensee.ui.view.xlistview.XListView;
import com.wangzuo.libgensee.ui.view.xlistview.XListView.IXListViewListener;
import com.wangzuo.libgensee.util.ResManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class LvHolder extends BaseHolder
        implements XListView.IXListViewListener, AbsListView.OnScrollListener
{
    protected String TAG = getClass().getSimpleName();
    protected XListView lvChat;
    private AtomicBoolean bLvBottom;
    protected boolean bRefreshing = false;

    private AtomicBoolean bLvIdle = new AtomicBoolean(true);
    private PopupWindow popupWindow;

    public XListView getLvChat()
    {
        return this.lvChat;
    }

    public LvHolder(View rootView, Object value) {
        super(rootView, value);
    }

    protected void initComp(Object value)
    {
        this.lvChat = ((XListView)findViewById(ResManager.getId("gs_chat_lv")));
        this.lvChat.setXListViewListener(this);
        this.lvChat.setOnScrollListener(this);
        this.lvChat.setPullLoadEnable(false);
        this.lvChat.setHeaderDividersEnabled(false);
        this.lvChat.setFooterDividersEnabled(false);
        this.bLvBottom = new AtomicBoolean(true);
    }

    public void updateXListViewUi(boolean bLatest) {
        if (!bLatest) {
            this.lvChat.addFootView();
            this.lvChat.setPullLoadEnable(true);
        } else {
            unEnableFootView();
        }
    }

    private void unEnableFootView() {
        this.lvChat.removeFootView();
        this.lvChat.setPullLoadEnable(false);
    }

    public void onLvReLoad() {
        this.lvChat.stopRefresh();
        onRefreshTime();
    }

    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        if (this.lvChat.getLastVisiblePosition() == this.lvChat.getCount() - 1)
            this.bLvBottom.set(true);
        else {
            this.bLvBottom.set(false);
        }
        switch (scrollState)
        {
            case 0:
                this.bLvIdle.set(true);
                break;
            case 1:
                this.bLvBottom.set(false);
                break;
            case 2:
                this.bLvBottom.set(false);
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        if (!this.bLvIdle.get())
        {
            if (firstVisibleItem + visibleItemCount == totalItemCount)
            {
                this.bLvBottom.set(true);
            }
            else
                this.bLvBottom.set(false);
        }
    }

    protected void onRefreshTime()
    {
        this.lvChat.setRefreshTime(getStringDate());
    }

    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    public boolean getLvBottom() {
        return this.bLvBottom.get();
    }

    protected void setLvBottom(boolean bTrue)
    {
        this.bLvBottom.set(bTrue);
    }

    public void onRefresh()
    {
        if (this.bRefreshing) {
            onLvReLoad();
            return;
        }
        this.bRefreshing = true;
        refresh();
    }

    public void onLoadMore()
    {
        if (this.bRefreshing) {
            this.lvChat.stopLoadMore();
            return;
        }
        this.bRefreshing = true;
        loadMore();
    }

    protected abstract void refresh();

    protected abstract void loadMore();

    public String replaceGifToText(String richText)
    {
        if ((richText.startsWith("<SPAN>")) && (richText.endsWith("</SPAN>"))) {
            richText = richText.substring(6, richText.length() - 7);
        }
        String startStr = "<IMG src=";
        String endStr = "custom=\"false\">";
        if (richText.indexOf(startStr) != -1) {
            String leftPart = richText.substring(0, richText.indexOf(startStr));
            String rightPart = richText.substring(richText.indexOf(endStr) + endStr.length(), richText.length());
            richText = leftPart + " [Emoji] " + rightPart;
            richText = replaceGifToText(richText);
        }
        return richText;
    }
}
