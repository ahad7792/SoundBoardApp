package abdulahad.imfast.io.sarcasmtrackboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class MainActivity
        extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<AudioObject>> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private Toolbar mToolbar;

    private ArrayList<AudioObject> mSoundList = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private Adapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private View mLayout;

    private DatabaseHandler mDatabaseHandler;

    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        mDatabaseHandler = DatabaseHandler.getInstance(this);

        if (appUpdate()) {

            mDatabaseHandler.createSoundCollection();

            mDatabaseHandler.updateFavorites();
        }

        mLayout = findViewById(R.id.activity_trackboard);

        mToolbar = (Toolbar) findViewById(R.id.soundboard_toolbar);

        setSupportActionBar(mToolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.audioRecyclerView);

        mLayoutManager = new GridLayoutManager(this, 4);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerAdapter = new Adapter(this, mSoundList);

        mRecyclerView.setAdapter(mRecyclerAdapter);

        requestPermissions();

        getSupportLoaderManager().initLoader(R.id.common_soundlist_loader_id, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                mRecyclerAdapter.queryData(query);
                return true;

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {

                getSupportLoaderManager()
                        .restartLoader(R.id.common_soundlist_loader_id,
                                null,
                                MainActivity.this);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_favorite_show) {
            this.startActivity(new Intent(this, FavouriteActivity.class));
        }

        switch (item.getItemId()) {

            case R.id.action_favorite:
                startActivity(new Intent(this, FavouriteActivity.class));
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventHandlerClass.releaseMediaPlayer();

    }

    private void requestPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0);
            }

            if (!Settings.System.canWrite(this)) {

                Snackbar.make(mLayout, "The app needs access to your settings", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Context context = v.getContext();
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                }).show();
            }

        }
    }

    private boolean appUpdate() {

        final String prefsName = "VersionPref";
        final String prefVersionCodeKey = "version_code";

        final int doesntExist = -1;

        int currentVersionCode = 0;
        try {

            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

        } catch (PackageManager.NameNotFoundException e) {

            Log.e(LOG_TAG, e.getMessage());
        }

        SharedPreferences prefs = getSharedPreferences(prefsName, MODE_PRIVATE);

        int savedVersionCode = prefs.getInt(prefVersionCodeKey, doesntExist);

        SharedPreferences.Editor edit = prefs.edit();

        if (savedVersionCode == doesntExist) {

            mDatabaseHandler.appUpdate();

            edit.putInt(prefVersionCodeKey, currentVersionCode);
            edit.apply();
            return true;
        } else if (currentVersionCode > savedVersionCode) {

            mDatabaseHandler.appUpdate();
            edit.putInt(prefVersionCodeKey, currentVersionCode);
            edit.apply();
            return true;
        }

        return false;
    }

    @NonNull
    @Override
    public Loader<ArrayList<AudioObject>> onCreateLoader(int id, @Nullable Bundle args) {
        return new AudioListLoader(getApplicationContext()) {

            @Override
            public ArrayList<AudioObject> loadInBackground() {
                return DatabaseHandler.getInstance(MainActivity.this).getSoundCollection();
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<AudioObject>> loader, ArrayList<AudioObject> data) {

        mRecyclerAdapter.swapData(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<AudioObject>> loader) {
        mRecyclerAdapter.swapData(new ArrayList<AudioObject>());
    }}