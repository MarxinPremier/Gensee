package com.wangzuo.libgensee.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.wangzuo.libgensee.db.PlayerChatDataBaseHelper;
import com.wangzuo.libgensee.entity.chat.AbsChatMessage;
import com.wangzuo.libgensee.entity.chat.PrivateMessage;
import com.wangzuo.libgensee.entity.chat.PublicMessage;
import com.wangzuo.libgensee.entity.chat.SysMessage;
import com.gensee.utils.GenseeLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerChatDataBaseManager {
    private PlayerChatDataBaseHelper dataBaseHelper;
    private SQLiteDatabase mDatabase;
    boolean isClosed = false;

    private boolean isDbClose() {
        return this.isClosed;
    }

    public PlayerChatDataBaseManager(Context context) {
        this.dataBaseHelper = new PlayerChatDataBaseHelper(context.getApplicationContext());
        this.mDatabase = this.dataBaseHelper.getWritableDatabase();
        if(this.mDatabase == null) {
            GenseeLog.e("PlayerChatDataBaseManager", "mDatabase is null");
            this.isClosed = true;
        } else {
            this.isClosed = false;
            this.getChatTableName(this.mDatabase);
        }

    }

    private String getChatTableName(SQLiteDatabase db) {
        String tableName = "table_player_chat";
        db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,colTime TEXT,colText TEXT,colChatType TEXT,colSendUserName TEXT,colSendUserId INTEGER,colRich TEXT,colChatRole INTEGER,colReceiveName TEXT,colReceiveUserId INTEGER,colReserved1 INTEGER,colReserved2 INTEGER,colReserved3 TEXT,colReserved4 TEXT);");
        return tableName;
    }

    private ContentValues _insert(AbsChatMessage entity, ContentValues values) {
        if(values == null) {
            values = new ContentValues();
        } else {
            values.clear();
        }

        if(entity instanceof PrivateMessage) {
            values.put("colChatType", "private");
            values.put("colReceiveUserId", Long.valueOf(((PrivateMessage)entity).getReceiveUserId()));
            values.put("colReceiveName", ((PrivateMessage)entity).getReceiveName());
        } else if(entity instanceof PublicMessage) {
            values.put("colChatType", "public");
        } else if(entity instanceof SysMessage) {
            values.put("colChatType", "sys");
        }

        values.put("colSendUserId", Long.valueOf(entity.getSendUserId()));
        values.put("colSendUserName", entity.getSendUserName());
        values.put("colRich", entity.getRich());
        values.put("colChatRole", Integer.valueOf(entity.getSenderRole()));
        values.put("colText", entity.getText());
        values.put("colTime", Long.valueOf(entity.getTime()));
        values.put("colReserved1", Integer.valueOf(0));
        values.put("colReserved2", Integer.valueOf(0));
        values.put("colReserved3", "");
        values.put("colReserved4", "");
        return values;
    }

    public void insertValues(List<AbsChatMessage> msgList) {
        if(!this.isDbClose()) {
            try {
                String e = this.getChatTableName(this.mDatabase);
                this.mDatabase.beginTransaction();
                ContentValues values = new ContentValues();

                for(int i = 0; i < msgList.size(); ++i) {
                    AbsChatMessage entity = (AbsChatMessage)msgList.get(i);
                    this._insert(entity, values);
                    this.mDatabase.insert(e, (String)null, values);
                }

                this.mDatabase.setTransactionSuccessful();
            } catch (Exception var9) {
                var9.printStackTrace();
            } finally {
                this.mDatabase.endTransaction();
            }

        }
    }

    private void add(AbsChatMessage entity) {
        String chatType = "";
        String receiveName = "";
        long receiveUserId = 0L;
        if(entity instanceof PrivateMessage) {
            chatType = "private";
            receiveUserId = ((PrivateMessage)entity).getReceiveUserId();
            receiveName = ((PrivateMessage)entity).getReceiveName();
        } else if(entity instanceof PublicMessage) {
            chatType = "public";
        } else if(entity instanceof SysMessage) {
            chatType = "sys";
        }

        String sql = "insert into table_player_chat(colText,colRich,colTime,colSendUserId,colSendUserName,colReceiveName,colReceiveUserId,colChatType,colReserved1,colReserved2,colReserved3,colReserved4) values(?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            this.mDatabase.execSQL(sql, new Object[]{entity.getText(), entity.getRich(), Long.valueOf(entity.getTime()), Long.valueOf(entity.getSendUserId()), entity.getSendUserName(), receiveName, Long.valueOf(receiveUserId), chatType, Integer.valueOf(0), Integer.valueOf(0), "", ""});
        } catch (Exception var8) {
            GenseeLog.e("execSQL", "mDatabase.execSQL exception:" + var8);
        }

    }

    private AbsChatMessage dataToObject(Cursor cursor) {
        String msgType = cursor.getString(cursor.getColumnIndex("colChatType"));
        Object msg = null;
        if("private".equals(msgType)) {
            msg = new PrivateMessage();
            ((PrivateMessage)msg).setReceiveUserId(cursor.getLong(cursor.getColumnIndex("colReceiveUserId")));
            ((PrivateMessage)msg).setReceiveName(cursor.getString(cursor.getColumnIndex("colReceiveName")));
        } else if("public".equals(msgType)) {
            msg = new PublicMessage();
        } else if("sys".equals(msgType)) {
            msg = new SysMessage();
        }

        if(msg != null) {
            ((AbsChatMessage)msg).setSendUserId(cursor.getLong(cursor.getColumnIndex("colSendUserId")));
            ((AbsChatMessage)msg).setSendUserName(cursor.getString(cursor.getColumnIndex("colSendUserName")));
            ((AbsChatMessage)msg).setRich(cursor.getString(cursor.getColumnIndex("colRich")));
            ((AbsChatMessage)msg).setSenderRole(cursor.getInt(cursor.getColumnIndex("colChatRole")));
            ((AbsChatMessage)msg).setText(cursor.getString(cursor.getColumnIndex("colText")));
            ((AbsChatMessage)msg).setTime(cursor.getLong(cursor.getColumnIndex("colTime")));
        }

        return (AbsChatMessage)msg;
    }

    public AbsChatMessage getLatestMsg() {
        Cursor cursor = null;
        AbsChatMessage msg = null;
        if(this.isDbClose()) {
            return null;
        } else {
            try {
                String sTableName = this.getChatTableName(this.mDatabase);
                String sql = "";
                sql = "select * from " + sTableName + " order by " + "colTime" + " desc limit ?";
                cursor = this.mDatabase.rawQuery(sql, new String[]{"1"});
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    AbsChatMessage entity = this.dataToObject(cursor);
                    msg = entity;
                    cursor.moveToNext();
                }
            } finally {
                if(cursor != null) {
                    cursor.close();
                }

            }

            return msg;
        }
    }

    public List<AbsChatMessage> getLatestMsgsList(int limit) {
        Cursor cursor = null;
        ArrayList msgList = new ArrayList();
        if(this.isDbClose()) {
            return msgList;
        } else {
            try {
                String sTableName = this.getChatTableName(this.mDatabase);
                String sql = "";
                sql = "select * from " + sTableName + " order by " + "colTime" + " desc limit ?";
                cursor = this.mDatabase.rawQuery(sql, new String[]{String.valueOf(limit)});
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    AbsChatMessage entity = this.dataToObject(cursor);
                    msgList.add(entity);
                    cursor.moveToNext();
                }
            } finally {
                if(cursor != null) {
                    cursor.close();
                }

            }

            Collections.reverse(msgList);
            return msgList;
        }
    }

    public List<AbsChatMessage> getMsgsByOwnerId(long msgOwnerId) {
        Cursor cursor = null;
        ArrayList msgList = new ArrayList();
        if(this.isDbClose()) {
            return msgList;
        } else {
            try {
                String sql = "";
                String sTableName = this.getChatTableName(this.mDatabase);
                sql = "select * from " + sTableName + " where " + "colReceiveUserId" + "=?" + " or " + "colSendUserId" + "=?" + " order by " + "colTime";
                cursor = this.mDatabase.rawQuery(sql, new String[]{String.valueOf(msgOwnerId), String.valueOf(msgOwnerId)});
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    AbsChatMessage entity = this.dataToObject(cursor);
                    msgList.add(entity);
                    cursor.moveToNext();
                }
            } finally {
                if(cursor != null) {
                    cursor.close();
                }

            }

            return msgList;
        }
    }

    public List<AbsChatMessage> queryChatMsgsLimitNext(int limit, long timeMillions) {
        Cursor cursor = null;
        ArrayList msgList = new ArrayList();
        if(this.isDbClose()) {
            return msgList;
        } else {
            try {
                String sTableName = this.getChatTableName(this.mDatabase);
                String sql = "";
                sql = "select * from " + sTableName + " where " + "colTime" + ">?" + " order by " + "colTime" + " limit ?";
                cursor = this.mDatabase.rawQuery(sql, new String[]{String.valueOf(timeMillions), String.valueOf(limit)});
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    AbsChatMessage entity = this.dataToObject(cursor);
                    msgList.add(entity);
                    cursor.moveToNext();
                }
            } finally {
                if(cursor != null) {
                    cursor.close();
                }

            }

            return msgList;
        }
    }

    public List<AbsChatMessage> queryChatMsgsLimitPre(int limit, long timeMillions) {
        ArrayList msgList = new ArrayList();
        if(this.isDbClose()) {
            return msgList;
        } else {
            Cursor cursor = null;

            try {
                String sql = "";
                String sTableName = this.getChatTableName(this.mDatabase);
                sql = "select * from " + sTableName + " where " + "colTime" + "<?" + " order by " + "colTime" + " desc limit ?";
                cursor = this.mDatabase.rawQuery(sql, new String[]{String.valueOf(timeMillions), String.valueOf(limit)});
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    AbsChatMessage entity = this.dataToObject(cursor);
                    msgList.add(entity);
                    cursor.moveToNext();
                }
            } finally {
                if(cursor != null) {
                    cursor.close();
                }

            }

            Collections.reverse(msgList);
            return msgList;
        }
    }

    public void removeAllChatMsgs() {
        if(!this.isDbClose()) {
            String sTableName = this.getChatTableName(this.mDatabase);
            this.mDatabase.delete(sTableName, (String)null, (String[])null);
        }
    }

    public int removeChatMsgByUUID(String UUID) {
        Integer nReturnValue = Integer.valueOf(0);
        if(this.isDbClose()) {
            return nReturnValue.intValue();
        } else {
            String sTableName = this.getChatTableName(this.mDatabase);
            String sCondition = "colTime=?";
            nReturnValue = Integer.valueOf(this.mDatabase.delete(sTableName, sCondition, new String[]{UUID}));
            return nReturnValue.intValue();
        }
    }

    public void dropChatTable() {
        if(!this.isDbClose()) {
            this.mDatabase.execSQL("DROP TABLE IF EXISTS table_player_chat");
        }
    }

    public void closeDb() {
        if(this.mDatabase != null && this.mDatabase.isOpen()) {
            this.mDatabase.close();
            this.isClosed = true;
        }

    }

    public List<AbsChatMessage> getLatestMsgsByOwnerId(int limit, long msgOwnerId) {
        Cursor cursor = null;
        ArrayList msgList = new ArrayList();
        if(this.isDbClose()) {
            return msgList;
        } else {
            try {
                String sql = "";
                String sTableName = this.getChatTableName(this.mDatabase);
                sql = "select * from " + sTableName + " where " + "colReceiveUserId" + "=?" + " or " + "colSendUserId" + "=?" + " order by " + "colTime" + " desc limit ?";
                cursor = this.mDatabase.rawQuery(sql, new String[]{String.valueOf(msgOwnerId), String.valueOf(msgOwnerId), String.valueOf(limit)});
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    AbsChatMessage entity = this.dataToObject(cursor);
                    msgList.add(entity);
                    cursor.moveToNext();
                }
            } finally {
                if(cursor != null) {
                    cursor.close();
                }

            }

            Collections.reverse(msgList);
            return msgList;
        }
    }

    public List<AbsChatMessage> queryChatMsgsByOwnerIdLimitNext(long ownerId, int limit, long timeMillions) {
        Cursor cursor = null;
        ArrayList msgList = new ArrayList();
        if(this.isDbClose()) {
            return msgList;
        } else {
            try {
                String sTableName = this.getChatTableName(this.mDatabase);
                String sql = "";
                sql = "select * from " + sTableName + " where (" + "colReceiveUserId" + "=?" + " or " + "colSendUserId" + "=?)" + " and " + "colTime" + ">?" + " order by " + "colTime" + " limit ?";
                cursor = this.mDatabase.rawQuery(sql, new String[]{String.valueOf(ownerId), String.valueOf(ownerId), String.valueOf(timeMillions), String.valueOf(limit)});
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    AbsChatMessage entity = this.dataToObject(cursor);
                    msgList.add(entity);
                    cursor.moveToNext();
                }
            } finally {
                if(cursor != null) {
                    cursor.close();
                }

            }

            return msgList;
        }
    }

    public List<AbsChatMessage> queryChatMsgsByOwnerIdLimitPre(long ownerId, int limit, long timeMillions) {
        ArrayList msgList = new ArrayList();
        if(this.isDbClose()) {
            return msgList;
        } else {
            Cursor cursor = null;

            try {
                String sql = "";
                String sTableName = this.getChatTableName(this.mDatabase);
                sql = "select * from " + sTableName + " where (" + "colReceiveUserId" + "=?" + " or " + "colSendUserId" + "=?)" + " and " + "colTime" + "<?" + " order by " + "colTime" + " desc limit ?";
                cursor = this.mDatabase.rawQuery(sql, new String[]{String.valueOf(ownerId), String.valueOf(ownerId), String.valueOf(timeMillions), String.valueOf(limit)});
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    AbsChatMessage entity = this.dataToObject(cursor);
                    msgList.add(entity);
                    cursor.moveToNext();
                }
            } finally {
                if(cursor != null) {
                    cursor.close();
                }

            }

            Collections.reverse(msgList);
            return msgList;
        }
    }

    public List<AbsChatMessage> getPrivateLatestMsgsByOwnerId(int limit, long msgOwnerId, long selfId) {
        Cursor cursor = null;
        ArrayList msgList = new ArrayList();
        if(this.isDbClose()) {
            return msgList;
        } else {
            try {
                String sql = "";
                String sTableName = this.getChatTableName(this.mDatabase);
                sql = "select * from " + sTableName + " where " + "(" + "colReceiveUserId" + "=?" + " and " + "colSendUserId" + "=?)" + " or " + "(" + "colReceiveUserId" + "=?" + " and " + "colSendUserId" + "=?)" + " order by " + "colTime" + " desc limit ?";
                cursor = this.mDatabase.rawQuery(sql, new String[]{String.valueOf(msgOwnerId), String.valueOf(selfId), String.valueOf(selfId), String.valueOf(msgOwnerId), String.valueOf(limit)});
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    AbsChatMessage entity = this.dataToObject(cursor);
                    msgList.add(entity);
                    cursor.moveToNext();
                }
            } finally {
                if(cursor != null) {
                    cursor.close();
                }

            }

            Collections.reverse(msgList);
            return msgList;
        }
    }

    public List<AbsChatMessage> queryPrivateChatMsgsByOwnerIdLimitNext(long selfId, long toUserId, int limit, long timeMillions) {
        Cursor cursor = null;
        ArrayList msgList = new ArrayList();
        if(this.isDbClose()) {
            return msgList;
        } else {
            try {
                String sTableName = this.getChatTableName(this.mDatabase);
                String sql = "";
                sql = "select * from " + sTableName + " where " + "((" + "colReceiveUserId" + "=?" + " and " + "colSendUserId" + "=?)" + " or " + "(" + "colReceiveUserId" + "=?" + " and " + "colSendUserId" + "=?)) " + " and " + "colTime" + ">?" + " order by " + "colTime" + " limit ?";
                cursor = this.mDatabase.rawQuery(sql, new String[]{String.valueOf(selfId), String.valueOf(toUserId), String.valueOf(toUserId), String.valueOf(selfId), String.valueOf(timeMillions), String.valueOf(limit)});
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    AbsChatMessage entity = this.dataToObject(cursor);
                    msgList.add(entity);
                    cursor.moveToNext();
                }
            } finally {
                if(cursor != null) {
                    cursor.close();
                }

            }

            return msgList;
        }
    }

    public List<AbsChatMessage> queryPrivateChatMsgsByOwnerIdLimitPre(long selfId, long toUserId, int limit, long timeMillions) {
        ArrayList msgList = new ArrayList();
        if(this.isDbClose()) {
            return msgList;
        } else {
            Cursor cursor = null;

            try {
                String sql = "";
                String sTableName = this.getChatTableName(this.mDatabase);
                sql = "select * from " + sTableName + " where " + "((" + "colReceiveUserId" + "=?" + " and " + "colSendUserId" + "=?)" + " or " + "(" + "colReceiveUserId" + "=?" + " and " + "colSendUserId" + "=?))" + " and " + "colTime" + "<?" + " order by " + "colTime" + " desc limit ?";
                cursor = this.mDatabase.rawQuery(sql, new String[]{String.valueOf(selfId), String.valueOf(toUserId), String.valueOf(toUserId), String.valueOf(selfId), String.valueOf(timeMillions), String.valueOf(limit)});
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    AbsChatMessage entity = this.dataToObject(cursor);
                    msgList.add(entity);
                    cursor.moveToNext();
                }
            } finally {
                if(cursor != null) {
                    cursor.close();
                }

            }

            Collections.reverse(msgList);
            return msgList;
        }
    }
}
