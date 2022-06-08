package abdulahad.imfast.io.sarcasmtrackboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class FavouriteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<AudioObject>>  {

    private static final String LOG_TAG = FavouriteActivity.class.getSimpleName();

    private Toolbar mToolbar;

    private ArrayList<AudioObject> mSoundList = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private Adapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        mToolbar = (Toolbar) findViewById(R.id.favorite_toolbar);

        setSupportActionBar(mToolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.favoriteRecyclerView);

        mLayoutManager = new GridLayoutManager(this, 3);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerAdapter = new Adapter(this, mSoundList);

        mRecyclerView.setAdapter(mRecyclerAdapter);

        getSupportLoaderManager().initLoader(R.id.favorites_soundlist_loader_id, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.fav_toolbar_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_favorite_hide) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventHandlerClass.releaseMediaPlayer();
    }

    public void refreshSoundList() {

        getSupportLoaderManager().restartLoader(R.id.favorites_soundlist_loader_id, null, this);
    }

    @NonNull
    @Override
    public Loader<ArrayList<AudioObject>> onCreateLoader(int id, @Nullable Bundle args) {
        return new AudioListLoader(getApplicationContext()) {

            @Override
            public ArrayList<AudioObject> loadInBackground() {
                return DatabaseHandler.getInstance(FavouriteActivity.this).getFavorites();
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
    }
}