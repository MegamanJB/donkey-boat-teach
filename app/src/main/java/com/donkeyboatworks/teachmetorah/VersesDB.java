package com.donkeyboatworks.teachmetorah;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Joshua on 7/12/2015.
 */

public class VersesDB extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "verses";
    private static final String TABLE_NAME = "UrlResponse";
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "url TEXT PRIMARY KEY, " +
                    "response TEXT);";

    VersesDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String getVerseByUrl(String url)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT response FROM UrlResponse WHERE url = '" + url + "'", null);
        cursor.moveToFirst();

        try {
            String response = cursor.getString(cursor.getColumnIndex("response"));

            if (!cursor.isClosed()) {
                cursor.close();
            }

            return response;
        }
        catch (CursorIndexOutOfBoundsException exception) {
            return null;
        }
    }

    public Boolean insertVerseByUrl(String url, String response)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("url", url);
        contentValues.put("response", response);
        db.insert("UrlResponse", null, contentValues);
        return true;
    }
}