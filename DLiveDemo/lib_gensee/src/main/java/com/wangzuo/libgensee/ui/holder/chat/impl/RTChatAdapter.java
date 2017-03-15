package com.wangzuo.libgensee.ui.holder.chat.impl;


import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.wangzuo.libgensee.adapter.AbstractAdapter;
import com.wangzuo.libgensee.adapter.AbstractViewHolder;
import com.wangzuo.libgensee.core.RTLive;
import com.wangzuo.libgensee.entity.chat.AbsChatMessage;
import com.wangzuo.libgensee.entity.chat.PrivateMessage;
import com.wangzuo.libgensee.entity.chat.PublicMessage;
import com.wangzuo.libgensee.entity.chat.SysMessage;
import com.wangzuo.libgensee.util.GenseeUtils;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.routine.UserInfo;
import com.gensee.utils.GenseeLog;
import com.gensee.view.MyTextViewEx;

public class RTChatAdapter extends AbstractAdapter {
    public static final String TAG = "RTChatAdapter";
    private Context context;

    public RTChatAdapter() {
    }

    protected View createView(Context context) {
        this.context = context;
        return LayoutInflater.from(context).inflate(ResManager.getLayoutId("gs_public_chat_item"), (ViewGroup)null);
    }

    protected AbstractViewHolder createViewHolder(View view) {
        return new RTChatAdapter.PublicChatViewHolder(view);
    }

    private class PublicChatViewHolder extends AbstractViewHolder {
        private MyTextViewEx tv_msg_content;

        public PublicChatViewHolder(View view) {
            super(view);
        }

        public void initView(View view) {
            this.tv_msg_content = (MyTextViewEx)view.findViewById(ResManager.getId("gs_tv_msg_content"));
        }

        public void initValue(int positon) {
            AbsChatMessage chatMessage = (AbsChatMessage)RTChatAdapter.this.getItem(positon);
            String msgRole = "";
            String msg = chatMessage.getRich();
            String color = "#ffe661";
            if(chatMessage instanceof SysMessage) {
                color = "#fe526e";
                msgRole = RTChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_sys"));
            } else {
                UserInfo rich;
                if(chatMessage instanceof PublicMessage) {
                    msgRole = GenseeUtils.formatText(chatMessage.getSendUserName(), 12);
                    rich = RTLive.getIns().getUserById(chatMessage.getSendUserId());
                    UserInfo sTitle = RTLive.getIns().getSelf();
                    if(rich != null) {
                        if(rich.IsHost()) {
                            color = "#a071fc";
                        }

                        if(sTitle != null && sTitle.getId() == rich.getId()) {
                            msgRole = RTChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_me"));
                        }
                    }
                } else if(chatMessage instanceof PrivateMessage) {
                    rich = RTLive.getIns().getSelf();
                    if(rich != null) {
                        String sTitle1 = "";
                        UserInfo sendUser = RTLive.getIns().getUserById(chatMessage.getSendUserId());
                        if(sendUser != null && sendUser.getId() == rich.getId()) {
                            sTitle1 = RTChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_me")) + " ";
                            sTitle1 = sTitle1 + RTChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_to")) + " ";
                            sTitle1 = sTitle1 + GenseeUtils.formatText(chatMessage.getSendUserName(), 12) + " ";
                            sTitle1 = sTitle1 + RTChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_say")) + " ";
                        } else {
                            sTitle1 = GenseeUtils.formatText(chatMessage.getSendUserName(), 12) + " ";
                            sTitle1 = sTitle1 + RTChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_to")) + " ";
                            sTitle1 = sTitle1 + RTChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_me")) + " ";
                            sTitle1 = sTitle1 + RTChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_say")) + " ";
                        }

                        msgRole = sTitle1;
                    } else {
                        GenseeLog.d("RTChatAdapter", "privatemsg error = " + chatMessage);
                    }
                } else {
                    GenseeLog.d("RTChatAdapter", "privatemsg error = " + chatMessage);
                }
            }

            if(chatMessage instanceof PublicMessage && msg.equals(chatMessage.getText()) && (msg.startsWith("<span>") && msg.endsWith("</span>") || msg.startsWith("<SPAN>") && msg.endsWith("</SPAN>"))) {
                msg = msg.substring(6, msg.length() - 7);
            }

            if(!TextUtils.isEmpty(msgRole)) {
                msgRole = "<font color=\"" + color + "\">" + msgRole + " " + "</font>";
            }

            String rich1 = "<font color=\"white\">" + msg + "</font>";
            this.tv_msg_content.setRichText(msgRole + rich1);
        }
    }
}
