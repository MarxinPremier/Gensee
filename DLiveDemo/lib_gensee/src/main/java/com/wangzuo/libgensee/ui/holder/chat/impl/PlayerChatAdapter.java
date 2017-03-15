package com.wangzuo.libgensee.ui.holder.chat.impl;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.wangzuo.libgensee.adapter.AbstractAdapter;
import com.wangzuo.libgensee.adapter.AbstractViewHolder;
import com.wangzuo.libgensee.entity.chat.AbsChatMessage;
import com.wangzuo.libgensee.entity.chat.PrivateMessage;
import com.wangzuo.libgensee.entity.chat.PublicMessage;
import com.wangzuo.libgensee.entity.chat.SysMessage;
import com.wangzuo.libgensee.ui.WatchActivity;
import com.wangzuo.libgensee.ui.holder.chat.AbsChatImpl;
import com.wangzuo.libgensee.ui.holder.chat.impl.PlayerChatImpl;
import com.wangzuo.libgensee.util.GenseeUtils;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.utils.GenseeLog;
import com.gensee.view.MyTextViewEx;

public class PlayerChatAdapter extends AbstractAdapter {
    private Context context;
    private PlayerChatImpl playerChatImpl;
    private long selfId = 0L;

    public PlayerChatAdapter() {
    }

    protected View createView(Context context) {
        this.context = context;
        AbsChatImpl abs = ((WatchActivity)context).getChatImpl();
        if(abs != null) {
            this.playerChatImpl = (PlayerChatImpl)abs;
            this.selfId = this.playerChatImpl.getSelfId();
        }

        return LayoutInflater.from(context).inflate(ResManager.getLayoutId("gs_public_chat_item"), (ViewGroup)null);
    }

    protected AbstractViewHolder createViewHolder(View view) {
        return new PlayerChatAdapter.PublicChatViewHolder(view);
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
            AbsChatMessage chatMessage = (AbsChatMessage)PlayerChatAdapter.this.getItem(positon);
            String msgRole = "";
            String msg = chatMessage.getRich();
            String color = "#ffe661";
            String rich;
            if(chatMessage instanceof SysMessage) {
                color = "#fe526e";
                msgRole = PlayerChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_sys"));
            } else if(chatMessage instanceof PublicMessage) {
                msgRole = GenseeUtils.formatText(chatMessage.getSendUserName(), 12);
                if(chatMessage.isSendByHost()) {
                    color = "#a071fc";
                }

                if(PlayerChatAdapter.this.selfId == chatMessage.getSendUserId()) {
                    msgRole = PlayerChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_me"));
                }
            } else if(chatMessage instanceof PrivateMessage) {
                rich = "";
                if(chatMessage.getSendUserId() == PlayerChatAdapter.this.selfId) {
                    rich = PlayerChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_me")) + " ";
                    rich = rich + PlayerChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_to")) + " ";
                    rich = rich + GenseeUtils.formatText(chatMessage.getSendUserName(), 12) + " ";
                    rich = rich + PlayerChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_say")) + " ";
                } else {
                    rich = GenseeUtils.formatText(chatMessage.getSendUserName(), 12) + " ";
                    rich = rich + PlayerChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_to")) + " ";
                    rich = rich + PlayerChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_me")) + " ";
                    rich = rich + PlayerChatAdapter.this.context.getResources().getString(ResManager.getStringId("gs_chat_say")) + " ";
                }

                msgRole = rich;
            } else {
                GenseeLog.d("PlayerChatAdapter", "privatemsg error = " + chatMessage);
            }

            if(chatMessage instanceof PublicMessage && msg.equals(chatMessage.getText()) && (msg.startsWith("<span>") && msg.endsWith("</span>") || msg.startsWith("<SPAN>") && msg.endsWith("</SPAN>"))) {
                msg = msg.substring(6, msg.length() - 7);
            }

            if(!TextUtils.isEmpty(msgRole)) {
                msgRole = "<font color=\"" + color + "\">" + msgRole + " " + "</font>";
            }

            rich = "<font color=\"white\">" + msg + "</font>";
            this.tv_msg_content.setRichText(msgRole + rich);
        }
    }
}
