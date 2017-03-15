package com.wangzuo.libgensee.ui.holder.chat;


import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gensee.chat.gif.SpanResource;
import com.wangzuo.libgensee.adapter.ChatViewPageAdapter;
import com.wangzuo.libgensee.adapter.GridViewAvatarAdapter;
import com.wangzuo.libgensee.ui.holder.InputBottomHolder;
import com.wangzuo.libgensee.ui.view.CustomInputDialog;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.routine.UserInfo;
import com.wangzuo.libgensee.ui.BaseActivity;
import com.wangzuo.libgensee.ui.holder.chat.impl.RTChatImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DialogInputHolder extends InputBottomHolder
        implements GridViewAvatarAdapter.SelectAvatarInterface, ViewPager.OnPageChangeListener, RTChatImpl.OnChatModeChangeListener {
    public static final int MEXPRESSIONNUM = 18;
    private CustomInputDialog mDialog;
    private ChatViewPageAdapter mChatViewPageAdapter;
    private ViewPager mViewPage;
    private GridView mGridViewlayout;
    private int mPageExpresion;
    private LinearLayout mLinearLayoutCourse;
    protected LinearLayout mLinearLayoutExpLin;
    private List<ImageView> mListImageViewExp;
    private List<LinearLayout> mListLinearLayoutExp;
    private GridViewAvatarAdapter mGridViewAvatarAdapter;
    private SimpleChatHolder simpleChatHolder;

    public DialogInputHolder(View rootView, Object value) {
        super(rootView, value);
        this.mDialog = ((CustomInputDialog) value);
    }

    protected void initComp(Object value) {
        super.initComp(value);
        this.lyBottomTop.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == 1) {
                    DialogInputHolder.this.mDialog.dismiss();
                }
                return false;
            }
        });
        this.simpleChatHolder = ((BaseActivity) getContext()).getSimpleChatHolder();
        int roomChatMode = 1;
        ((BaseActivity) getContext()).getChatImpl().setOnChatModeChangeListener(this);
        roomChatMode = ((BaseActivity) getContext()).getChatImpl().getChatMode();
        onChatModeChage(roomChatMode);
        if (roomChatMode != 0)
            onSelfChatEnable(((BaseActivity) getContext()).getChatImpl().getChatEnable());
    }

    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == ResManager.getId("gs_chat_avatar_iv"))
            selectAvatar();
        else if (v.getId() == ResManager.getId("gs_chat_content_edt"))
            emotionPanel(false);
    }

    protected void send(String text, String rich) {
        this.simpleChatHolder.sendPublicMsg(text, rich);
    }

    public void hide() {
        this.ivAvatar.setSelected(false);
        this.mLinearLayoutExpLin.setVisibility(View.GONE);
        hideDialog();
        super.hide();
    }

    protected void selectAvatar() {
        this.ivAvatar.setSelected(!this.ivAvatar.isSelected());
        if (this.ivAvatar.isSelected())
            hideKeyBoard();
        else {
            showKeyBoard();
        }
        if (this.keyBoradStatus == 0)
            postDelayed(new Runnable() {
                            public void run() {
                                DialogInputHolder.this.mLinearLayoutExpLin
                                        .setVisibility(DialogInputHolder.this.ivAvatar.isSelected() ? View.VISIBLE : View.GONE);
                            }
                        }
                    , 150L);
    }

    protected void keyBoardShow(boolean bVisible) {
        if (!bVisible)
            if (this.ivAvatar.isSelected())
                postDelayed(new Runnable() {
                                public void run() {
                                    DialogInputHolder.this.mLinearLayoutExpLin.setVisibility(View.VISIBLE);
                                }
                            }
                        , 150L);
            else
                hideDialog();
    }

    private void hideDialog() {
        if ((this.mDialog != null) && (this.mDialog.isShowing()))
            this.mDialog.dismiss();
    }

    protected void initAvatar(View view) {
        this.mListImageViewExp = new ArrayList();

        List mListGridView = new ArrayList();
        this.mListLinearLayoutExp = new ArrayList();
        this.mLinearLayoutExpLin =
                ((LinearLayout) view
                        .findViewById(ResManager.getId("gs_viewpageexpressionlinear")));
        int mSumExpresion = SpanResource.getBrowMap(getContext()).keySet()
                .toArray().length;

        if (mSumExpresion % 18 == 0)
            this.mPageExpresion = (mSumExpresion / 18);
        else {
            this.mPageExpresion = (mSumExpresion / 18 + 1);
        }
        this.mLinearLayoutCourse =
                ((LinearLayout) view
                        .findViewById(ResManager.getId("gs_chatexpressaddimg")));

        for (int i = 0; i < this.mPageExpresion * 2; i++) {
            View mGridViewLayoutExp = LayoutInflater.from(getContext())
                    .inflate(ResManager.getLayoutId("gs_chat_gridview_expression_layout"), null);

            if (i < this.mPageExpresion) {
                ImageView mImageView = new ImageView(getContext());
                if (i == 0)
                    mImageView
                            .setBackgroundResource(ResManager.getDrawableId("gs_chat_viewpage_fource"));
                else {
                    mImageView
                            .setBackgroundResource(ResManager.getDrawableId("gs_chat_viewpage_unfource"));
                }
                LinearLayout.LayoutParams mPlayout = new LinearLayout.LayoutParams(
                        -2, -2);
                mPlayout.setMargins(0, 10, 10, 10);
                mImageView.setLayoutParams(mPlayout);
                this.mLinearLayoutCourse.addView(mImageView);
                this.mListImageViewExp.add(mImageView);
            }

            int index = i % this.mPageExpresion;
            this.mGridViewAvatarAdapter = new GridViewAvatarAdapter(getContext(),
                    this, index * 18, mSumExpresion - index *
                    18);

            this.mGridViewlayout =
                    ((GridView) mGridViewLayoutExp
                            .findViewById(ResManager.getId("gs_allexpressionGrid")));
            this.mGridViewlayout.setAdapter(this.mGridViewAvatarAdapter);
            mListGridView.add(mGridViewLayoutExp);
            LinearLayout mLExpressionLinearlayout = (LinearLayout) view
                    .findViewById(ResManager.getId("gs_expr_tran_linearlayout"));
            this.mListLinearLayoutExp.add(mLExpressionLinearlayout);
        }

        this.mChatViewPageAdapter = new ChatViewPageAdapter(mListGridView);
        this.mViewPage = ((ViewPager) view.findViewById(ResManager.getId("gs_viewpager")));
        this.mViewPage.setVisibility(View.VISIBLE);
        this.mViewPage.setAdapter(this.mChatViewPageAdapter);
        this.mViewPage.setCurrentItem(mListGridView.size() * 100);
        this.mViewPage.setOnPageChangeListener(this);
    }

    public void selectAvatar(String sAvatar, Drawable drawable) {
        insertValue(sAvatar);
    }

    public void onPageScrolled(int i, float v, int i1) {
    }

    public void onPageSelected(int i) {
    }

    public void onPageScrollStateChanged(int i) {
    }

    protected void emotionPanel(boolean bVisible) {
        this.mLinearLayoutExpLin.setVisibility(bVisible ? View.VISIBLE : View.GONE);
        if (!bVisible)
            this.ivAvatar.setSelected(false);
    }

    public void onChatModeChage(final int nChatMode) {
        post(new Runnable() {
            public void run() {
                if (nChatMode == 0) {
                    DialogInputHolder.this.chatEnable(false, ResManager.getStringId("gs_chat_unenable"));
                } else if (nChatMode == 1) {
                    boolean isSelfChatEnable = ((BaseActivity) DialogInputHolder.this.getContext()).getChatImpl().getChatEnable();
                    if (isSelfChatEnable)
                        DialogInputHolder.this.chatEnable(true, 0);
                    else
                        DialogInputHolder.this.chatEnable(false, ResManager.getStringId("gs_user_disable_chat"));
                }
            }
        });
    }

    public void onSelfChatEnable(final boolean enable) {
        post(new Runnable() {
            public void run() {
                int nChatMode = ((BaseActivity) DialogInputHolder.this.getContext()).getChatImpl().getChatMode();
                if (enable) {
                    if (nChatMode == 0)
                        DialogInputHolder.this.chatEnable(false, ResManager.getStringId("gs_chat_unenable"));
                    else {
                        DialogInputHolder.this.chatEnable(true, 0);
                    }
                } else if (nChatMode == 1)
                    DialogInputHolder.this.chatEnable(false, ResManager.getStringId("gs_user_disable_chat"));
            }
        });
    }

    public void onPrivateMsg(UserInfo toUser) {
    }
}
