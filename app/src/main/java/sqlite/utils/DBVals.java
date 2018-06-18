package sqlite.utils;

import android.database.Cursor;

public class DBVals {
    public static int GetInt(Cursor cursor, String columnName){
        return  cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
    }
    public static Integer GetNullableInt(Cursor cursor,String columnName){
        int columnIndex=cursor.getColumnIndexOrThrow(columnName);
        if (cursor.isNull(columnIndex)){
            return  null;
        }
        else {
            return cursor.getInt(columnIndex);
        }
    }
}
