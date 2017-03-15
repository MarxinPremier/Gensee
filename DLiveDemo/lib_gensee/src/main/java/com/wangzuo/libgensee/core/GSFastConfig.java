package com.wangzuo.libgensee.core;


public class GSFastConfig
{
    private boolean isPublish = false;

    public boolean isPublish() {
        return this.isPublish;
    }

    public GSFastConfig setPublish(boolean isPublish) {
        this.isPublish = isPublish;
        return this;
    }

    public String toString()
    {
        return "GSFastConfig [isPublish=" + this.isPublish + "]";
    }
}
