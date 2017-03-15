package com.wangzuo.libgensee.ui.holder.chat.impl;

import com.wangzuo.libgensee.db.PlayerChatDataBaseManager;
import com.wangzuo.libgensee.entity.chat.AbsChatMessage;
import com.wangzuo.libgensee.entity.chat.PrivateMessage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MsgQueue {
    protected ReentrantReadWriteLock mLock = new ReentrantReadWriteLock();
    private PlayerChatDataBaseManager dataBaseManager;
    public static final int READ_PER_COUNT = 200;
    private static MsgQueue msgManager = null;
    private static final int QUEUE_MAX_LENGH = 200;
    private List<AbsChatMessage> msgList = new ArrayList();
    private List<AbsChatMessage> privateMsgList = new ArrayList();
    private List<AbsChatMessage> selfMsgList = new ArrayList();
    private boolean bPublicLatest = true;
    private boolean bPrivateLatest = true;
    private boolean bSelfLatest = true;
    private MsgQueue.OnPublicChatHolderListener onPublicChatHolderListener;
    private MsgQueue.OnPrivateChatHolderListener onPrivateChatHolderListener;
    private long toUserId = -1L;
    private long selfUserId = -1L;

    public static MsgQueue getIns() {
        Class var0 = MsgQueue.class;
        synchronized(MsgQueue.class) {
            if(msgManager == null) {
                msgManager = new MsgQueue();
            }
        }

        return msgManager;
    }

    public MsgQueue() {
    }

    public void initMsgDbHelper(PlayerChatDataBaseManager playerDataBaseManager) {
        this.dataBaseManager = playerDataBaseManager;
    }

    public void addMsgList(List<AbsChatMessage> dataList) {
        this.mLock.writeLock().lock();

        try {
            if(this.toUserId > 0L || this.selfUserId > 0L) {
                ArrayList nSize = new ArrayList();
                ArrayList bBottom = new ArrayList();
                Iterator otherSize = dataList.iterator();

                label235:
                while(true) {
                    AbsChatMessage nTotalSize;
                    do {
                        do {
                            if(!otherSize.hasNext()) {
                                if(nSize.size() > 0) {
                                    this.processToUserList(nSize);
                                }

                                if(bBottom.size() > 0) {
                                    this.processSelfList(bBottom);
                                }
                                break label235;
                            }

                            nTotalSize = (AbsChatMessage)otherSize.next();
                            if(this.toUserId > 0L && nTotalSize instanceof PrivateMessage && (nTotalSize.getSendUserId() == this.toUserId || nTotalSize.getReceiveUserId() == this.toUserId)) {
                                nSize.add(nTotalSize);
                            }
                        } while(this.selfUserId <= 0L);
                    } while(nTotalSize.getSendUserId() != this.selfUserId && nTotalSize.getReceiveUserId() != this.selfUserId);

                    bBottom.add(nTotalSize);
                }
            }

            int var11 = dataList.size();
            boolean var12 = this.onPublicChatHolderListener == null?false:this.onPublicChatHolderListener.isLvBottom();
            if(this.bPublicLatest) {
                int var13;
                int var14;
                if(var11 >= 200) {
                    if(var12) {
                        var13 = var11 - 200;
                        this.msgList.clear();

                        for(var14 = var13; var14 < var11; ++var14) {
                            this.msgList.add((AbsChatMessage)dataList.get(var14));
                        }
                    } else {
                        var13 = 200 - this.msgList.size();

                        for(var14 = 0; var14 < var13; ++var14) {
                            this.msgList.add((AbsChatMessage)dataList.get(var14));
                        }

                        this.bPublicLatest = false;
                    }
                } else {
                    var13 = var11 + this.msgList.size();
                    if(var13 < 200) {
                        this.msgList.addAll(dataList);
                    } else if(var12) {
                        var14 = var13 - 200;
                        ArrayList i = new ArrayList();

                        for(int i1 = 0; i1 < var14; ++i1) {
                            i.add((AbsChatMessage)this.msgList.get(i1));
                        }

                        this.msgList.removeAll(i);
                        this.msgList.addAll(dataList);
                    } else {
                        var14 = 200 - this.msgList.size();

                        for(int var15 = 0; var15 < var14; ++var15) {
                            this.msgList.add((AbsChatMessage)dataList.get(var15));
                        }

                        this.bPublicLatest = false;
                    }
                }
            }

            this.dataBaseManager.insertValues(dataList);
            this.refreshMsg();
        } finally {
            this.mLock.writeLock().unlock();
        }

    }

    private void processToUserList(List<AbsChatMessage> toUserList) {
        int nSize = toUserList.size();
        boolean bBottom = this.onPrivateChatHolderListener == null?false:this.onPrivateChatHolderListener.isLvBottom();
        if(this.bPrivateLatest) {
            int nTotalSize;
            int otherSize;
            if(nSize >= 200) {
                if(bBottom) {
                    nTotalSize = nSize - 200;
                    this.privateMsgList.clear();

                    for(otherSize = nTotalSize; otherSize < nSize; ++otherSize) {
                        this.privateMsgList.add((AbsChatMessage)toUserList.get(otherSize));
                    }
                } else {
                    nTotalSize = 200 - this.privateMsgList.size();

                    for(otherSize = 0; otherSize < nTotalSize; ++otherSize) {
                        this.privateMsgList.add((AbsChatMessage)toUserList.get(otherSize));
                    }

                    this.bPrivateLatest = false;
                }
            } else {
                nTotalSize = nSize + this.privateMsgList.size();
                if(nTotalSize <= 200) {
                    this.privateMsgList.addAll(toUserList);
                } else if(bBottom) {
                    otherSize = nTotalSize - 200;
                    ArrayList i = new ArrayList();

                    for(int i1 = 0; i1 < otherSize; ++i1) {
                        i.add((AbsChatMessage)this.privateMsgList.get(i1));
                    }

                    this.privateMsgList.removeAll(i);
                    this.privateMsgList.addAll(toUserList);
                } else {
                    otherSize = 200 - this.privateMsgList.size();

                    for(int var8 = 0; var8 < otherSize; ++var8) {
                        this.privateMsgList.add((AbsChatMessage)toUserList.get(var8));
                    }

                    this.bPrivateLatest = false;
                }
            }
        }

        if(this.onPrivateChatHolderListener != null) {
            this.onPrivateChatHolderListener.refreshMsg(this.privateMsgList, this.bPrivateLatest, this.toUserId);
        }

    }

    private void processSelfList(List<AbsChatMessage> selfUserList) {
        int nSize = selfUserList.size();
        boolean bBottom = this.onPublicChatHolderListener == null?false:this.onPublicChatHolderListener.isSelfLvBottom();
        if(this.bSelfLatest) {
            int nTotalSize;
            int otherSize;
            if(nSize >= 200) {
                if(bBottom) {
                    nTotalSize = nSize - 200;
                    this.selfMsgList.clear();

                    for(otherSize = nTotalSize; otherSize < nSize; ++otherSize) {
                        this.selfMsgList.add((AbsChatMessage)selfUserList.get(otherSize));
                    }
                } else {
                    nTotalSize = 200 - this.selfMsgList.size();

                    for(otherSize = 0; otherSize < nTotalSize; ++otherSize) {
                        this.selfMsgList.add((AbsChatMessage)selfUserList.get(otherSize));
                    }

                    this.bSelfLatest = false;
                }
            } else {
                nTotalSize = nSize + this.selfMsgList.size();
                if(nTotalSize <= 200) {
                    this.selfMsgList.addAll(selfUserList);
                } else if(bBottom) {
                    otherSize = nTotalSize - 200;
                    ArrayList i = new ArrayList();

                    for(int i1 = 0; i1 < otherSize; ++i1) {
                        i.add((AbsChatMessage)this.selfMsgList.get(i1));
                    }

                    this.selfMsgList.removeAll(i);
                    this.selfMsgList.addAll(selfUserList);
                } else {
                    otherSize = 200 - this.selfMsgList.size();

                    for(int var8 = 0; var8 < otherSize; ++var8) {
                        this.selfMsgList.add((AbsChatMessage)selfUserList.get(var8));
                    }

                    this.bSelfLatest = false;
                }
            }
        }

        if(this.onPublicChatHolderListener != null && this.selfUserId > 0L) {
            this.onPublicChatHolderListener.refreshSelfMsg(this.selfMsgList, this.bSelfLatest);
        }

    }

    public AbsChatMessage getLatestMsg() {
        AbsChatMessage absChatMessage = null;
        this.mLock.readLock().lock();

        try {
            absChatMessage = this.dataBaseManager.getLatestMsg();
        } finally {
            this.mLock.readLock().unlock();
        }

        return absChatMessage;
    }

    public void deleteMsg(AbsChatMessage msg) {
        this.mLock.writeLock().lock();

        try {
            int nSize = this.msgList.size();
            int i = 0;

            while(true) {
                if(i < nSize) {
                    AbsChatMessage tmpMsg = (AbsChatMessage)this.msgList.get(i);
                    if(tmpMsg.getTime() != msg.getTime()) {
                        ++i;
                        continue;
                    }

                    this.msgList.remove(i);
                }

                this.dataBaseManager.removeChatMsgByUUID(String.valueOf(msg.getTime()));
                return;
            }
        } finally {
            this.mLock.writeLock().unlock();
        }
    }

    public void getLatestMsgsList() {
        this.mLock.writeLock().lock();

        try {
            this.msgList.clear();
            this.msgList.addAll(this.dataBaseManager.getLatestMsgsList(200));
            this.bPublicLatest = true;
            this.refreshMsg();
        } finally {
            this.mLock.writeLock().unlock();
        }

    }

    public List<AbsChatMessage> getAllMsgsListNext(long timeMillions) {
        List returnList = null;
        this.mLock.readLock().lock();

        try {
            returnList = this.dataBaseManager.queryChatMsgsLimitNext(200, timeMillions);
        } finally {
            this.mLock.readLock().unlock();
        }

        return returnList;
    }

    public List<AbsChatMessage> getAllMsgsListPre(long timeMillions) {
        List returnList = null;
        this.mLock.readLock().lock();

        try {
            returnList = this.dataBaseManager.queryChatMsgsLimitPre(200, timeMillions);
        } finally {
            this.mLock.readLock().unlock();
        }

        return returnList;
    }

    private void refreshMsg() {
        if(this.onPublicChatHolderListener != null) {
            this.onPublicChatHolderListener.refreshMsg(new ArrayList(this.msgList), this.bPublicLatest);
        }

    }

    public void getMsgList() {
        this.mLock.writeLock().lock();

        try {
            this.refreshMsg();
        } finally {
            this.mLock.writeLock().unlock();
        }

    }

    public void onMessageFresh() {
        this.mLock.writeLock().lock();

        try {
            int nMsgSize = this.msgList.size();
            if(nMsgSize > 0) {
                AbsChatMessage firstMsg = (AbsChatMessage)this.msgList.get(0);
                List tmpList = getIns().getAllMsgsListPre(firstMsg.getTime());
                int tempSize = tmpList.size();
                if(nMsgSize + tempSize > 200) {
                    int nDeleteCount = nMsgSize + tempSize - 200;
                    ArrayList deleteList = new ArrayList();

                    for(int i = nMsgSize - 1; i >= nMsgSize - nDeleteCount; --i) {
                        deleteList.add((AbsChatMessage)this.msgList.get(i));
                    }

                    this.msgList.removeAll(deleteList);
                    deleteList.clear();
                    deleteList = null;
                    this.bPublicLatest = false;
                }

                this.msgList.addAll(0, tmpList);
                tmpList = null;
            }

            if(this.onPublicChatHolderListener != null) {
                this.onPublicChatHolderListener.onPullMsg(this.msgList, this.bPublicLatest);
            }
        } finally {
            this.mLock.writeLock().unlock();
        }

    }

    public void onMessageLoadMore() {
        this.mLock.writeLock().lock();

        try {
            int nMsgSize = this.msgList.size();
            if(nMsgSize > 0) {
                AbsChatMessage lastMsg = (AbsChatMessage)this.msgList.get(nMsgSize - 1);
                List tmpList = getIns().getAllMsgsListNext(lastMsg.getTime());
                int tempSize = tmpList.size();
                AbsChatMessage deleteList;
                if(tempSize < 200) {
                    this.bPublicLatest = true;
                } else if(tempSize == 200) {
                    boolean nDeleteCount = false;
                    deleteList = (AbsChatMessage)tmpList.get(tempSize - 1);
                    AbsChatMessage i = getIns().getLatestMsg();
                    if(i == null) {
                        nDeleteCount = true;
                    } else {
                        nDeleteCount = deleteList.getTime() == i.getTime();
                    }

                    this.bPublicLatest = nDeleteCount;
                }

                if(nMsgSize + tempSize > 200) {
                    int var11 = nMsgSize + tempSize - 200;
                    ArrayList var12 = new ArrayList();

                    for(int var13 = 0; var13 < var11; ++var13) {
                        var12.add((AbsChatMessage)this.msgList.get(var13));
                    }

                    this.msgList.removeAll(var12);
                    var12.clear();
                    deleteList = null;
                }

                this.msgList.addAll(tmpList);
            }

            if(this.onPublicChatHolderListener != null) {
                this.onPublicChatHolderListener.onLoadMsg(this.msgList, this.bPublicLatest);
            }
        } finally {
            this.mLock.writeLock().unlock();
        }

    }

    public List<AbsChatMessage> getMsgsByOwnerId(long msgOwnerId) {
        List returnList = null;
        this.mLock.readLock().lock();

        try {
            returnList = this.dataBaseManager.getMsgsByOwnerId(msgOwnerId);
        } finally {
            this.mLock.readLock().unlock();
        }

        return returnList;
    }

    public void clear() {
        this.mLock.writeLock().lock();

        try {
            this.msgList.clear();
            this.selfMsgList.clear();
            this.privateMsgList.clear();
            this.bPublicLatest = true;
            this.bPrivateLatest = true;
            this.bSelfLatest = true;
        } finally {
            this.mLock.writeLock().unlock();
        }

    }

    public void closedb() {
        this.mLock.writeLock().lock();

        try {
            this.dataBaseManager.closeDb();
        } finally {
            this.mLock.writeLock().unlock();
        }

    }

    public void getSelfLatestMsg(long selfUserId) {
        this.mLock.writeLock().lock();

        try {
            this.selfUserId = selfUserId;
            this.selfMsgList.clear();
            this.selfMsgList.addAll(this.dataBaseManager.getLatestMsgsByOwnerId(200, selfUserId));
            this.bSelfLatest = true;
            this.refreshSelfMsg();
        } finally {
            this.mLock.writeLock().unlock();
        }

    }

    private void refreshSelfMsg() {
        if(this.onPublicChatHolderListener != null && this.selfUserId > 0L) {
            this.onPublicChatHolderListener.refreshSelfMsg(this.selfMsgList, this.bSelfLatest);
        }

    }

    public void onSelfMessageFresh(long selfId) {
        this.mLock.writeLock().lock();

        try {
            int nMsgSize = this.selfMsgList.size();
            if(nMsgSize > 0) {
                AbsChatMessage firstMsg = (AbsChatMessage)this.selfMsgList.get(0);
                List tmpList = this.dataBaseManager.queryChatMsgsByOwnerIdLimitPre(selfId, 200, firstMsg.getTime());
                int tempSize = tmpList.size();
                if(nMsgSize + tempSize > 200) {
                    int nDeleteCount = nMsgSize + tempSize - 200;
                    ArrayList deleteList = new ArrayList();

                    for(int i = nMsgSize - 1; i >= nMsgSize - nDeleteCount; --i) {
                        deleteList.add((AbsChatMessage)this.selfMsgList.get(i));
                    }

                    this.selfMsgList.removeAll(deleteList);
                    deleteList.clear();
                    deleteList = null;
                    this.bSelfLatest = false;
                }

                this.selfMsgList.addAll(0, tmpList);
                tmpList = null;
            }

            if(this.onPublicChatHolderListener != null && this.selfUserId > 0L) {
                this.onPublicChatHolderListener.onPullSelfMsg(this.selfMsgList, this.bSelfLatest);
            }
        } finally {
            this.mLock.writeLock().unlock();
        }

    }

    public void onSelfMessageLoadMore(long selfId) {
        this.mLock.writeLock().lock();

        try {
            int nMsgSize = this.selfMsgList.size();
            if(nMsgSize > 0) {
                AbsChatMessage lastMsg = (AbsChatMessage)this.selfMsgList.get(nMsgSize - 1);
                List tmpList = this.dataBaseManager.queryChatMsgsByOwnerIdLimitNext(selfId, 200, lastMsg.getTime());
                int tempSize = tmpList.size();
                AbsChatMessage deleteList;
                if(tempSize < 200) {
                    this.bSelfLatest = true;
                } else if(tempSize == 200) {
                    boolean nDeleteCount = false;
                    deleteList = (AbsChatMessage)tmpList.get(tempSize - 1);
                    AbsChatMessage i = getIns().getLatestMsg();
                    if(i == null) {
                        nDeleteCount = true;
                    } else {
                        nDeleteCount = deleteList.getTime() == i.getTime();
                    }

                    this.bSelfLatest = nDeleteCount;
                }

                if(nMsgSize + tempSize > 200) {
                    int var13 = nMsgSize + tempSize - 200;
                    ArrayList var14 = new ArrayList();

                    for(int var15 = 0; var15 < var13; ++var15) {
                        var14.add((AbsChatMessage)this.selfMsgList.get(var15));
                    }

                    this.selfMsgList.removeAll(var14);
                    var14.clear();
                    deleteList = null;
                }

                this.selfMsgList.addAll(tmpList);
            }

            if(this.onPublicChatHolderListener != null && this.selfUserId > 0L) {
                this.onPublicChatHolderListener.onLoadSelfMsg(this.selfMsgList, this.bSelfLatest);
            }
        } finally {
            this.mLock.writeLock().unlock();
        }

    }

    public void getPrivateLatestMsg(long selfId, long toUserId) {
        this.mLock.writeLock().lock();

        try {
            this.toUserId = toUserId;
            this.privateMsgList.clear();
            this.privateMsgList.addAll(this.dataBaseManager.getPrivateLatestMsgsByOwnerId(200, toUserId, selfId));
            this.bPrivateLatest = true;
            this.refreshPrivateMsg(toUserId);
        } finally {
            this.mLock.writeLock().unlock();
        }

    }

    private void refreshPrivateMsg(long ownerId) {
        if(this.onPrivateChatHolderListener != null && this.toUserId > 0L && this.toUserId == ownerId) {
            this.onPrivateChatHolderListener.refreshMsg(this.privateMsgList, this.bPrivateLatest, ownerId);
        }

    }

    public void onPrivateMessageFresh(long selfId, long toUserId) {
        this.mLock.writeLock().lock();

        try {
            int nMsgSize = this.privateMsgList.size();
            if(nMsgSize > 0) {
                AbsChatMessage firstMsg = (AbsChatMessage)this.privateMsgList.get(0);
                List tmpList = this.dataBaseManager.queryPrivateChatMsgsByOwnerIdLimitPre(selfId, toUserId, 200, firstMsg.getTime());
                int tempSize = tmpList.size();
                if(nMsgSize + tempSize > 200) {
                    int nDeleteCount = nMsgSize + tempSize - 200;
                    ArrayList deleteList = new ArrayList();

                    for(int i = nMsgSize - 1; i >= nMsgSize - nDeleteCount; --i) {
                        deleteList.add((AbsChatMessage)this.privateMsgList.get(i));
                    }

                    this.privateMsgList.removeAll(deleteList);
                    deleteList.clear();
                    deleteList = null;
                    this.bPrivateLatest = false;
                }

                this.privateMsgList.addAll(0, tmpList);
                tmpList = null;
            }

            if(this.onPrivateChatHolderListener != null && this.toUserId > 0L && this.toUserId == toUserId) {
                this.onPrivateChatHolderListener.onPullMsg(this.privateMsgList, this.bPrivateLatest, toUserId);
            }
        } finally {
            this.mLock.writeLock().unlock();
        }

    }

    public void onPrivateMessageLoadMore(long selfId, long ownerId) {
        this.mLock.writeLock().lock();

        try {
            int nMsgSize = this.privateMsgList.size();
            if(nMsgSize > 0) {
                AbsChatMessage lastMsg = (AbsChatMessage)this.privateMsgList.get(nMsgSize - 1);
                List tmpList = this.dataBaseManager.queryPrivateChatMsgsByOwnerIdLimitNext(selfId, ownerId, 200, lastMsg.getTime());
                int tempSize = tmpList.size();
                AbsChatMessage deleteList;
                if(tempSize < 200) {
                    this.bPrivateLatest = true;
                } else if(tempSize == 200) {
                    boolean nDeleteCount = false;
                    deleteList = (AbsChatMessage)tmpList.get(tempSize - 1);
                    AbsChatMessage i = getIns().getLatestMsg();
                    if(i == null) {
                        nDeleteCount = true;
                    } else {
                        nDeleteCount = deleteList.getTime() == i.getTime();
                    }

                    this.bPrivateLatest = nDeleteCount;
                }

                if(nMsgSize + tempSize > 200) {
                    int var15 = nMsgSize + tempSize - 200;
                    ArrayList var16 = new ArrayList();

                    for(int var17 = 0; var17 < var15; ++var17) {
                        var16.add((AbsChatMessage)this.privateMsgList.get(var17));
                    }

                    this.privateMsgList.removeAll(var16);
                    var16.clear();
                    deleteList = null;
                }

                this.privateMsgList.addAll(tmpList);
            }

            if(this.onPrivateChatHolderListener != null && this.toUserId > 0L && this.toUserId == ownerId) {
                this.onPrivateChatHolderListener.onLoadMsg(this.privateMsgList, this.bPrivateLatest, ownerId);
            }
        } finally {
            this.mLock.writeLock().unlock();
        }

    }

    public void resetSelfList() {
        this.selfUserId = -1L;
    }

    public void resetToUserList() {
        this.toUserId = -1L;
    }

    public void setOnPublicChatHolderListener(MsgQueue.OnPublicChatHolderListener onPublicChatHolderListener) {
        this.onPublicChatHolderListener = onPublicChatHolderListener;
    }

    public void setOnPrivateChatHolderListener(MsgQueue.OnPrivateChatHolderListener onPrivateChatHolderListener) {
        this.onPrivateChatHolderListener = onPrivateChatHolderListener;
    }

    public boolean isPublicLatest() {
        this.mLock.readLock().lock();

        boolean var2;
        try {
            var2 = this.bPublicLatest;
        } finally {
            this.mLock.readLock().unlock();
        }

        return var2;
    }

    public boolean isSelfLatest() {
        this.mLock.readLock().lock();

        boolean var2;
        try {
            var2 = this.bSelfLatest;
        } finally {
            this.mLock.readLock().unlock();
        }

        return var2;
    }

    public boolean isPrivateLatest() {
        this.mLock.readLock().lock();

        boolean var2;
        try {
            var2 = this.bPrivateLatest;
        } finally {
            this.mLock.readLock().unlock();
        }

        return var2;
    }

    public long getSelfUserId() {
        this.mLock.readLock().lock();

        long var2;
        try {
            var2 = this.selfUserId;
        } finally {
            this.mLock.readLock().unlock();
        }

        return var2;
    }

    public interface OnPrivateChatHolderListener {
        boolean isLvBottom();

        void refreshMsg(List<AbsChatMessage> var1, boolean var2, long var3);

        void onPullMsg(List<AbsChatMessage> var1, boolean var2, long var3);

        void onLoadMsg(List<AbsChatMessage> var1, boolean var2, long var3);
    }

    public interface OnPublicChatHolderListener {
        boolean isLvBottom();

        boolean isSelfLvBottom();

        void refreshSelfMsg(List<AbsChatMessage> var1, boolean var2);

        void onPullSelfMsg(List<AbsChatMessage> var1, boolean var2);

        void onLoadSelfMsg(List<AbsChatMessage> var1, boolean var2);

        void refreshMsg(List<AbsChatMessage> var1, boolean var2);

        void onPullMsg(List<AbsChatMessage> var1, boolean var2);

        void onLoadMsg(List<AbsChatMessage> var1, boolean var2);
    }
}
