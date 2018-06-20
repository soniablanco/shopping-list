package com.piscos.soni.shoppinglist;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import sqlite.utils.DatabaseInfo;
import sqlite.utils.SQLVersionExecutor;

public class ShoppingListDatabase extends DatabaseInfo {
    @Override
    public String GetName() {
        return "shoppinglistdb";
    }

    @Override
    public ArrayList<SQLVersionExecutor> GetDatabaseStructure() {
        ArrayList<SQLVersionExecutor> versions=new ArrayList<SQLVersionExecutor>();
        versions.add(new SQLVersionExecutor() {
            @Override
            public void Exec(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE Products (Id integer primary key autoincrement,Code text collate nocase, Name text collate nocase, ThumbnailUrl text)");
            }
        });
        versions.add(new SQLVersionExecutor() {
            @Override
            public void Exec(SQLiteDatabase db) {
                db.execSQL("ALTER TABLE Products add column ThumbnailPath text");
            }
        });

        return  versions;
    }
}