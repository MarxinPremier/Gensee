package com.wangzuo.libgensee.entity.chat;

public class SysMessage extends AbsChatMessage
{
    private String mStringSendName;
    private String mStringMSG;
    private long mCurrenTime;
    protected long sendUserId;

    public long getSendUserId()
    {
        return this.sendUserId;
    }

    public void setSendUserId(long sendUserId) {
        this.sendUserId = sendUserId;
    }

    public String getmStringSendName() {
        return this.mStringSendName;
    }

    public void setmStringSendName(String mStringSendName) {
        this.mStringSendName = mStringSendName;
    }

    public String getmStringMSG() {
        return this.mStringMSG;
    }

    public void setmStringMSG(String mStringMSG) {
        this.mStringMSG = mStringMSG;
    }

    public long getmCurrenTime() {
        return this.mCurrenTime;
    }

    public void setmCurrenTime(long mCurrenTime) {
        this.mCurrenTime = mCurrenTime;
    }
}
