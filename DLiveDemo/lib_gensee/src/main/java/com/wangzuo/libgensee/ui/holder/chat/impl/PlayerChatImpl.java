package com.wangzuo.libgensee.ui.holder.chat.impl;

import android.content.Context;
import android.os.Handler;
import com.gensee.entity.UserInfo;
import com.wangzuo.libgensee.entity.chat.AbsChatMessage;
import com.wangzuo.libgensee.entity.chat.PrivateMessage;
import com.wangzuo.libgensee.entity.chat.PublicMessage;
import com.wangzuo.libgensee.entity.chat.SysMessage;
import com.wangzuo.libgensee.ui.holder.chat.AbsChatImpl;
import com.wangzuo.libgensee.ui.holder.chat.impl.MsgQueue;
import com.wangzuo.libgensee.ui.holder.chat.impl.RTChatImpl.OnChatModeChangeListener;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.player.IPlayerChat;
import com.gensee.player.IPlayerModule;
import com.gensee.player.OnChatListener;
import com.gensee.taskret.OnTaskRet;
import com.gensee.utils.GenseeLog;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerChatImpl extends AbsChatImpl implements OnChatListener {
    public static final int CHAT_MODE_DENY = 0;
    public static final int CHAT_MODE_ALLOW_ALL = 1;
    private static final String TAG = "PlayerChatImpl";
    private AtomicBoolean bChatEnable = new AtomicBoolean(true);
    private AtomicInteger nChatMode = new AtomicInteger(1);
    private Handler mHandler;
    private Thread chatMsgThread;
    private AtomicBoolean bRunning = new AtomicBoolean(false);
    private Object object = new Object();
    private Object objChatList = new Object();
    private List<AbsChatMessage> chatMsgList = new ArrayList();
    private long nStartTime = 0L;
    private IPlayerChat mChatHandle;
    private OnChatModeChangeListener onChatModeChangeListener;
    private Context context;

    public IPlayerChat getmChatHandle() {
        return this.mChatHandle;
    }

    public void setOnChatModeChangeListener(OnChatModeChangeListener onChatModeChangeListener) {
        this.onChatModeChangeListener = onChatModeChangeListener;
    }

    public boolean getChatEnable() {
        return this.bChatEnable.get();
    }

    public int getChatMode() {
        return this.nChatMode.get();
    }

    public long getSelfId() {
        UserInfo self = this.mChatHandle == null?null:this.mChatHandle.getSelfInfo();
        return self != null?self.getUserId():-1L;
    }

    private PlayerChatImpl() {
    }

    public PlayerChatImpl(Context context) {
        this.context = context;
    }

    public void onSysMsg(String sSysMsg) {
        SysMessage sysMessage = new SysMessage();
        sysMessage.setTime(Calendar.getInstance().getTimeInMillis());
        sysMessage.setSendUserId(-1L);
        sysMessage.setRich(sSysMsg);
        sysMessage.setText(sSysMsg);
        this.updateMessage(sysMessage);
    }

    public void onChatWithPerson(long userId, String sSendName, int senderRole, String text, String rich, int onChatID) {
        UserInfo self = this.mChatHandle.getSelfInfo();
        if(self != null) {
            GenseeLog.d("PlayerChatImpl", "OnChatWithPersion userId = " + userId + " sSendName = " + sSendName + " rich = " + rich + " text = " + text);
            PrivateMessage privateMessage = new PrivateMessage();
            privateMessage.setReceiveName(self.getName());
            privateMessage.setReceiveUserId(self.getUserId());
            privateMessage.setRich(rich);
            privateMessage.setText(text);
            privateMessage.setSendUserId(userId);
            privateMessage.setSendUserName(sSendName);
            privateMessage.setSenderRole(senderRole);
            privateMessage.setTime(Calendar.getInstance().getTimeInMillis());
            this.updateMessage(privateMessage);
        } else {
            GenseeLog.d("GSChatView OnChatWithPersion getselfIno is null");
        }

    }

    public void onChatWithPublic(long userId, String sSendName, int senderRole, String text, String rich, int onChatID) {
        UserInfo self = this.mChatHandle.getSelfInfo();
        if(self != null) {
            GenseeLog.d("PlayerChatImpl", "OnChatWithPublic userId = " + userId + " sSendName = " + sSendName + " rich = " + rich + " text = " + text);
            PublicMessage publicMessage = new PublicMessage();
            publicMessage.setRich(rich);
            publicMessage.setText(text);
            publicMessage.setSendUserId(userId);
            publicMessage.setSendUserName(sSendName);
            publicMessage.setTime(Calendar.getInstance().getTimeInMillis());
            publicMessage.setSenderRole(senderRole);
            this.updateMessage(publicMessage);
        } else {
            GenseeLog.d("PlayerChatImpl", "OnChatWithPersion getselfIno is null");
        }

    }

    public void onMute(boolean isMute) {
        this.bChatEnable.set(!isMute);
        if(this.onChatModeChangeListener != null) {
            this.onChatModeChangeListener.onSelfChatEnable(!isMute);
        }

    }

    public void onRoomMute(boolean isMute) {
        int chatMode = isMute?0:1;
        if(this.nChatMode.get() != chatMode) {
            this.nChatMode.set(chatMode);
            String tip = this.context.getResources().getString(ResManager.getStringId("gs_chat_publicchat_open"));
            if(isMute) {
                tip = this.context.getResources().getString(ResManager.getStringId("gs_chat_publicchat_close"));
            } else {
                tip = this.context.getResources().getString(ResManager.getStringId("gs_chat_publicchat_open"));
            }

            this.onSysMsg(tip);
            if(this.onChatModeChangeListener != null) {
                this.onChatModeChangeListener.onChatModeChage(this.nChatMode.get());
            }
        }

    }

    public void onReconnection() {
    }

    public void onPublish(boolean isPlaying) {
    }

    public void setModuleHandle(IPlayerModule chatHandle) {
        this.mChatHandle = (IPlayerChat)chatHandle;
    }

    public void sendPublicMsg(final String content, final String rich) {
        IPlayerChat handle = this.mChatHandle;
        if(handle == null) {
            GenseeLog.d("PlayerChatImpl", "sendPublicMsg fail, handle==null");
        } else {
            UserInfo selfInfo = handle.getSelfInfo();
            if(selfInfo == null) {
                GenseeLog.e("PlayerChatImpl", "sendPublicMsg fail, selfInfo==null");
            } else {
                final long senderId = selfInfo.getUserId();
                final String senderName = selfInfo.getName();
                final int chatId = selfInfo.getChatId();
                final int role = selfInfo.getRole();
                if(this.nChatMode.get() == 1 && this.bChatEnable.get()) {
                    handle.chatToPublic(content, rich, new OnTaskRet() {
                        public void onTaskRet(boolean ret, int id, String desc) {
                            if(ret) {
                                PlayerChatImpl.this.onChatWithPublic(senderId, senderName, role, content, rich, chatId);
                            }

                        }
                    });
                }

            }
        }
    }

    private void updateMessage(AbsChatMessage absChatMessage) {
        this.addMsg(absChatMessage);
        if(this.chatMsgThread == null || !this.bRunning.get()) {
            this.bRunning.set(true);
            this.chatMsgThread = new PlayerChatImpl.ChatMsgThread();
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

    private class ChatMsgThread extends Thread {
        private ChatMsgThread() {
        }

        public void run() {
            while(PlayerChatImpl.this.bRunning.get()) {
                PlayerChatImpl.this.handleMsg();
            }

        }
    }
}
