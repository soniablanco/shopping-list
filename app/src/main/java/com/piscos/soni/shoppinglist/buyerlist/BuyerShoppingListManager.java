package com.piscos.soni.shoppinglist.buyerlist;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.piscos.soni.shoppinglist.shoppinglist.ShoppingList;
import com.piscos.soni.shoppinglist.shoppinglist.ShoppingListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import sqlite.utils.DB;
import sqlite.utils.DBOperation;
import sqlite.utils.DBTransaction;

public class BuyerShoppingListManager {

    public static BuyerShoppingList getShoppingList(final UUID shoppingListId){
        final List<BuyerShoppingList> list = new ArrayList<>();// = new ShoppingList();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select Name,LastLocalUpdateTimeStamp from ShoppingLists where UUID = ?",new String[]{String.valueOf(shoppingListId)});
                if(c.moveToFirst()) {
                    list.add(new BuyerShoppingList(shoppingListId,c.getString(0),c.getLong(1)));
                }
            }
        });
        return list.get(0);
    }

    public static void updateShoppingList(final BuyerShoppingList shoppingList){
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                db.execSQL("Update ShoppingLists Set Name=?,LastLocalUpdateTimeStamp=ifnull(?,LastLocalUpdateTimeStamp) where UUID = ?",new Object[]{shoppingList.mName,shoppingList.mLastLocalTimeStamp,shoppingList.mId});
            }
        });
    }

    public static List<BuyerShoppingListItem> getShoppingListItems(final UUID shoppingListId){
        final List<BuyerShoppingListItem> products =  new ArrayList<>();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select ShoppingListItems.Name,ShoppingListItems.Code,Products.ThumbnailPath, ShoppingListItems.Quantity,ShoppingListItems.TimeStamp,ShoppingListItems.WasModified,ShoppingListItems.WasCollected from ShoppingListItems " +
                        "join Products on  Products.Code = ShoppingListItems.Code " +
                        "where ShoppingListItems.ShoppingListId = ? AND ShoppingListItems.Quantity > ? order by ShoppingListItems.Name",new String[]{String.valueOf(shoppingListId),"0"});
                while(c.moveToNext()) {
                    products.add(new BuyerShoppingListItem(c.getString(0),c.getString(1),c.getString(2),c.getInt(3),c.getInt(6)!=0));
                }
            }
        });
        return  products;
    }

    public static void updateShoppingListProduct(final UUID shoppingListId, final BuyerShoppingListItem product){
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c = db.rawQuery("select Name from ShoppingListItems where ShoppingListId = ? and Code = ?",new String[]{String.valueOf(shoppingListId),String.valueOf(product.getCode())});
                if (c.moveToNext()){
                    db.execSQL("Update ShoppingListItems Set WasCollected=? where ShoppingListId = ? and Code = ?",new Object[]{product.wasCollected()?1:0,shoppingListId,product.getCode()});
                }
            }
        });
    }

    public static void updateMyShoppingListsInfo(final Long lastLocalUpdateTS){
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c = db.rawQuery("select Id from MyShoppingLists limit 1",new String[]{});
                if (c.moveToNext()){
                    db.execSQL("Update MyShoppingLists Set LastLocalUpdateTimeStamp = ifnull(?,LastLocalUpdateTimeStamp)",new Object[]{lastLocalUpdateTS});
                }
            }
        });
    }
}
