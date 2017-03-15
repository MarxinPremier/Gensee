package com.wangzuo.libgensee.util;


import android.os.Environment;
import java.io.File;

public class ConfigApp
{
    public static final String APP_JOIN_PARAMS = "APP_JOIN_PARAMS";
    public static final String APP_START_TYPE = "APP_START_TYPE";
    public static final String APP_LUNACH_CODE = "APP_LUNACH_CODE";
    public static final String APP_LUNACH_ENTITY = "APP_LUNACH_ENTITY";
    public static final String APP_WEB_START = "APP_WEB_START";
    public static final String APP_DESKTOP_START = "APP_DESKTOP_START";
    public static final int PARSE_SCAN_MESSAGE = 100;
    public static final String SUCCESS_SCAN_MESSAGE = "SUCCESS_SCAN_MESSAGE";
    public static final int QRCODE_SELECT_BITMAP = 101;
    public static final boolean DEBUG = true;
    public static final int MAX_SAVE_DOMAIN = 6;
    public static String ROOTPAHT = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "FastSdk-logcat" + File.separator;
    public static String LOGPATH = ROOTPAHT + "log" + File.separator;
    public static final int NICK_NAME_MAX_LENGHT = 12;
    public static final int NOTIFY_DATA = 200;
    public static final int NEW_MSG = 10000;
    public static final String LATEST = "LATEST";
    public static final int NEW_REFRESH = 10002;
    public static final int NEW_LOADMORE = 10003;
    public static final int NEW_SELF_MSG = 10004;
    public static final int NEW_SELF_REFRESH = 10005;
    public static final int NEW_SELF_LOADMORE = 10006;
    public static final String QALATEST = "QALATEST";
    public static final int NEW_QA_MSG = 20000;
    public static final int QA_CANCEL_PUB = 20001;
    public static final int NEW_QA_SELF_MSG = 20002;
    public static final int NEW_QA_LAST_MSG = 20003;
    public static final int NEW_QA_REFRESH = 20004;
    public static final int NEW_QA_LOADMORE = 20005;
    public static final int NEW_QA_SELF_REFRESH = 20006;
    public static final int NEW_QA_SELF_LOADMORE = 20007;
    public static final int QA_CANCEL_SELF_PUB = 20008;
    public static final int PRIVATE_NEW_MSG = 30000;
    public static final String OWNERID = "OWNERID";
    public static final int PRIVATE_NEW_REFRESH = 30002;
    public static final int PRIVATE_NEW_LOADMORE = 30003;
    public static final int BOTTOM_MSG = 40000;
    public static final int BOTTOM_PRIVATE_UPDATE_MSG = 40001;
    public static final int BOTTOM_QA_UPDATE_MSG = 40002;
    public static final int PUBLISH_DOC_MAX = 512000;
    public static final int PUBLISH_DOC_WIDTH = 800;
    public static final int PUBLISH_DOC_HEIGHT = 600;
    public static final int HARD_DECODE_SETTING = 50000;

    static
    {
        File rootDir = new File(ROOTPAHT);
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
        File fileDir = new File(LOGPATH);
        if (!fileDir.exists())
            fileDir.mkdirs();
    }

    public static abstract interface AppServiceType
    {
        public static final int UNKOWN = -1;
        public static final int WEBCAST = 0;
        public static final int TRAINING = 1;
    }

    public static abstract interface ROLLCALL
    {
        public static final int ROLL_CALL_ACK = 500;
        public static final int ROLL_CALL_ACK_TIMEOUT = 501;
    }

    public static abstract interface VOTE
    {
        public static final int VOTE_JOIN_CONFIREM = 400;
        public static final int VOTE_ADD = 401;
        public static final int VOTE_DEL = 402;
        public static final int VOTE_PUBLISH = 403;
        public static final int VOTE_PUBLISH_RESULT = 404;
        public static final int VOTE_SUBMIT = 405;
        public static final int VOTE_DEADLINT = 406;
        public static final int VOTE_POST_URL = 407;
    }
}
