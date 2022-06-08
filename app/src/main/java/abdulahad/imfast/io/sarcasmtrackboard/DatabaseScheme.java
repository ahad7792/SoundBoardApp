package abdulahad.imfast.io.sarcasmtrackboard;
import android.provider.BaseColumns;

public abstract class DatabaseScheme {

    public abstract static class MainTable implements BaseColumns {

        public static final String TABLE_NAME = "main_table";
        public static final String NAME = "name";
        public static final String RESOURCE_ID = "resourceID";
    }

    public abstract static class FavoritesTable implements BaseColumns {

        public static final String TABLE_NAME = "favorites_table";
        public static final String NAME = "name";
        public static final String RESOURCE_ID = "resourceID";
    }
}
