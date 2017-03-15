package com.wangzuo.libgensee.ui.holder.chat.impl;

import android.os.Handler;
import com.gensee.callback.IChatCallBack;
import com.wangzuo.libgensee.core.RTLive;
import com.wangzuo.libgensee.core.RTLive.OnSysMsgListener;
import com.wangzuo.libgensee.entity.chat.AbsChatMessage;
import com.wangzuo.libgensee.entity.chat.PrivateMessage;
import com.wangzuo.libgensee.entity.chat.PublicMessage;
import com.wangzuo.libgensee.entity.chat.SysMessage;
import com.wangzuo.libgensee.ui.holder.chat.AbsChatImpl;
import com.gensee.routine.UserInfo;
import com.gensee.utils.GenseeLog;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RTChatImpl extends AbsChatImpl implements IChatCallBack, OnSysMsgListener {
    public static final int NEW_DELAY_TIME = 1000;
    private RTChatImpl.OnMsgBottomListener onMsgBottomListener;
    private RTChatImpl.OnChatModeChangeListener onChatModeChangeListener;
    private RTChatImpl.OnChatTopMsgTipListener onTopMsgTipListener;
    private Thread chatMsgThread = null;
    private AtomicBoolean bRunning = new AtomicBoolean(false);
    private List<AbsChatMessage> chatMsgList = new ArrayList();
    List<AbsChatMessage> selfList = new ArrayList();
    private Object object = new Object();
    private Object objChatList = new Object();
    private long nStartTime = 0L;
    private AtomicBoolean bChatEnable = new AtomicBoolean(true);
    private AtomicInteger nChatMode = new AtomicInteger(1);
    private int nSiteChatMode = 101;
    private Handler mHandler;

    public int getSiteChatMode() {
        return this.nSiteChatMode;
    }

    public boolean getChatEnable() {
        return this.bChatEnable.get();
    }

    public void setOnTopMsgTipListener(RTChatImpl.OnChatTopMsgTipListener onTopMsgTipListener) {
        this.onTopMsgTipListener = onTopMsgTipListener;
    }

    public void setOnMsgBottomListener(RTChatImpl.OnMsgBottomListener onMsgBottomListener) {
        this.onMsgBottomListener = onMsgBottomListener;
    }

    public void setOnChatModeChangeListener(RTChatImpl.OnChatModeChangeListener onChatModeChangeListener) {
        this.onChatModeChangeListener = onChatModeChangeListener;
    }

    public RTChatImpl() {
        RTLive.getIns().setOnSysMsgListener(this);
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void onChatJoinConfirm(boolean bRet) {
    }

    public void onChatWithPersion(UserInfo userInfo, String msg, String richText) {
        PrivateMessage message = new PrivateMessage();
        message.setText(msg);
        message.setTime(Calendar.getInstance().getTimeInMillis());
        message.setSendUserId(userInfo.getId());
        message.setRich(richText);
        message.setReceiveUserId(RTLive.getIns().getSelf() == null?0L:RTLive.getIns().getSelf().getId());
        message.setSendUserName(userInfo.getName());
        message.setReceiveName(RTLive.getIns().getSelf() == null?"":RTLive.getIns().getSelf().getName());
        this.updateMessage(message);
        if(this.onTopMsgTipListener != null && userInfo != null) {
            this.onTopMsgTipListener.onPrivateMsg(userInfo.getName());
        }

        if(this.onMsgBottomListener != null && userInfo != null) {
            this.onMsgBottomListener.onPrivateMsg(userInfo);
        }

    }

    public void onChatWithPublic(UserInfo userInfo, String msg, String richText) {
        PublicMessage message = new PublicMessage();
        message.setText(msg);
        message.setRich(richText);
        message.setSendUserId(userInfo.getId());
        message.setTime(Calendar.getInstance().getTimeInMillis());
        message.setSendUserName(userInfo.getName());
        message.setReceiveUserId(-1L);
        this.updateMessage(message);
    }

    public void onChatToPersion(long userId, String msg, String richText) {
        PrivateMessage message = new PrivateMessage();
        message.setText(msg);
        message.setTime(Calendar.getInstance().getTimeInMillis());
        message.setSendUserId(RTLive.getIns().getSelf() == null?0L:RTLive.getIns().getSelf().getId());
        message.setRich(richText);
        message.setReceiveUserId(userId);
        UserInfo toUser = RTLive.getIns().getUserById(userId);
        message.setReceiveName(toUser == null?"":toUser.getName());
        message.setSendUserName(RTLive.getIns().getSelf() == null?"":RTLive.getIns().getSelf().getName());
        this.updateMessage(message);
    }

    public void onChatEnable(boolean enable) {
        this.bChatEnable.set(enable);
        if(this.onChatModeChangeListener != null) {
            this.onChatModeChangeListener.onSelfChatEnable(enable);
        }

    }

    private void updateMessage(AbsChatMessage absChatMessage) {
        this.addMsg(absChatMessage);
        if(this.chatMsgThread == null || !this.bRunning.get()) {
            this.bRunning.set(true);
            this.chatMsgThread = new RTChatImpl.ChatMsgThread();
            this.chatMsgThread.start();
        }

        Object var2 = this.object;
        synchronized(this.object) {
            this.object.notifyAll();
        }
    }

    private void addMsg(AbsChatMessage msg) {
        Object var2 = this.objChatList;
        synchronized(this.objChatList) {
            this.chatMsgList.add(msg);
        }
    }

    private List<AbsChatMessage> getAndClearMsgs() {
        ArrayList msg = null;
        Object var2 = this.objChatList;
        synchronized(this.objChatList) {
            msg = new ArrayList(this.chatMsgList);
            this.chatMsgList.clear();
        }

        return msg == null?new ArrayList():msg;
    }

    private void handleMsg() {
        long nEndTime = Calendar.getInstance().getTimeInMillis();
        long nTimelong = nEndTime - this.nStartTime;
        if(nTimelong >= 1000L) {
            this.nStartTime = nEndTime;
            List msgList = null;
            Object var6 = this.object;
            synchronized(this.object) {
                int nCount = this.chatMsgList.size();
                GenseeLog.d("ChatImpl", "handleMsg chat nCount = " + nCount);
                if(nCount > 0) {
                    msgList = this.getAndClearMsgs();
                } else {
                    try {
                        this.object.wait();
                    } catch (InterruptedException var10) {
                        ;
                    }
                }
            }

            if(msgList != null && msgList.size() > 0) {
                MsgQueue.getIns().addMsgList(msgList);
            }
        } else {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException var9) {
                ;
            }
        }

    }

    public void sendPublicMsg(String text, String rich) {
        if(this.nChatMode.get() == 1) {
            if(this.bChatEnable.get()) {
                RTLive.getIns().chatWithPublic(text, rich);
            } else {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(9000, Integer.valueOf(0)));
                PublicMessage message = new PublicMessage();
                message.setRich(rich);
                message.setText(text);
                message.setSendUserId(RTLive.getIns().getSelf() == null?0L:RTLive.getIns().getSelf().getId());
                message.setTime(Calendar.getInstance().getTimeInMillis());
                message.setSendUserName(RTLive.getIns().getSelf() == null?"":RTLive.getIns().getSelf().getName());
                message.setReceiveUserId(-1L);
                this.updateMessage(message);
            }
        }

    }

    public void onSysMsg(String sSysMsg) {
        SysMessage sysMessage = new SysMessage();
        sysMessage.setTime(Calendar.getInstance().getTimeInMillis());
        sysMessage.setSendUserId(-1L);
        sysMessage.setRich(sSysMsg);
        sysMessage.setText(sSysMsg);
        this.updateMessage(sysMessage);
    }

    public void onBraodcastMsg(String sSysMsg) {
        this.onSysMsg(sSysMsg);
        if(this.onTopMsgTipListener != null) {
            this.onTopMsgTipListener.onBraodcastMsg(sSysMsg);
        }

    }

    public void onChatMode(int nChatMode, String tip) {
    }

    public void onSitePrivateChatMode(int nSiteChatMode) {
        this.nSiteChatMode = nSiteChatMode;
    }

    public int getChatMode() {
        return this.nChatMode.get();
    }

    public void release() {
        if(this.chatMsgList != null) {
            this.chatMsgList.clear();
        }

        if(this.chatMsgThread != null) {
            this.chatMsgThread.interrupt();
        }

        this.bRunning.set(false);
        MsgQueue.getIns().clear();
        MsgQueue.getIns().closedb();
    }

    public void addLocalSystemMsg(String msg) {
        SysMessage message = new SysMessage();
        message.setText(msg);
        message.setRich(msg);
        message.setTime(Calendar.getInstance().getTimeInMillis());
        message.setReceiveUserId(-1L);
        this.updateMessage(message);
    }

    private class ChatMsgThread extends Thread {
        private ChatMsgThread() {
        }

        public void run() {
            while(RTChatImpl.this.bRunning.get()) {
                RTChatImpl.this.handleMsg();
            }

        }
    }

    public interface OnChatModeChangeListener {
        void onChatModeChage(int var1);

        void onSelfChatEnable(boolean var1);
    }

    public interface OnChatTopMsgTipListener {
        void onBraodcastMsg(String var1);

        void onPrivateMsg(String var1);
    }

    public interface OnMsgBottomListener {
        void onChatMode(int var1);

        void onPrivateMsg(UserInfo var1);
    }
}