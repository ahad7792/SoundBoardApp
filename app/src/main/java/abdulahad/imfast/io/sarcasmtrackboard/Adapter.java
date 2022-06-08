package abdulahad.imfast.io.sarcasmtrackboard;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.AudioViewHolder> {

    private ArrayList<AudioObject> audioObjects;

    private DatabaseHandler databaseHandler;

    public Adapter(Context context, ArrayList<AudioObject> audioObjects) {

        this.audioObjects = audioObjects;
        databaseHandler = DatabaseHandler.getInstance(context.getApplicationContext());

    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, null);

        return new AudioViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {

        final AudioObject audioObject = audioObjects.get(position);

        holder.itemTextView.setText(audioObject.getItemName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Context context = v.getContext();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        EventHandlerClass.startMediaPlayer(context, audioObject.getItemId());
                    }
                }).start();
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                EventHandlerClass.popupManager(v, audioObject);
                return true;

            }
        });
    }

    @Override
    public int getItemCount() {
        return audioObjects.size();
    }

    class AudioViewHolder extends RecyclerView.ViewHolder {

        private TextView itemTextView;

        AudioViewHolder(View itemView) {
            super(itemView);

            itemTextView = itemView.findViewById(R.id.button_text);

        }
    }

    public void swapData(ArrayList<AudioObject> data) {

        this.audioObjects = data;
        notifyDataSetChanged();

    }

    public void queryData(final String soundName) {

        final Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {

                final ArrayList<AudioObject> queryList =
                        databaseHandler.getSoundCollectionFromQuery(soundName);

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        swapData(queryList);
                    }
                });
            }
        }).start();
    }
}
