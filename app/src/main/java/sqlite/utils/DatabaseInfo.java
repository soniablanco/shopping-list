package sqlite.utils;

import java.util.ArrayList;

public abstract class DatabaseInfo {



    public abstract String GetName();

    public abstract ArrayList<SQLVersionExecutor> GetDatabaseStructure();
}
