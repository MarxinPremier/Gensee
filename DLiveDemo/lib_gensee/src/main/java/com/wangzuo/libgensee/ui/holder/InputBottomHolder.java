package com.wangzuo.libgensee.ui.holder;


import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gensee.chat.gif.SpanResource;
import com.wangzuo.libgensee.core.RTLive;
import com.wangzuo.libgensee.ui.holder.chat.impl.RTChatImpl.OnMsgBottomListener;
import com.wangzuo.libgensee.ui.view.ResizeLayout;
import com.wangzuo.libgensee.ui.view.ResizeLayout.OnResizeListener;
import com.wangzuo.libgensee.util.GenseeUtils;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.routine.UserInfo;
import com.gensee.view.ChatEditText;
import com.wangzuo.libgensee.ui.holder.chat.impl.RTChatImpl;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public abstract class InputBottomHolder extends BaseHolder
        implements RTChatImpl.OnMsgBottomListener, View.OnFocusChangeListener, ResizeLayout.OnResizeListener
{
    private static final int BOTTOM_TIP_LENGHT = 3000;
    public static final int SELECT_PUBLIC_INDEX = 0;
    public static final int SELECT_PRIVATE_INDEX = 1;
    public static final int SELECT_QA_INDEX = 2;
    public static final int SELECT_DOC_INDEX = 3;
    protected static final int KEYBOARD_SHOW = 1;
    protected static final int KEYBOARD_HIDE = 0;
    protected int keyBoradStatus = 0;
    protected int nCurSelectIndex;
    private OnPublicChatBottomListener onPublicChatBottomListener;
    private OnPrivateChatBottomListener onPrivateChatBottomListener;
    private OnQaBottomListener onQaBottomListener;
    protected ChatEditText edtChat;
    protected ImageView ivAvatar;
    private TextView tvSend;
    protected LinearLayout lyChat;
    protected View lyBottomTop;
    private TextView tvChatTip;
    private InputMethodManager inputMethodManager;
    private long chatPreTime = 0L;
    private LinearLayout lyMsgBottom;
    private TextView tvMsgBottom;
    private int nSelelctSelfStatus = 0;
    private static final int PUBLIC_CHAT_SELECT_SELF = 1;
    private static final int QA_SELECT_SELF = 2;
    private String sPublicContent = "";
    private String sQaContent = "";

    private UserInfo tmpToUser = null;
    private boolean bResume = true;
    private String tmpQaName = null;

    private Runnable msgBottomRunnable = new Runnable()
    {
        public void run()
        {
            InputBottomHolder.this.lyMsgBottom.setVisibility(View.GONE);
        }
    };

    public InputBottomHolder(View rootView, Object value)
    {
        super(rootView, value);
    }

    public void onClick(View v)
    {
        if (v.getId() == ResManager.getId("gs_chat_send_tv"))
            send();
    }

    protected void initData(Object value)
    {
    }

    public void onStop()
    {
        this.bResume = false;
    }

    protected void initComp(Object value)
    {
        if ((this.rootView != null) && ((this.rootView instanceof ResizeLayout))) {
            ((ResizeLayout)this.rootView).setOnResizeListener(this);
        }
        this.lyBottomTop = findViewById(ResManager.getId("input_bottom_top_ly"));
        this.lyBottomTop.setOnTouchListener(new View.OnTouchListener()
        {
            public boolean onTouch(View v, MotionEvent event)
            {
                GenseeUtils.hideSoftInputmethod(InputBottomHolder.this.getContext());
                InputBottomHolder.this.emotionPanel(false);
                return false;
            }
        });
        this.edtChat = ((ChatEditText)findViewById(ResManager.getId("gs_chat_content_edt")));
        this.edtChat.setOnClickListener(this);
        this.edtChat.setOnFocusChangeListener(this);
        this.edtChat.addTextChangedListener(new TextWatcher()
        {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                InputBottomHolder.this.tvSend.setSelected(!TextUtils.isEmpty(s));
            }

            public void afterTextChanged(Editable s)
            {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }
        });
        this.tvChatTip = ((TextView)findViewById(ResManager.getId("gs_chat_tip_tv")));

        this.ivAvatar = ((ImageView)findViewById(ResManager.getId("gs_chat_avatar_iv")));
        this.ivAvatar.setOnClickListener(this);
        this.lyChat = ((LinearLayout)findViewById(ResManager.getId("gs_chat_ly")));

        this.tvSend = ((TextView)findViewById(ResManager.getId("gs_chat_send_tv")));
        this.tvSend.setOnClickListener(this);

        initAvatar(this.rootView);

        this.lyMsgBottom = ((LinearLayout)findViewById(ResManager.getId("gs_msg_bottom_ly")));
        this.tvMsgBottom = ((TextView)findViewById(ResManager.getId("gs_msg_bottom_tv")));

        this.nCurSelectIndex = 3;
    }

    public void onFocusChange(View v, boolean hasFocus)
    {
        v.getId(); ResManager.getId("gs_chat_content_edt");
    }

    private void chatLyBg()
    {
        if (this.lyChat.isEnabled())
            this.lyChat.setBackgroundResource(ResManager.getDrawableId("gs_chat_input_bg"));
        else
            this.lyChat.setBackgroundResource(ResManager.getDrawableId("gs_chat_input_bg_unable"));
    }

    public void hideKeyBoard()
    {
        if ((this.inputMethodManager == null) &&
                (this.inputMethodManager == null)) {
            this.inputMethodManager =
                    ((InputMethodManager)getContext()
                            .getSystemService("input_method"));
        }

        this.inputMethodManager.hideSoftInputFromWindow(this.edtChat.getWindowToken(), 0);
    }

    public void showKeyBoard() {
        this.edtChat.setFocusable(true);
        this.edtChat.setFocusableInTouchMode(true);
        this.edtChat.requestFocus();
        if (this.inputMethodManager == null) {
            this.inputMethodManager =
                    ((InputMethodManager)getContext()
                            .getSystemService("input_method"));
        }

        Timer timer = new Timer();

        timer.schedule(new TimerTask()
                       {
                           public void run()
                           {
                               InputBottomHolder.this.inputMethodManager.toggleSoftInputFromWindow(
                                       InputBottomHolder.this.edtChat.getWindowToken(), 1, 0);
                           }
                       }
                , 100L);
    }

    protected void hide()
    {
        this.edtChat.setText("");
        hideKeyBoard();
    }

    protected void initAvatar(View view)
    {
    }

    private void send() {
        String sContent = this.edtChat.getRichText();
        if ("".equals(sContent))
        {
            return;
        }
        if (RTLive.getIns().isReconnecting()) {
            showMsgBottom(getString(ResManager.getStringId("gs_net_have_disconnect_tip")));
            return;
        }

        long nCurrent = Calendar.getInstance().getTimeInMillis();
        if (nCurrent - this.chatPreTime > 2000L) {
            String sText = (this.nCurSelectIndex == 2 ? this.edtChat
                    .getText() : this.edtChat.getChatText()).toString();
            send(sText, sContent);
            hide();
            if (this.nCurSelectIndex == 0)
            {
                this.sPublicContent = "";
            }
            else if (this.nCurSelectIndex == 2)
            {
                this.sQaContent = "";
            }
            this.chatPreTime = nCurrent;
        } else {
            showMsgBottom(getString(ResManager.getStringId("gs_chat_quickly")));
        }
    }

    protected void insertValue(String sValue)
    {
        this.edtChat.insertAvatar(sValue, 0);
    }

    protected void send(String text, String rich)
    {
    }

    public void setOnPublicChatBottomListener(OnPublicChatBottomListener onPublicChatBottomListener)
    {
        this.onPublicChatBottomListener = onPublicChatBottomListener;
    }

    public void setOnPrivateChatBottomListener(OnPrivateChatBottomListener onPrivateChatBottomListener)
    {
        this.onPrivateChatBottomListener = onPrivateChatBottomListener;
    }

    public void onChatMode(final int nChatMode)
    {
        post(new Runnable()
        {
            public void run() {
                if (nChatMode == 0)
                    InputBottomHolder.this.chatEnable(false, ResManager.getStringId("gs_chat_unenable"));
                else if (nChatMode == 1)
                    InputBottomHolder.this.chatEnable(true, ResManager.getStringId("gs_chat_unenable"));
            }
        });
    }

    private void showMsgBottom(String msg)
    {
        if (this.lyMsgBottom.getVisibility() != View.VISIBLE) {
            this.lyMsgBottom.setVisibility(View.VISIBLE);
        }
        this.tvMsgBottom.setText(msg);

        if (this.msgBottomRunnable != null) {
            removeCallbacks(this.msgBottomRunnable);
            postDelayed(this.msgBottomRunnable, 3000L);
        }
    }

    public void showChatEdt()
    {
        this.edtChat.setText(SpanResource.convetToSpan(this.sPublicContent, getContext()));
    }

    protected void chatEnable(boolean bTrue, int chatTip) {
        this.edtChat.setVisibility(bTrue ? View.VISIBLE : View.GONE);
        this.tvSend.setEnabled(bTrue);
        this.lyChat.setEnabled(bTrue);
        this.ivAvatar.setEnabled(bTrue);
        this.tvChatTip.setVisibility(bTrue ? View.GONE : View.VISIBLE);
        chatLyBg();
        if (bTrue) {
            this.edtChat.setText(SpanResource.convetToSpan(this.sPublicContent, getContext()));
            this.edtChat.setSelection(this.edtChat.getText().length());
        } else {
            this.tvChatTip.setText(chatTip);
            this.ivAvatar.setSelected(false);
            emotionPanel(false);
            hideKeyBoard();
        }
    }

    public void setOnQaBottomListener(OnQaBottomListener onQaBottomListener)
    {
        this.onQaBottomListener = onQaBottomListener;
    }

    public void OnResize(int h, int oldh)
    {
        if (h < oldh) {
            if (this.keyBoradStatus != 1) {
                this.keyBoradStatus = 1;
                this.lyChat.setTag(Boolean.valueOf(true));
                chatLyBg();
                keyBoardShow(true);
            }
        }
        else if (this.keyBoradStatus != 0) {
            this.keyBoradStatus = 0;
            this.lyChat.setTag(Boolean.valueOf(false));
            chatLyBg();
            keyBoardShow(false);
        }
    }

    protected void keyBoardShow(boolean bVisible)
    {
    }

    protected void emotionPanel(boolean bVisible)
    {
    }

    public static abstract interface OnPrivateChatBottomListener
    {
        public abstract void sendPrivateMsg(String paramString1, String paramString2);

        public abstract long getToUserId();

        public abstract boolean isLvBottom();
    }

    public static abstract interface OnPublicChatBottomListener
    {
        public abstract void sendPublicMsg(String paramString1, String paramString2);
    }

    public static abstract interface OnQaBottomListener
    {
        public abstract boolean isSelfLvBottom();

        public abstract boolean isLvBottom();

        public abstract void sendQaMsg(String paramString);

        public abstract void querySelfMsg(boolean paramBoolean);
    }
}