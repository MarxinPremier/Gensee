package com.wangzuo.libgensee.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferUtil
{
    private static final String PREF_NAME = "gensee_fast_sdk";
    private static PreferUtil fastsdkSharePreferences;
    private SharedPreferences preferences;
    public static final String FIRST_VIDEO_UP = "gensee.fastsdk.video.up.first";
    public static final String FIRST_DOC_HOR = "gensee.fastsdk.video.hor.first";
    public static final String FIRST_DOC_DOWN = "gensee.fastsdk.video.down.first";
    public static final String SHARE_ADDR = "gensee.fastsdk.shareaddr";
    public static final String DOMAIN_SAVE = "fastsdk.domain";
    public static final String NUMBER_SAVE = "fastsdk.number";
    public static final String SUBJECT_SAVE = "fastsdk.subject";
    public static final String JOIN_PARAM = "fastsdk.join.param";
    public static final String PUB_VIDEO_MODE = "fastsdk.pub.video.mode";
    public static final String PUB_VIDEO_QUALITY = "fastsdk.pub.video.quality";
    public static final String PUB_VIDEO_ENCODE_TYPE = "fastsdk.pub.video.encode.type";
    public static final String PUB_VIDEO_DECODE_TYPE = "fastsdk.pub.video.decode.type";
    public static final String ORIENTATION_PORTRAIT_UNCROP = "orientation.portrait.uncrop";
    public static final int VIDEO_HD = 1;
    public static final int VIDEO_SD = 2;
    public static final String FIRST_DOC_SCROLL = "FIRST_DOC_SCROLL";
    public static final String FIRST_GET_HONGBAO = "FIRST_GET_HONGBAO";
    public static final String MIC_STATUS = "MIC_STATUS";
    public static final String CAMERA_STATUS = "CAMERA_STATUS";
    public static final String VIDEO_SELF_ACTIVED = "VIDEO_SELF_ACTIVED";
    public static final String HAND_STATUS = "HAND_STATUS";
    public static final String KEY_FIRST_HARD_CODE_GUIDE = "first.hard.code.guide";
    public static final String KEY_IS_HARD_ENCODE_FOR_PURE_VIDEO = "is.hard.encode.for.pure.video";
    public static final String KEY_BEAUTY_STATUS = "is.beauty.open";

    private PreferUtil(Context context)
    {
        this.preferences = context.getSharedPreferences("gensee_fast_sdk",
                0);
    }

    public static void initPref(Context context) {
        synchronized (PreferUtil.class) {
            if (fastsdkSharePreferences == null)
                fastsdkSharePreferences = new PreferUtil(context);
        }
    }

    public static PreferUtil getIns()
    {
        return fastsdkSharePreferences;
    }

    public synchronized boolean putFirstVideoUp(boolean bVideoUp) {
        return this.preferences.edit().putBoolean("gensee.fastsdk.video.up.first", bVideoUp).commit();
    }

    public synchronized boolean getFirstVideoUp() {
        return this.preferences.getBoolean("gensee.fastsdk.video.up.first", true);
    }

    public synchronized boolean putFirstDocHor(boolean bDocHor) {
        return this.preferences.edit().putBoolean("gensee.fastsdk.video.hor.first", bDocHor).commit();
    }

    public synchronized boolean getFirstDocHor() {
        return this.preferences.getBoolean("gensee.fastsdk.video.hor.first", true);
    }

    public synchronized boolean putFirstDocDown(boolean bDocDown) {
        return this.preferences.edit().putBoolean("gensee.fastsdk.video.down.first", bDocDown).commit();
    }

    public synchronized boolean getFirstDocDown() {
        return this.preferences.getBoolean("gensee.fastsdk.video.down.first", true);
    }

    public String getShareAddr() {
        return this.preferences.getString("gensee.fastsdk.shareaddr", "");
    }

    public boolean putShareAddr(String shareAddr) {
        return this.preferences.edit().putString("gensee.fastsdk.shareaddr", shareAddr).commit();
    }

    public boolean putDomainSave(String sDomain) {
        return this.preferences.edit().putString("fastsdk.domain", sDomain).commit();
    }

    public boolean putNumberSave(String sNumber) {
        return this.preferences.edit().putString("fastsdk.number", sNumber).commit();
    }

    public String getDomainSave() {
        return this.preferences.getString("fastsdk.domain", "");
    }

    public String getNumberSave() {
        return this.preferences.getString("fastsdk.number", "");
    }

    public void putSubject(String subject) {
        this.preferences.edit().putString("fastsdk.subject", subject).commit();
    }

    public String getSubject() {
        return this.preferences.getString("fastsdk.subject", "");
    }

    public SharedPreferences.Editor edit()
    {
        return this.preferences.edit();
    }

    public int getInt(String key, int def) {
        return this.preferences.getInt(key, def);
    }

    public boolean isFirstHardCodeGuide() {
        return this.preferences.getBoolean("first.hard.code.guide", true);
    }

    public void setNotFirstHardCodeGuide() {
        this.preferences.edit().putBoolean("first.hard.code.guide", false).commit();
    }

    public void putInt(String key, int value) {
        this.preferences.edit().putInt(key, value).commit();
    }

    public int getInt(String key) {
        return this.preferences.getInt(key, -1);
    }

    public void putBoolean(String key, boolean bool) {
        this.preferences.edit().putBoolean(key, bool).commit();
    }

    public boolean getBoolean(String key) {
        return this.preferences.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return this.preferences.getBoolean(key, defValue);
    }
}
