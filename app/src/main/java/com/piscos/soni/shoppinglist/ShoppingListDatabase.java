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
        versions.add(new SQLVersionExecutor() {
            @Override
            public void Exec(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE ShoppingLists (Id integer primary key autoincrement,Name text collate nocase,UUID text collate nocase,LastLocalUpdateTimeStamp TIMESTAMP, LastSyncTimeStamp TIMESTAMP)");
            }
        });
        versions.add(new SQLVersionExecutor() {
            @Override
            public void Exec(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE ShoppingListItems (Id integer primary key autoincrement,Code text collate nocase, Name text collate nocase, Quantity integer, ShoppingListId text collate nocase,WasModified integer,TimeStamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL)");
            }
        });

        versions.add(new SQLVersionExecutor() {
            @Override
            public void Exec(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE MyShoppingLists (Id integer primary key autoincrement,LastLocalUpdateTimeStamp TIMESTAMP, LastSyncTimeStamp TIMESTAMP)");
            }
        });

        return  versions;
    }
}