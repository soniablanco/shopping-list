package com.piscos.soni.shoppinglist.shoppinglist;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.piscos.soni.shoppinglist.lists.MyShoppingListsCtrlInfo;
import com.piscos.soni.shoppinglist.lists.ShoppingListElement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import sqlite.utils.DB;
import sqlite.utils.DBOperation;
import sqlite.utils.DBTransaction;

public class ShoppingListManager {

    public static  List<ShoppingListItem> getAllProducts(){
        final List<ShoppingListItem> products =  new ArrayList<>();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select Name,Code,ThumbnailPath from Products order by Name",new String[]{});
                while(c.moveToNext()) {
                    products.add(new ShoppingListItem(c.getString(0),c.getString(1),c.getString(2)));
                }
            }
        });
        return  products;
    }

    public static void createShoppingList(final ShoppingList shoppingList)  {
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                db.execSQL("insert into ShoppingLists (Name,UUID,LastLocalUpdateTimeStamp,LastSyncTimeStamp) values (?,?,?,?)",new Object[]{shoppingList.getName(), shoppingList.getId(),shoppingList.getLastUpdateTS(),shoppingList.getLastSyncTS()});
            }
        });
    }

    public static void addShoppingListProducts(final UUID shoppingListId, final List<ShoppingListItem> products){
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                db.execSQL("delete from ShoppingListItems where ShoppingListId = ?",new Object[]{shoppingListId});
                for(ShoppingListItem fb:products) {
                    db.execSQL("insert into ShoppingListItems (Name, Code, Quantity,ShoppingListId,WasCollected) values (?,?,?,?,?)", new Object[]{fb.getName(),fb.getCode(),fb.getQuantity(),shoppingListId,fb.mWasCollected});
                }
            }
        });

    }

    public static void addShoppingListProduct(final UUID shoppingListId, final ShoppingListItem product){
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c = db.rawQuery("select Name from ShoppingListItems where ShoppingListId = ? and Code = ?",new String[]{String.valueOf(shoppingListId),String.valueOf(product.getCode())});
                if (c.moveToNext()){
                    db.execSQL("Update ShoppingListItems Set Quantity = ?, WasModified =?,TimeStamp =?,WasCollected=? where ShoppingListId = ? and Code = ?",new Object[]{product.getQuantity(),1,product.getTimestamp(),product.mWasCollected?1:0,shoppingListId,product.getCode()});
                }
                else {
                    db.execSQL("insert into ShoppingListItems (Name, Code, Quantity,ShoppingListId,WasModified,TimeStamp,WasCollected) values (?,?,?,?,?,?,?)", new Object[]{product.getName(),product.getCode(),product.getQuantity(),shoppingListId,1,product.getTimestamp(),product.mWasCollected?1:0});
                }
                //db.execSQL("Update ShoppingList Set Quantity = ?, WasModified =?,TimeStamp =? where ShoppingListId = ? and Code = ?",new Object[]{product.getQuantity(),product.mUpdated,product.getTimestamp(),shoppingListId,product.getCode()});
            }
        });
    }


    public static void deleteShoppingListProduct(final UUID shoppingListId, final ShoppingListItem product){
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
               db.execSQL("delete from ShoppingListItems where ShoppingListId = ? and Code = ?",new Object[]{shoppingListId, product.getCode()});
               //db.execSQL("Update ShoppingListItems Set Quantity = ?, WasModified =?,TimeStamp =? where ShoppingListId = ? and Code = ?",new Object[]{-1,1,product.getTimestamp(),shoppingListId,product.getCode()});
            }
        });
    }

    public static ShoppingList getShoppingList(final UUID shoppingListId){
        final List<ShoppingList> list = new ArrayList<>();// = new ShoppingList();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select Name,LastLocalUpdateTimeStamp,LastSyncTimeStamp from ShoppingLists where UUID = ?",new String[]{String.valueOf(shoppingListId)});
                if(c.moveToFirst()) {
                   list.add(new ShoppingList(shoppingListId,c.getString(0),c.getLong(1),c.getLong(2)));
                }
            }
        });
        return list.get(0);
    }

    public static void updateShoppingList(final ShoppingList shoppingList){
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                    db.execSQL("Update ShoppingLists Set Name=?,LastLocalUpdateTimeStamp=ifnull(?,LastLocalUpdateTimeStamp), LastSyncTimeStamp=ifnull(?,LastSyncTimeStamp) where UUID = ?",new Object[]{shoppingList.getName(),shoppingList.getLastUpdateTS(),shoppingList.getLastSyncTS(),shoppingList.getId()});
            }
        });
    }

    public static List<ShoppingListItem> getShoppingListProducts(final UUID shoppingListId){
        final List<ShoppingListItem> products =  new ArrayList<>();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select Products.Name,Products.Code,Products.ThumbnailPath, ifnull(ShoppingListItems.Quantity,0),ShoppingListItems.TimeStamp,ShoppingListItems.WasModified,ShoppingListItems.WasCollected from Products " +
                        "left join ShoppingListItems " +
                        "on Products.Code = ShoppingListItems.Code " +
                        "and ShoppingListItems.ShoppingListId = ? order by Products.Name",new String[]{String.valueOf(shoppingListId)});
                while(c.moveToNext()) {
                    products.add(new ShoppingListItem(c.getString(0),c.getString(1),c.getString(2),c.getInt(3),c.isNull(4)? null:c.getLong(4),c.getInt(5)!=0,c.getInt(6)!=0));
                }
            }
        });
        return  products;
    }

    public static List<ShoppingListItem> getShoppingListItems(final UUID shoppingListId){
        final List<ShoppingListItem> products =  new ArrayList<>();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select ShoppingListItems.Name,ShoppingListItems.Code,Products.ThumbnailPath, ShoppingListItems.Quantity,ShoppingListItems.TimeStamp,ShoppingListItems.WasModified,ShoppingListItems.WasCollected from ShoppingListItems " +
                        "join Products on  Products.Code = ShoppingListItems.Code " +
                        "where ShoppingListItems.ShoppingListId = ? AND ShoppingListItems.Quantity > ? order by ShoppingListItems.Name",new String[]{String.valueOf(shoppingListId),"0"});
                while(c.moveToNext()) {
                    products.add(new ShoppingListItem(c.getString(0),c.getString(1),c.getString(2),c.getInt(3),c.isNull(4)? null:c.getLong(4),c.getInt(5)!=0,c.getInt(6)!=0));
                }
            }
        });
        return  products;
    }

    public static List<ShoppingListElement> getAllShoppingLists(){
        final List<ShoppingListElement> shoppingLists =  new ArrayList<>();
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("Select ShoppingLists.Id,ShoppingLists.Name,(Select Count(ShoppingListId) from ShoppingListItems where ShoppingListId =ShoppingLists.UUID),ShoppingLists.UUID from ShoppingLists order by ShoppingLists.Name",new String[]{});
                while(c.moveToNext()) {
                    shoppingLists.add(new ShoppingListElement(c.getInt(0),c.getString(1),c.getInt(2),UUID.fromString(c.getString(3))));
                }
            }
        });
        return shoppingLists;
    }

    public static void deleteShoppingList(final UUID shoppingListId){
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                db.execSQL("delete from ShoppingListItems where ShoppingListId = ?",new Object[]{shoppingListId});
                db.execSQL("delete from ShoppingLists where UUID = ?",new Object[]{shoppingListId});
            }
        });
    }

     public static void updateShoppingListProduct(final UUID shoppingListId, final ShoppingListItem product){
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c = db.rawQuery("select Name from ShoppingListItems where ShoppingListId = ? and Code = ?",new String[]{String.valueOf(shoppingListId),String.valueOf(product.getCode())});
                if (c.moveToNext()){
                    db.execSQL("Update ShoppingListItems Set Quantity = ?, WasModified =?,TimeStamp =? where ShoppingListId = ? and Code = ?",new Object[]{product.getQuantity(),product.mUpdated,product.getTimestamp(),shoppingListId,product.getCode()});
                }
            }
        });
    }

    public static void updateMyShoppingListsInfo(final Long lastLocalUpdateTS, final Long lastSyncTS ){
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c = db.rawQuery("select Id from MyShoppingLists limit 1",new String[]{});
                if (c.moveToNext()){
                    db.execSQL("Update MyShoppingLists Set LastLocalUpdateTimeStamp = ifnull(?,LastLocalUpdateTimeStamp), LastSyncTimeStamp =ifnull(?,LastSyncTimeStamp)",new Object[]{lastLocalUpdateTS,lastSyncTS});
                }
                else {
                    db.execSQL("insert into MyShoppingLists (LastLocalUpdateTimeStamp, LastSyncTimeStamp) values (?,?)", new Object[]{lastLocalUpdateTS,lastSyncTS});
                }
                //db.execSQL("Update ShoppingList Set Quantity = ?, WasModified =?,TimeStamp =? where ShoppingListId = ? and Code = ?",new Object[]{product.getQuantity(),product.mUpdated,product.getTimestamp(),shoppingListId,product.getCode()});
            }
        });
    }

    public static MyShoppingListsCtrlInfo getMyShoppingListsInfo(){
        final MyShoppingListsCtrlInfo shoppingListsInfo[] =  new MyShoppingListsCtrlInfo[1];
        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                Cursor c=db.rawQuery("select LastLocalUpdateTimeStamp, LastSyncTimeStamp from MyShoppingLists limit 1",new String[]{});
                if(c.moveToFirst()) {
                    shoppingListsInfo[0] =new MyShoppingListsCtrlInfo(c.isNull(0)? null:c.getLong(0),c.isNull(1)? null:c.getLong(1));
                }
            }
        });
        return shoppingListsInfo[0] == null ? new MyShoppingListsCtrlInfo():shoppingListsInfo[0];
    }

}
