package com.wangzuo.libgensee.ui.holder;


import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;
import com.wangzuo.libgensee.core.RTLive;
import com.wangzuo.libgensee.ui.PublishActivity;
import com.wangzuo.libgensee.ui.holder.chat.SimpleChatHolder;
import com.wangzuo.libgensee.util.GenseeUtils;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.room.RTRoom;
import com.gensee.room.RtSdk;
import com.gensee.routine.IRoutine;
import com.gensee.routine.UserInfo;
import com.gensee.taskret.OnTaskRet;

public class UserOperateHolder extends BaseHolder
{
    private TextView tv_user_name;
    private TextView tv_user_chat;
    private View tv_user_eject;
    private View user_blank_area;
    private View tv_cancel_btm_view;
    private UserInfo selectUserInfo;
    private SimpleChatHolder simpleChatHolder;
    private View tv_user_chat_line;

    public UserOperateHolder(View rootView, Object value)
    {
        super(rootView, value);
    }

    protected void initData(Object value)
    {
    }

    public void selectUser(UserInfo userInfo)
    {
        if (userInfo == null) {
            return;
        }
        this.tv_user_name.setText(GenseeUtils.formatText(userInfo.getName(), 12));
        this.selectUserInfo = userInfo;
        if ((userInfo.IsPresentor()) || (userInfo.IsPanelist())) {
            this.tv_user_chat.setVisibility(8);
            this.tv_user_chat_line.setVisibility(8);
        } else {
            this.tv_user_chat.setVisibility(0);
            this.tv_user_chat_line.setVisibility(0);
        }
        if (this.selectUserInfo.IsChatMute())
            this.tv_user_chat.setText(getContext().getResources().getString(ResManager.getStringId("gs_user_enable_chat")));
        else {
            this.tv_user_chat.setText(getContext().getResources().getString(ResManager.getStringId("gs_user_disable_chat")));
        }
        show(true);
    }

    protected void initComp(Object value)
    {
        this.tv_user_name = ((TextView)findViewById(ResManager.getId("gs_tv_user_name")));
        this.tv_user_chat = ((TextView)findViewById(ResManager.getId("gs_tv_user_chat")));
        this.tv_user_chat_line = findViewById(ResManager.getId("gs_tv_user_chat_line"));
        this.tv_user_chat.setOnClickListener(this);
        this.tv_user_eject = findViewById(ResManager.getId("gs_tv_user_eject"));
        this.tv_user_eject.setOnClickListener(this);

        this.user_blank_area = findViewById(ResManager.getId("gs_user_blank_area"));
        this.user_blank_area.setOnClickListener(this);
        this.tv_cancel_btm_view = findViewById(ResManager.getId("gs_tv_cancel_btm_view"));
        this.tv_cancel_btm_view.setOnClickListener(this);
    }

    public void onClick(View v)
    {
        this.simpleChatHolder = ((PublishActivity)getContext()).getSimpleChatHolder();
        if (v.getId() == ResManager.getId("gs_tv_user_chat")) {
            if (this.selectUserInfo.IsChatMute())
                chatControl(true);
            else {
                ((PublishActivity)getContext()).showDialog("",
                        getString(ResManager.getStringId("gs_disable_somebody_chat")),
                        getString(ResManager.getStringId("gs_cancel")),
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        }
                        , getString(ResManager.getStringId("gs_sure_1")),
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                                UserOperateHolder.this.chatControl(false);
                            }
                        }
                        , null);
            }
            show(false);
        } else if (v.getId() == ResManager.getId("gs_tv_user_eject")) {
            ((PublishActivity)getContext()).showDialog("",
                    getString(ResManager.getStringId("gs_remove_somebody")),
                    getString(ResManager.getStringId("gs_cancel")),
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    }
                    , getString(ResManager.getStringId("gs_sure_1")),
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                            UserOperateHolder.this.ejectControl();
                        }
                    }
                    , null);
            show(false);
        } else if ((v.getId() == ResManager.getId("gs_user_blank_area")) || (v.getId() == ResManager.getId("gs_tv_cancel_btm_view"))) {
            show(false);
        }
    }

    private void ejectControl() {
        RTLive.getIns().getRtSdk().roomEjectUser(this.selectUserInfo.getId(), false, new OnTaskRet()
        {
            public void onTaskRet(boolean b, int i, String s) {
                if ((b) && (UserOperateHolder.this.simpleChatHolder != null)) {
                    String msg = GenseeUtils.formatText(UserOperateHolder.this.selectUserInfo.getName(), 12) + UserOperateHolder.this.getString(ResManager.getStringId("gs_chat_is_kicked_out"));
                    RTLive.getIns().getRtSdk().roomNotifyBroadcastMsg(msg, true, null);
                }
            }
        });
    }

    private void chatControl(boolean flag)
    {
        RTRoom.getIns().getRoutine().chatEnable(this.selectUserInfo.getId(),
                flag, new OnTaskRet()
                {
                    public void onTaskRet(boolean b, int i, String s) {
                        if ((b) && (UserOperateHolder.this.simpleChatHolder != null)) {
                            String msg = "";
                            if (UserOperateHolder.this.selectUserInfo.IsChatMute())
                                msg = GenseeUtils.formatText(UserOperateHolder.this.selectUserInfo.getName(), 12) + UserOperateHolder.this.getString(ResManager.getStringId("gs_chat_is_alowed_to_chat"));
                            else {
                                msg = GenseeUtils.formatText(UserOperateHolder.this.selectUserInfo.getName(), 12) + UserOperateHolder.this.getString(ResManager.getStringId("gs_chat_is_disable_to_chat"));
                            }
                            RTLive.getIns().getRtSdk().roomNotifyBroadcastMsg(msg, true, null);
                        }
                    }
                });
    }
}
