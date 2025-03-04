package com.example.isee;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "Userdata.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table User(name TEXT primary key ,language TEXT,voice TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists User");
    }

    public Boolean Insert(String name){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("name",name);
        contentValues.put("language","English");
        contentValues.put("voice","male");

        long result=db.insert("User",null,contentValues);
        db.close();
        if(result==-1){
            return false;
        }else{
            return true;
        }
    }


    public Boolean Update(String name,String voice){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("voice",voice);

        Cursor cursor=db.rawQuery("Select * from User where name =?",new String[]{name});

        if(cursor.getCount()>0) {
            long result = db.update("User", contentValues, "name=?", new String[]{name});

            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }else {
            return false;
        }
    }


    public Cursor getdata(){
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor cursor=db.rawQuery("Select * from User",null);
        return cursor;
    }
}
