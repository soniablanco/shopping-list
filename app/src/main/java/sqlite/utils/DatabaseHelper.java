package sqlite.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private DatabaseInfo _databaseInfo;
    public DatabaseHelper(Context context,DatabaseInfo databaseInfo) {
        super(context, databaseInfo.GetName(), null, databaseInfo.GetDatabaseStructure().size());
        _databaseInfo=databaseInfo;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        DB.RunTransaction(db, new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                DatabaseHelper.UpdateDBImplementation(db,DatabaseHelper.this._databaseInfo.GetDatabaseStructure(),0);
            }
        });
    }

    private static void UpdateDBImplementation(SQLiteDatabase db, ArrayList<SQLVersionExecutor> versionChanges,int oldVersion){
        for (int v=oldVersion; v<versionChanges.size(); v++)
        {
            SQLVersionExecutor updater=versionChanges.get(v);
            updater.Exec(db);
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db,final int oldVersion, int newVersion) {
        DB.RunTransaction(db, new DBTransaction() {
            @Override
            public void Operate(SQLiteDatabase db) {
                DatabaseHelper.UpdateDBImplementation(db,DatabaseHelper.this._databaseInfo.GetDatabaseStructure(),oldVersion);
            }
        });
    }
}