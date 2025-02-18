package com.example.focusbuddy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SessionHistoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sessionHistory.db";
    private static final int DATABASE_VERSION = 2;

    public SessionHistoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_SESSION_ENTRIES =
                "CREATE TABLE SessionHistory (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "focusTime INTEGER NOT NULL," +
                        "breakActivity TEXT NOT NULL," +
                        "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")";
        String SQL_CREATE_POINTS_ENTRIES =
                "CREATE TABLE Points (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "points INTEGER NOT NULL" +
                        ")";
        String SQL_CREATE_BADGES_ENTRIES =
                "CREATE TABLE Badges (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "badge TEXT NOT NULL" +
                        ")";
        db.execSQL(SQL_CREATE_SESSION_ENTRIES);
        db.execSQL(SQL_CREATE_POINTS_ENTRIES);
        db.execSQL(SQL_CREATE_BADGES_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Upgrade database schema if needed
            db.execSQL("DROP TABLE IF EXISTS Points");
            db.execSQL("DROP TABLE IF EXISTS Badges");
            db.execSQL("DROP TABLE IF EXISTS SessionHistory");
            onCreate(db);
        }
    }
}
