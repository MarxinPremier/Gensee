package com.wangzuo.libgensee.core;


public abstract interface UIMsg
{
    public static final int ROOM_ON_ROOM_JOIN = 1000;
    public static final int ROOM_ON_ROOM_USER_JOIN = 1001;
    public static final int ROOM_ON_ROOM_USER_UPDATE = 1002;
    public static final int ROOM_ON_ROOM_USER_LEAVE = 1003;
    public static final int ROOM_ON_ROOM_HANDUP = 1004;
    public static final int ROOM_ON_ROOM_HANDDOWN = 1005;
    public static final int ROOM_ON_ROOM_SUBJECT = 1006;
    public static final int ROOM_ON_ROOM_PUBLISH = 1007;
    public static final int ROOM_ON_ROOM_LEAVE = 1008;
    public static final int ROOM_ON_ROOM_RECONNENT = 1009;
    public static final int ROOM_ON_ROOM_NOT_ATTENDE = 1010;
    public static final int ROOM_JOIN_PANELIST = 1011;
    public static final int ROOM_HOST_ROLE = 1012;
    public static final int ROOM_USER_ROLE = 1013;
    public static final int ROOM_NETWORK_REPORT = 1014;
    public static final int ROOM_ROOT_SVR = 1015;
    public static final int ROOM_ROOT_SVR_SUCCESS = 1016;
    public static final int VIDEO_ON_VIDEO_START = 2000;
    public static final int VIDEO_ON_VIDEO_END = 2001;
    public static final int VIDEO_ON_CAMERA_CLOSE = 2002;
    public static final int VIDEO_ON_CAMERA_OPEN = 2003;
    public static final int VIDEO_UNDISPLAY = 2004;
    public static final int VIDEO_ACTIVED_SELF = 2005;
    public static final int VIDEO_RENDER_SIZE = 2006;
    public static final int AUDIO_ON_AUDIO_MIC_OPEN = 3000;
    public static final int AUDIO_ON_AUDIO_MIC_CLOSE = 3001;
    public static final int AUDIO_ON_AUDIO_LEVEL = 3002;
    public static final int doc_on_doc_gotopage = 4000;
    public static final int doc_on_doc_closed = 4001;
    public static final int DISMISS_POP_WINDOW = 5000;
    public static final int ROLE_PRESENTOR_TO_HOST = 6000;
    public static final int ROLE_HOST_TO_PANELIST = 6001;
    public static final int ROLE_PANELIST_TO_HOST = 6002;
    public static final int ROLE_PANELIST_TO_PRESENTOR = 6003;
    public static final int ROLE_PRESENTOR_TO_PANELIST = 6004;
    public static final int ROLE_HOST_DOWNGRADE = 6006;
    public static final int ROLE_CHANGE = 6005;
    public static final int ROLE_HOST_TO_PRESENTOR = 6007;
    public static final int ROLE_HOST_NOT_IN_ROSTRUM = 6008;
    public static final int LOD_ON_LOD_START = 7000;
    public static final int LOD_ON_LOD_END = 7001;
    public static final int AS_ON_AS_START = 8000;
    public static final int AS_ON_AS_END = 8001;
    public static final int CHAT_FORBID = 9000;
}