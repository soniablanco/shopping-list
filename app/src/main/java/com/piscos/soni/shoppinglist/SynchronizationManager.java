package com.piscos.soni.shoppinglist;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import sqlite.utils.DB;
import sqlite.utils.DBOperation;
import sqlite.utils.DBTransaction;

public class SynchronizationManager {

    public static  List<SyncShoppingList> GetShoppingListsToUpload(){
        final List<SyncShoppingList> shoppingLists =  new ArrayList<>();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select UUID,Name,LastLocalUpdateTimeStamp,LastSyncTimeStamp from ShoppingLists where LastLocalUpdateTimeStamp > (select ifnull(LastSyncTimeStamp,0) from MyShoppingLists)",new String[]{});
                while(c.moveToNext()) {
                    shoppingLists.add(new SyncShoppingList(UUID.fromString(c.getString(0)),c.getString(1),c.getLong(2),c.getLong(3)));
                }
            }
        });
        return  shoppingLists;
    }

    public static List<SyncShoppingListProduct> GetShoppingListItems(final UUID shoppingListId){
        final List<SyncShoppingListProduct> products =  new ArrayList<>();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select ShoppingListItems.Name,ShoppingListItems.Code,ShoppingListItems.Quantity from ShoppingListItems " +
                        "where ShoppingListItems.ShoppingListId = ? and ShoppingListItems.Quantity > ?",new String[]{String.valueOf(shoppingListId),"0"});
                while(c.moveToNext()) {
                    products.add(new SyncShoppingListProduct(c.getString(0),c.getString(1),c.getInt(2)));
                }
            }
        });
        return  products;
    }


}
