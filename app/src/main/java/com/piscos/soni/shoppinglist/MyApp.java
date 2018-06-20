package com.piscos.soni.shoppinglist;

import android.app.Application;

import sqlite.utils.DatabaseManager;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DatabaseManager.initializeInstance(this.getApplicationContext(), new ShoppingListDatabase());

    }
}
