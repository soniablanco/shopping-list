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

public class ShoppingListManager {

    public static  List<ShoppingListItem> GetAllProducts(){
        final List<ShoppingListItem> products =  new ArrayList<>();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select Name,Code,ThumbnailPath from Products",new String[]{});
                while(c.moveToNext()) {
                    products.add(new ShoppingListItem(c.getString(0),c.getString(1),c.getString(2)));
                }
            }
        });
        return  products;
    }

    public static void CreateShoppingList(final String name,final UUID uuid)  {
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                db.execSQL("insert into ShoppingLists (Name,UUID) values (?,?)",new Object[]{name, uuid});
            }
        });
    }

    public static void AddShoppingListProducts(final UUID shoppingListId, final List<ShoppingListItem> products){
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                db.execSQL("delete from ShoppingListItems where ShoppingListId = ?",new Object[]{shoppingListId});
                for(ShoppingListItem fb:products) {
                    db.execSQL("insert into ShoppingListItems (Name, Code, ThumbnailPath,Quantity,ShoppingListId) values (?,?,?,?,?)", new Object[]{fb.getName(),fb.getCode(),fb.getThumbnailPath(),fb.getQuantity(),shoppingListId});
                }
            }
        });

    }

    public static void AddShoppingListProduct(final UUID shoppingListId, final ShoppingListItem product){
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c = db.rawQuery("select Name from ShoppingListItems where ShoppingListId = ? and Code = ?",new String[]{String.valueOf(shoppingListId),String.valueOf(product.getCode())});
                if (c.moveToNext()){
                    db.execSQL("Update ShoppingListItems Set Quantity = ?, WasModified =?,TimeStamp =? where ShoppingListId = ? and Code = ?",new Object[]{product.getQuantity(),1,product.getTimestamp(),shoppingListId,product.getCode()});
                }
                else {
                    db.execSQL("insert into ShoppingListItems (Name, Code, ThumbnailPath,Quantity,ShoppingListId,WasModified,TimeStamp) values (?,?,?,?,?,?,?)", new Object[]{product.getName(),product.getCode(),product.getThumbnailPath(),product.getQuantity(),shoppingListId,1,product.getTimestamp()});
                }
            }
        });
    }

    public static void DeleteShoppingListProduct(final UUID shoppingListId, final ShoppingListItem product){
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                db.execSQL("delete from ShoppingListItems where ShoppingListId = ? and Code = ?",new Object[]{shoppingListId, product.getCode()});
            }
        });
    }

    public static String GetShoppingListName(final UUID shoppingListId){
        final String[] name= new String [1];
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select Name from ShoppingLists where UUID = ?",new String[]{String.valueOf(shoppingListId)});
                if(c.moveToFirst()) {
                    name[0] = c.getString(0);
                }
            }
        });
        return name[0];
    }

    public static List<ShoppingListItem> GetShoppingListProducts(final UUID shoppingListId){
        final List<ShoppingListItem> products =  new ArrayList<>();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select Products.Name,Products.Code,Products.ThumbnailPath, ifnull(ShoppingListItems.Quantity,0),ShoppingListItems.TimeStamp from Products " +
                        "left join ShoppingListItems " +
                        "on Products.Code = ShoppingListItems.Code " +
                        "and ShoppingListItems.ShoppingListId = ?",new String[]{String.valueOf(shoppingListId)});
                while(c.moveToNext()) {
                    products.add(new ShoppingListItem(c.getString(0),c.getString(1),c.getString(2),c.getInt(3),c.getLong(4)));
                }
            }
        });
        return  products;
    }

    public static List<ShoppingListItem> GetShoppingListItems(final UUID shoppingListId){
        final List<ShoppingListItem> products =  new ArrayList<>();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select ShoppingListItems.Name,ShoppingListItems.Code,ShoppingListItems.ThumbnailPath, ShoppingListItems.Quantity,ShoppingListItems.TimeStamp from ShoppingListItems " +
                        "where ShoppingListItems.ShoppingListId = ?",new String[]{String.valueOf(shoppingListId)});
                while(c.moveToNext()) {
                    products.add(new ShoppingListItem(c.getString(0),c.getString(1),c.getString(2),c.getInt(3),c.getLong(4)));
                }
            }
        });
        return  products;
    }

    public static List<ShoppingListElement> GetAllShoppingLists(){
        final List<ShoppingListElement> shoppingLists =  new ArrayList<>();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select Id,Name, 2,UUID from ShoppingLists",new String[]{});
                while(c.moveToNext()) {
                    shoppingLists.add(new ShoppingListElement(c.getInt(0),c.getString(1),c.getInt(2),UUID.fromString(c.getString(3))));
                }
            }
        });
        return shoppingLists;
    }

    /*public static ShoppingList GetShoppingList(final int ShoppingListId){

        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select Name,Code,ThumbnailUrl from Products",new String[]{});
                while(c.moveToNext()) {
                    products.add(new ShoppingListItem(c.getString(0),c.getString(1),c.getString(2)));
                }
            }
        });
    }*/
}
