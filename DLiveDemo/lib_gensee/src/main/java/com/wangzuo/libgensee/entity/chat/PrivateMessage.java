package com.wangzuo.libgensee.entity.chat;


public class PrivateMessage extends AbsChatMessage
{
    String mReceiveName;

    public String getReceiveName()
    {
        return this.mReceiveName;
    }

    public void setReceiveName(String mReceiveName) {
        this.mReceiveName = mReceiveName;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass()) {
            return false;
        }

        return true;
    }
}
