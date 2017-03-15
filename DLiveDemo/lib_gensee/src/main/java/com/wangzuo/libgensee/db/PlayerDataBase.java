package com.wangzuo.libgensee.db;

import android.provider.BaseColumns;

public final class PlayerDataBase
{
    public static final class ChatColumns
            implements BaseColumns
    {
        public static final String PLAYER_TAB_NAME = "table_player_chat";
        public static final String PLAYER_CHAT_TEXT = "colText";
        public static final String PLAYER_CHAT_RICH = "colRich";
        public static final String PLAYER_CHAT_TIME = "colTime";
        public static final String PLAYER_CHAT_SENDUSERID = "colSendUserId";
        public static final String PLAYER_CHAT_SENDUSERNAME = "colSendUserName";
        public static final String PLAYER_CHAT_RECEIVENAME = "colReceiveName";
        public static final String PLAYER_CHAT_RECEIVEUSERID = "colReceiveUserId";
        public static final String PLAYER_CHAT_TYPE = "colChatType";
        public static final String PLAYER_CHAT_RESERVED1 = "colReserved1";
        public static final String PLAYER_CHAT_RESERVED2 = "colReserved2";
        public static final String PLAYER_CHAT_RESERVED3 = "colReserved3";
        public static final String PLAYER_CHAT_RESERVED4 = "colReserved4";
        public static final String PLAYER_CHAT_ROLE = "colChatRole";
    }

    public static final class QaColumns
            implements BaseColumns
    {
        public static final String PLAYER_QA_TAB_NAME = "table_player_qa";
        public static final String PLAYER_QA_COL_TIMESTAMP = "colTimestamp";
        public static final String PLAYER_QA_COL_QUESTION_ID = "colQId";
        public static final String PLAYER_QA_COL_QUESTION_TIMESTAMP = "colQuestTimgstamp";
        public static final String PLAYER_QA_COL_QUESTION_OWNERNAME = "colQuestOwnerName";
        public static final String PLAYER_QA_COL_QUESTION_OWNERID = "colQuestOwnerId";
        public static final String PLAYER_QA_COL_QUESTION = "colQuestion";
        public static final String PLAYER_QA_COL_ANSWER_OWNER = "colAnswerOwner";
        public static final String PLAYER_QA_COL_ANSWER_ID = "colAnswerId";
        public static final String PLAYER_QA_COL_ANSWER_TIMESTAMP = "colAnswerTimestamp";
        public static final String PLAYER_QA_COL_ANSWER = "colAnswer";
    }
}
