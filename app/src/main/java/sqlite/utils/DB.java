package sqlite.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DB {

    public static void Operate(DBOperation operation){
        SQLiteDatabase db=DatabaseManager.getInstance().openDatabase();
        try
        {
            operation.Operate(db);
        }
        finally
        {
            DatabaseManager.getInstance().closeDatabase();
        }
    }

    public static void RunTransaction(final DBTransaction tx){

        DB.Operate(new DBOperation() {
            @Override
            public void Operate(SQLiteDatabase db) {
                DB.RunTransaction(db,tx);
            }
        });
    }


    public static void RunTransaction(SQLiteDatabase db,DBTransaction tx){

        try
        {	db.execSQL("BEGIN IMMEDIATE TRANSACTION");
            tx.Operate(db);
        }
        finally
        {
            db.execSQL("COMMIT TRANSACTION");
        }
    }

    public static void Exec(final String sqlCommand){
        DB.RunTransaction(new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                db.rawQuery(sqlCommand,new String[]{});
            }
        });
    }

}
