package abdulahad.imfast.io.sarcasmtrackboard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import abdulahad.imfast.io.sarcasmtrackboard.DatabaseScheme.MainTable;
import abdulahad.imfast.io.sarcasmtrackboard.DatabaseScheme.FavoritesTable;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String LOG_TAG = DatabaseHandler.class.getSimpleName();

    private static DatabaseHandler instance = null;

    private Context context;

    private static final String DATABASE_NAME = "soundboard.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_MAIN_TABLE = "CREATE TABLE IF NOT EXISTS "
            + MainTable.TABLE_NAME + "("
            + MainTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MainTable.NAME + " TEXT, "
            + MainTable.RESOURCE_ID + " INTEGER unique);";

    private static final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE IF NOT EXISTS "
            + FavoritesTable.TABLE_NAME + "("
            + FavoritesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + FavoritesTable.NAME + " TEXT, "
            + FavoritesTable.RESOURCE_ID + " INTEGER);";

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(LOG_TAG, "Database successfully initialised: " + getDatabaseName());

        this.context = context;
    }

    public static DatabaseHandler getInstance(Context context) {

        if (instance == null) {
            return new DatabaseHandler(context.getApplicationContext());
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {

            db.execSQL(SQL_CREATE_MAIN_TABLE);
            db.execSQL(SQL_CREATE_FAVORITES_TABLE);

        } catch (SQLException e) {
            Log.e(LOG_TAG, "Failed to create tables: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + MainTable.TABLE_NAME);
        onCreate(db);

    }

    public void createSoundCollection() {

        String[] nameList = context.getResources().getStringArray(R.array.audioNames);

        Integer[] soundIDs = {R.raw.akash_bora_tara, R.raw.ripon_hajar_salam, R.raw.hi_friends,
                R.raw.jole_jokhon_nemechi, R.raw.kemon_dilam, R.raw.kibolo_bondhura,
                R.raw.injoy_anondo, R.raw.ei_tui_ki_bolli, R.raw.ghumate_parina,
                R.raw.kukur_child, R.raw.mara_kha, R.raw.ore_batpar,
                R.raw.hisab_bujona, R.raw.ore_cheater, R.raw.lal_kore_dibo,
                R.raw.etai_science, R.raw.chad_utechilo_gogone, R.raw.r_kotokal,
                R.raw.sobar_ongko_mile, R.raw.baincod, R.raw.thikache_bondhura
        };

        ArrayList<AudioObject> soundItems = new ArrayList<>();

        for (int i = 0; i < soundIDs.length; i++) {

            soundItems.add(new AudioObject(nameList[i], soundIDs[i]));

        }

        for (AudioObject i : soundItems) {

            putIntoMain(i);

        }
    }

    private boolean verification(SQLiteDatabase database, AudioObject soundObject) {

        int count = -1;
        Cursor cursor = null;

        try {

            cursor = database.query(FavoritesTable.TABLE_NAME, new String[]{FavoritesTable.NAME},
                    FavoritesTable.RESOURCE_ID + "=?",
                    new String[]{soundObject.getItemId().toString()},
                    null, null, null);

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Cursor is a NullPointer: " + e.getMessage());
        } finally {

            if (cursor != null) {
                cursor.close();

            }
        }

        return (count > 0);

    }

    private void putIntoMain(AudioObject soundObject) {

        SQLiteDatabase database = this.getWritableDatabase();

        try {

            ContentValues contentValues = new ContentValues();

            contentValues.put(MainTable.NAME, soundObject.getItemName());
            contentValues.put(MainTable.RESOURCE_ID, soundObject.getItemId());

            database.insertOrThrow(MainTable.TABLE_NAME, null, contentValues);

        } catch (SQLException e) {

            Log.e(LOG_TAG, "(MAIN) Failed to insert sound: " + e.getMessage());

        }
    }

    public ArrayList<AudioObject> getSoundCollection() {

        SQLiteDatabase database = this.getReadableDatabase();

        ArrayList<AudioObject> soundObjects = new ArrayList<>();

        Cursor cursor = null;

        try {

            cursor = database.query(MainTable.TABLE_NAME,
                    new String[]{MainTable.NAME, MainTable.RESOURCE_ID}, null, null,
                    null, null, MainTable.NAME);

            if (cursor.getCount() != 0) {

                while (cursor.moveToNext()) {

                    String name = cursor.getString(cursor.getColumnIndex(MainTable.NAME));
                    Integer resId = cursor.getInt(cursor.getColumnIndex(MainTable.RESOURCE_ID));

                    soundObjects.add(new AudioObject(name, resId));

                }

            } else {

                Log.d(LOG_TAG, "Failed to convert data");

            }

        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Cursor is a NullPointer: " + e.getMessage());
        } finally {

            if (cursor != null) {
                cursor.close();

            }
        }

        return soundObjects;
    }

    public ArrayList<AudioObject> getSoundCollectionFromQuery(String queryString) {

        SQLiteDatabase database = this.getReadableDatabase();

        ArrayList<AudioObject> soundObjects = new ArrayList<>();

        Cursor cursor = null;

        try {

            cursor = database.query(MainTable.TABLE_NAME,
                    new String[]{MainTable.NAME, MainTable.RESOURCE_ID},
                    MainTable.NAME + " LIKE ?",
                    new String[]{queryString.toLowerCase() + "%"},
                    null,
                    null,
                    MainTable.NAME);

            if (cursor.getCount() != 0) {

                while (cursor.moveToNext()) {

                    String name = cursor.getString(cursor.getColumnIndex(MainTable.NAME));
                    Integer resId = cursor
                            .getInt(cursor.getColumnIndex(MainTable.RESOURCE_ID));

                    soundObjects.add(new AudioObject(name, resId));
                }

            } else {

                Log.d(LOG_TAG, "Failed to convert data");
            }

        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Cursor is a NullPointer: " + e.getMessage());
        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return soundObjects;
    }

    public void addFavorite(AudioObject soundObject) {

        SQLiteDatabase database = this.getWritableDatabase();

        if (!verification(database, soundObject)) {

            try {

                ContentValues contentValues = new ContentValues();

                contentValues.put(FavoritesTable.NAME, soundObject.getItemName());
                contentValues.put(FavoritesTable.RESOURCE_ID, soundObject.getItemId());

                database.insertOrThrow(FavoritesTable.TABLE_NAME, null, contentValues);

            } catch (SQLException e) {
                Log.e(LOG_TAG, "(FAVORITES) Failed to insert sound: " + e.getMessage());
            }
        }
    }

    public void removeFavorite(Context context, AudioObject soundObject) {

        SQLiteDatabase database = this.getWritableDatabase();

        if (database.delete(FavoritesTable.TABLE_NAME, FavoritesTable.RESOURCE_ID + "=?",
                new String[]{Integer.toString(soundObject.getItemId())}) != 0) {

            if (context instanceof FavouriteActivity) {

                ((FavouriteActivity) context).refreshSoundList();

            }
        }
    }

    public ArrayList<AudioObject> getFavorites() {

        SQLiteDatabase database = this.getReadableDatabase();

        ArrayList<AudioObject> soundObjects = new ArrayList<>();

        Cursor cursor = null;

        try {

            cursor = database.query(FavoritesTable.TABLE_NAME,
                    new String[]{FavoritesTable.NAME, FavoritesTable.RESOURCE_ID},
                    null,
                    null,
                    null,
                    null,
                    FavoritesTable.NAME);

            if (cursor.getCount() != 0) {

                while (cursor.moveToNext()) {

                    String name = cursor.getString(cursor.getColumnIndex(FavoritesTable.NAME));
                    Integer resId = cursor
                            .getInt(cursor.getColumnIndex(FavoritesTable.RESOURCE_ID));

                    soundObjects.add(new AudioObject(name, resId));

                }

            } else {

                Log.d(LOG_TAG, "Failed to convert data");
            }

        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Cursor is a NullPointer: " + e.getMessage());
        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return soundObjects;

    }

    public void updateFavorites() {

        SQLiteDatabase database = this.getWritableDatabase();

        Cursor favoriteContent = null;
        Cursor updateEntry = null;

        try {

            favoriteContent = database.query(FavoritesTable.TABLE_NAME,
                    new String[]{FavoritesTable.NAME,
                            FavoritesTable.RESOURCE_ID},
                    null,
                    null,
                    null,
                    null,
                    null);

            if (favoriteContent.getCount() == 0) {

                Log.d(LOG_TAG, "Cursor is empty or failed to convert data");
                favoriteContent.close();
                return;
            }


            while (favoriteContent.moveToNext()) {

                String entryName = favoriteContent
                        .getString(favoriteContent.getColumnIndex(FavoritesTable.NAME));

                updateEntry = database.rawQuery(
                        "SELECT * FROM " + MainTable.TABLE_NAME + " WHERE "
                                + MainTable.NAME + " = '" + entryName + "'",
                        null);

                if (updateEntry.getCount() == 0) {

                    Log.d(LOG_TAG, "Cursor is empty or failed to convert data");
                    updateEntry.close();
                    return;
                }

                updateEntry.moveToFirst();

                if (favoriteContent.getInt(favoriteContent.getColumnIndex(FavoritesTable.RESOURCE_ID))
                        != updateEntry.getInt(updateEntry.getColumnIndex(MainTable.RESOURCE_ID))) {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(FavoritesTable.RESOURCE_ID,
                            updateEntry.getInt(updateEntry.getColumnIndex(MainTable.RESOURCE_ID)));

                    database.update(FavoritesTable.TABLE_NAME,
                            contentValues,
                            FavoritesTable.NAME + "=?",
                            new String[]{entryName});
                }
            }
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Cursor is a NullPointer: " + e.getMessage());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to update favorites: " + e.getMessage());
        } finally {

            if (favoriteContent != null) {
                favoriteContent.close();
            }

            if (updateEntry != null) {
                updateEntry.close();
            }
        }
    }

    public void appUpdate() {

        try {

            SQLiteDatabase database = this.getWritableDatabase();

            database.execSQL("DROP TABLE IF EXISTS " + MainTable.TABLE_NAME);

            database.execSQL(SQL_CREATE_MAIN_TABLE);


        } catch (SQLException e) {
            Log.e(LOG_TAG, "Failed to update the main table on app update: " + e.getMessage());
        }
    }

}
