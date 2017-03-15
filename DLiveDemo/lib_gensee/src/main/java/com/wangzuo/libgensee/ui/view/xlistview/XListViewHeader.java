package com.wangzuo.libgensee.ui.view.xlistview;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.wangzuo.libgensee.util.ResManager;

public class XListViewHeader extends LinearLayout
{
    private LinearLayout mContainer;
    private ImageView mArrowImageView;
    private ProgressBar mProgressBar;
    private TextView mHintTextView;
    private int mState = 0;
    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;
    private final int ROTATE_ANIM_DURATION = 180;
    public static final int STATE_NORMAL = 0;
    public static final int STATE_READY = 1;
    public static final int STATE_REFRESHING = 2;

    public XListViewHeader(Context context)
    {
        super(context);
        initView(context);
    }

    public XListViewHeader(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context)
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                -1, 0);
        this.mContainer = ((LinearLayout)LayoutInflater.from(context).inflate(
                getHeaderViewLayoutId(), null));
        addView(this.mContainer, lp);
        setGravity(80);

        this.mArrowImageView = ((ImageView)findViewById(getHeaderArrowIvId()));
        this.mHintTextView = ((TextView)findViewById(getHeaderHintTvId()));
        this.mProgressBar = ((ProgressBar)findViewById(getHeaderProgressbarId()));

        this.mRotateUpAnim = new RotateAnimation(0.0F, -180.0F,
                1, 0.5F, 1,
                0.5F);
        this.mRotateUpAnim.setDuration(180L);
        this.mRotateUpAnim.setFillAfter(true);
        this.mRotateDownAnim = new RotateAnimation(-180.0F, 0.0F,
                1, 0.5F, 1,
                0.5F);
        this.mRotateDownAnim.setDuration(180L);
        this.mRotateDownAnim.setFillAfter(true);
    }

    public void setState(int state) {
        if (state == this.mState) return;

        if (state == 2) {
            this.mArrowImageView.clearAnimation();
            this.mArrowImageView.setVisibility(4);
            this.mProgressBar.setVisibility(0);
        } else {
            this.mArrowImageView.setVisibility(0);

            this.mProgressBar.setVisibility(0);
        }

        switch (state) {
            case 0:
                if (this.mState == 1) {
                    this.mArrowImageView.startAnimation(this.mRotateDownAnim);
                }
                if (this.mState == 2) {
                    this.mArrowImageView.clearAnimation();
                }
                this.mHintTextView.setText(getHeaderHintNormalStrId());
                break;
            case 1:
                if (this.mState != 1) {
                    this.mArrowImageView.clearAnimation();
                    this.mArrowImageView.startAnimation(this.mRotateUpAnim);
                    this.mHintTextView.setText(getHeaderHintReadyStrId());
                }
                break;
            case 2:
                this.mHintTextView.setText(getHeaderHintLoadingStrId());
        }

        this.mState = state;
    }

    public void setVisiableHeight(int height) {
        if (height < 0)
            height = 0;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)this.mContainer
                .getLayoutParams();
        lp.height = height;
        this.mContainer.setLayoutParams(lp);
    }

    public int getVisiableHeight() {
        return this.mContainer.getHeight();
    }

    private int getHeaderViewLayoutId() {
        return ResManager.getLayoutId("gs_xlistview_header");
    }

    private int getHeaderHintNormalStrId() {
        return ResManager.getStringId("gs_xlistview_header_hint_normal");
    }

    private int getHeaderHintReadyStrId() {
        return ResManager.getStringId("gs_xlistview_header_hint_ready");
    }

    private int getHeaderHintLoadingStrId() {
        return ResManager.getStringId("gs_xlistview_header_hint_loading");
    }

    private int getHeaderArrowIvId() {
        return ResManager.getId("gs_xlistview_header_arrow");
    }

    private int getHeaderHintTvId() {
        return ResManager.getId("gs_xlistview_header_hint_textview");
    }

    private int getHeaderProgressbarId()
    {
        return ResManager.getId("gs_xlistview_header_progressbar");
    }
}
