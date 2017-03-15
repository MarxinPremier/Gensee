package com.wangzuo.libgensee.util;


import android.content.Context;
import android.content.res.Resources;

public class ResManager
{
    private static Resources mRes = null;
    private static String pkgName = null;
    private static ResManager resManager = null;

    public static ResManager getIns() {
        if (resManager == null) {
            synchronized (ResManager.class) {
                if (resManager == null) {
                    resManager = new ResManager();
                }
            }
        }
        return resManager;
    }

    public void init(Context context) {
        if (context != null) {
            mRes = context.getResources();
            pkgName = context.getPackageName();
        }
    }

    public static int getStringId(String name) {
        return mRes.getIdentifier(name, "string", pkgName);
    }

    public static int getDrawableId(String name) {
        return mRes.getIdentifier(name, "drawable", pkgName);
    }

    public static int getId(String name) {
        return mRes.getIdentifier(name, "id", pkgName);
    }

    public static int getDimenId(String name) {
        return mRes.getIdentifier(name, "dimen", pkgName);
    }

    public static int getStyleId(String name) {
        return mRes.getIdentifier(name, "style", pkgName);
    }

    public static int getAnimId(String name) {
        return mRes.getIdentifier(name, "anim", pkgName);
    }

    public static int getLayoutId(String name) {
        return mRes.getIdentifier(name, "layout", pkgName);
    }

    public static int getColorId(String name) {
        return mRes.getIdentifier(name, "color", pkgName);
    }
}