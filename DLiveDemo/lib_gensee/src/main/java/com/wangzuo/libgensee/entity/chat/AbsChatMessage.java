package com.wangzuo.libgensee.entity.chat;


import java.text.SimpleDateFormat;

public abstract class AbsChatMessage
{
    public static final SimpleDateFormat formatter = new SimpleDateFormat(
            "HH:mm");

    public static final SimpleDateFormat formatter1 = new SimpleDateFormat(
            "HH:mm:ss");
    protected String text;
    protected String rich;
    protected long time;
    protected long sendUserId;
    protected long receiveUserId;
    protected String mSendUserName;
    protected int senderRole;
    public static final int ROLE_HOST = 1;
    public static final int ROLE_PRESENT = 2;
    public static final int ROLE_PANELIST = 4;
    public static final int ROLE_ATTENDEE = 8;
    public static final int ROLE_ATTENDEE_WEB = 16;

    public long getReceiveUserId()
    {
        return this.receiveUserId;
    }

    public void setReceiveUserId(long receiveUserId) {
        this.receiveUserId = receiveUserId;
    }
    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRich() {
        return this.rich;
    }

    public void setRich(String rich) {
        this.rich = rich;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getSendUserId() {
        return this.sendUserId;
    }

    public void setSendUserId(long sendUserId) {
        this.sendUserId = sendUserId;
    }

    public String getSendUserName() {
        return this.mSendUserName;
    }

    public void setSendUserName(String mSendUserName) {
        this.mSendUserName = mSendUserName;
    }

    public int getSenderRole() {
        return this.senderRole;
    }

    public void setSenderRole(int senderRole) {
        this.senderRole = senderRole;
    }

    public boolean isSendByHost()
    {
        return (this.senderRole & 0x1) == 1;
    }

    public String toString()
    {
        return "AbsChatMessage [text=" + this.text + ", rich=" + this.rich + ", time=" +
                this.time + ", sendUserId=" + this.sendUserId + ", receiveUserId=" +
                this.receiveUserId + ", mSendUserName=" + this.mSendUserName + "]";
    }

    public static abstract interface IMesssageType
    {
        public static final String PUBLIC_MSG_TYPE = "public";
        public static final String PRIVATE_MSG_TYPE = "private";
        public static final String SYS_MSG_TYPE = "sys";
        public static final String HONGBAO_MSG_TYPE = "hongbao";
    }
}