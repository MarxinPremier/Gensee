package com.wangzuo.libgensee.entity;


import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class JoinParams
        implements Serializable
{
    public static final String KEY_NAME = "name";
    public static final String KEY_ACCOUNT = "account";
    public static final String KEY_PSW = "password";
    public static final String KEY_LOGIN_PSW = "login.password";
    public static final String KEY_LIVE_ID = "live.id";
    public static final String KEY_PUB_MODE = "pob.mode";
    private static final long serialVersionUID = 1L;
    public static final int JOIN_LIVE_TYPE = 1;
    public static final int JOIN_NUMBER_TYPE = 0;
    public static final int PUB_MODE_DEF = 0;
    public static final int PUB_MODE_VIDEO = 1;
    public static final int PUB_MODE_VIDEO_DOC = 2;
    private String loginAccount;
    private String loginPwd;
    private String domain;
    private String number;
    private String pwd;
    private String name;
    private String liveId;
    private int appServiceType;
    private int joinType = 0;

    private int pubMode = 0;
    private boolean needWatchWord;

    public boolean isNeedWatchWord()
    {
        return this.needWatchWord;
    }

    public void setNeedWatchWord(boolean needWatchWord) {
        this.needWatchWord = needWatchWord;
    }

    public String getLoginAccount() {
        return this.loginAccount;
    }

    public void setLoginAccount(String loginAccount) {
        this.loginAccount = loginAccount;
    }

    public String getLoginPwd() {
        return this.loginPwd;
    }

    public void setLoginPwd(String loginPwd) {
        this.loginPwd = loginPwd;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPwd() {
        return this.pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAppServiceType() {
        return this.appServiceType;
    }

    public void setAppServiceType(int appServiceType) {
        this.appServiceType = appServiceType;
    }

    public String getLiveId()
    {
        return this.liveId;
    }

    public void setLiveId(String liveId) {
        this.liveId = liveId;
    }

    public int getJoinType()
    {
        return this.joinType;
    }

    public void setJoinType(int joinType) {
        this.joinType = joinType;
    }

    public int getPubMode()
    {
        return this.pubMode;
    }

    public void setPubMode(int pubMode) {
        this.pubMode = pubMode;
    }

    public String toString()
    {
        return "JoinParams{loginAccount='" +
                this.loginAccount + '\'' +
                ", loginPwd='" + this.loginPwd + '\'' +
                ", domain='" + this.domain + '\'' +
                ", number='" + this.number + '\'' +
                ", pwd='" + this.pwd + '\'' +
                ", name='" + this.name + '\'' +
                ", liveId='" + this.liveId + '\'' +
                ", appServiceType=" + this.appServiceType +
                ", joinType=" + this.joinType +
                ", pubMode=" + this.pubMode +
                ", needWatchWord=" + this.needWatchWord +
                '}';
    }

    public static JSONObject toJSON(JoinParams joinParam) {
        if (joinParam == null) {
            return null;
        }
        JSONObject jo = null;
        try {
            jo = new JSONObject();
            jo.put("name", joinParam.getName());
            jo.put("account", joinParam.getLoginAccount());
            jo.put("password", joinParam.getPwd());
            jo.put("login.password", joinParam.getLoginPwd());
            jo.put("live.id", joinParam.getLiveId());
            jo.put("pob.mode", joinParam.getPubMode());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }

    public static JoinParams toObject(JSONObject jo) {
        if (jo == null) {
            return null;
        }
        JoinParams jp = new JoinParams();
        jp.setName(jo.optString("name"));
        jp.setLoginAccount(jo.optString("account"));
        jp.setPwd(jo.optString("password"));
        jp.setLoginPwd(jo.optString("login.password"));
        jp.setLiveId(jo.optString("live.id"));
        jp.setPubMode(jo.optInt("pob.mode"));
        return jp;
    }
}
