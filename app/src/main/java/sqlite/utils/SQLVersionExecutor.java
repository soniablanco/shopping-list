package sqlite.utils;

import android.database.sqlite.SQLiteDatabase;

public abstract  class SQLVersionExecutor {
    public abstract void Exec(SQLiteDatabase db);
}
