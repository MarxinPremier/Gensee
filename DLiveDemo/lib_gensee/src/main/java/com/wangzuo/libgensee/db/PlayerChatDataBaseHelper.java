package com.wangzuo.libgensee.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlayerChatDataBaseHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "FastSdkChat.db";
    private static final int DATABASE_VERSION = 1;
    public static final String CREATE_TABLE_CHAT = " (_id INTEGER PRIMARY KEY AUTOINCREMENT,colTime TEXT,colText TEXT,colChatType TEXT,colSendUserName TEXT,colSendUserId INTEGER,colRich TEXT,colChatRole INTEGER,colReceiveName TEXT,colReceiveUserId INTEGER,colReserved1 INTEGER,colReserved2 INTEGER,colReserved3 TEXT,colReserved4 TEXT);";

    public PlayerChatDataBaseHelper(Context context)
    {
        super(context, "FastSdkChat.db", null, 1);
    }

    public void onCreate(SQLiteDatabase db)
    {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }
}