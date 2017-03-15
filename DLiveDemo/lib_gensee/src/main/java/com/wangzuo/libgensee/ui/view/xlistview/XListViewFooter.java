package com.wangzuo.libgensee.ui.view.xlistview;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.wangzuo.libgensee.util.ResManager;

public class XListViewFooter extends LinearLayout
{
    public static final int STATE_NORMAL = 0;
    public static final int STATE_READY = 1;
    public static final int STATE_LOADING = 2;
    private Context mContext;
    private View mContentView;
    private View mProgressBar;
    private TextView mHintView;

    public XListViewFooter(Context context)
    {
        super(context);
        initView(context);
    }

    public XListViewFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void setState(int state)
    {
        this.mHintView.setVisibility(4);
        this.mProgressBar.setVisibility(4);
        this.mHintView.setVisibility(4);
        if (state == 1) {
            this.mHintView.setVisibility(0);
            this.mHintView.setText(getFooterHintReadyStrId());
        } else if (state == 2) {
            this.mProgressBar.setVisibility(0);
        } else {
            this.mHintView.setVisibility(0);
            this.mHintView.setText(getFooterHintNormalStrId());
        }
    }

    public void setBottomMargin(int height) {
        if (height < 0) return;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)this.mContentView.getLayoutParams();
        lp.bottomMargin = height;
        this.mContentView.setLayoutParams(lp);
    }

    public int getBottomMargin() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)this.mContentView.getLayoutParams();
        return lp.bottomMargin;
    }

    public void normal()
    {
        this.mHintView.setVisibility(0);
        this.mProgressBar.setVisibility(8);
    }

    public void loading()
    {
        this.mHintView.setVisibility(8);
        this.mProgressBar.setVisibility(0);
    }

    public void hide()
    {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)this.mContentView.getLayoutParams();
        lp.height = 0;
        this.mContentView.setLayoutParams(lp);
    }

    public void show()
    {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)this.mContentView.getLayoutParams();
        lp.height = -2;
        this.mContentView.setLayoutParams(lp);
    }

    private void initView(Context context) {
        this.mContext = context;
        LinearLayout moreView = (LinearLayout)LayoutInflater.from(this.mContext).inflate(getFooterViewLayoutId(), null);
        addView(moreView);
        moreView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

        this.mContentView = moreView.findViewById(getFooterContentTvId());
        this.mProgressBar = moreView.findViewById(getFooterProgressbarId());
        this.mHintView = ((TextView)moreView.findViewById(getFooterHintTvId()));
    }

    private int getFooterHintReadyStrId()
    {
        return ResManager.getStringId("gs_xlistview_footer_hint_ready");
    }

    private int getFooterHintNormalStrId() {
        return ResManager.getStringId("gs_xlistview_footer_hint_normal");
    }

    private int getFooterContentTvId() {
        return ResManager.getId("gs_xlistview_footer_content");
    }

    private int getFooterProgressbarId() {
        return ResManager.getId("gs_xlistview_footer_progressbar");
    }

    private int getFooterHintTvId() {
        return ResManager.getId("gs_xlistview_footer_hint_textview");
    }

    private int getFooterViewLayoutId() {
        return ResManager.getLayoutId("gs_xlistview_footer");
    }
}
