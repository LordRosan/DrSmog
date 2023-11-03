package com.jlu.drsmog.database;

import com.google.android.material.animation.ArgbEvaluatorCompat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;
import com.jlu.drsmog.adapters.Record;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "my_database.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "my_table";
    private static DatabaseHelper instance;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY, time TEXT, blackness TEXT, path TEXT, name TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public void addData(String time, String blackness, String path, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("time", time);
        values.put("blackness", blackness);
        values.put("path", path);
        values.put("name",name);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void deleteData(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Record> getAllData() {
        List<Record> recordsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("DatabaseHelper", "Database opened for reading."); // 日志输出打开数据库

        Cursor cursor = db.query(TABLE_NAME, new String[]{"id", "time", "blackness", "path", "name"}, null, null, null, null, null);
        Log.d("DatabaseHelper", "Query executed."); // 日志输出执行查询

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                String blackness = cursor.getString(cursor.getColumnIndexOrThrow("blackness"));
                String path = cursor.getString(cursor.getColumnIndexOrThrow("path"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

                // 创建新的 Record 对象并加入到 list 中
                Record record = new Record(id, time, blackness, path, name);
                recordsList.add(record);

                Log.d("DatabaseHelper", "Record added: " + record.toString()); // 日志输出每条记录
            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseHelper", "Cursor is empty. No data to retrieve."); // 日志输出空结果集
        }
        cursor.close();
        db.close();
        Log.d("DatabaseHelper", "Database closed."); // 日志输出关闭数据库
        Log.d("DatabaseHelper", "Total items retrieved: " + recordsList.size()); // 日志输出数据项总数
        return recordsList;
    }

    public int getIdByName(String blackness) {
        int id = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{"id"}, "blackness = ?", new String[]{blackness}, null, null, null);
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);  // 'id' is at index 0
        }
        cursor.close();
        return id;
    }
}