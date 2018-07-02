package com.piscos.soni.shoppinglist;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import sqlite.utils.DB;
import sqlite.utils.DBOperation;
import sqlite.utils.DBTransaction;

public class ProductsManager {

    public static void UpdateDatabase(final List<ProductListItem> products)  {
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                db.execSQL("delete from Products");
                for(ProductListItem fb:products) {
                    db.execSQL("insert into Products (Name, Code, ThumbnailUrl,ThumbnailPath) values (?,?,?,?)", new Object[]{fb.getName(),fb.getCode(),fb.mPhotoUrl,fb.getThumbnailPath()});
                }
            }
        });
    }

    public static  List<ProductListItem> GetProducts(){
        final List<ProductListItem> products =  new ArrayList<>();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select Name,Code,ThumbnailUrl from Products",new String[]{});
                while(c.moveToNext()) {
                    products.add(new ProductListItem(c.getString(0),c.getString(1),c.getString(2)));
                }
            }
        });
        return  products;
    }

    public static  List<ShoppingListItem> GetProducts2(){
        final List<ShoppingListItem> products =  new ArrayList<>();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select Name,Code,ThumbnailUrl from Products",new String[]{});
                while(c.moveToNext()) {
                    products.add(new ShoppingListItem(c.getString(0),c.getString(1),c.getString(2)));
                }
            }
        });
        return  products;
    }
}
