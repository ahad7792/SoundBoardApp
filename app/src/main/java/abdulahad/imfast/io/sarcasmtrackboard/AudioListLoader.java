package abdulahad.imfast.io.sarcasmtrackboard;

import android.content.Context;

import androidx.loader.content.AsyncTaskLoader;

import java.util.ArrayList;

public class AudioListLoader extends AsyncTaskLoader<ArrayList<AudioObject>> {

    public AudioListLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public ArrayList<AudioObject> loadInBackground() {
        return new ArrayList<AudioObject>();
    }

    @Override
    public void deliverResult(ArrayList<AudioObject> data) {
        super.deliverResult(data);
    }
}
