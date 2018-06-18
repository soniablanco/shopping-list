package sqlite.utils;

import android.database.sqlite.SQLiteDatabase;

public abstract  class DBTransaction {

    public abstract void Operate(SQLiteDatabase db);
}
