package abdulahad.imfast.io.sarcasmtrackboard;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EventHandlerClass {

    private static final String LOG_TAG = EventHandlerClass.class.getSimpleName();

    private static MediaPlayer mp;

    private static final int PERMISSIONS_REQUEST_WRITE_STORAGE = 0;

    public static void startMediaPlayer(Context context, Integer soundId) {

        try {

            if (soundId != null) {

                if (mp != null) {
                    mp.reset();
                }

                mp = MediaPlayer.create(context, soundId);
                mp.start();
            }
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "MediaPlayer is in an invalid state for start: " + e.getMessage());
        }
    }

    public static void releaseMediaPlayer() {

        if (mp != null) {

            mp.release();
            mp = null;
        }
    }

    private static boolean storagePermissionGranted(Context context) {

        return (ContextCompat
                .checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
    }

    private static boolean settingsPermissionGranted(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            return Settings.System.canWrite(context);
        }
        return true;
    }

    public static void popupManager(View view, final AudioObject soundObject) {

        final Context context = view.getContext();

        PopupMenu popup = new PopupMenu(context, view);

        if (context instanceof FavouriteActivity) {
            popup.getMenuInflater().inflate(R.menu.favo_longclick, popup.getMenu());
        } else {
            popup.getMenuInflater().inflate(R.menu.longclick, popup.getMenu());
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.action_send || item.getItemId() == R.id.action_ringtone) {

                    if (!storagePermissionGranted(context)) {

                        Toast.makeText(context, R.string.perm_write_storage_error, Toast.LENGTH_SHORT)
                                .show();
                        return true;
                    }

                    final String fileName = soundObject.getItemName() + ".mp3";

                    File storage = Environment.getExternalStorageDirectory();

                    File directory = new File(storage.getAbsolutePath() + "/my_trackboard/");

                    directory.mkdirs();

                    final File file = new File(directory, fileName);

                    InputStream in = null;
                    OutputStream out = null;

                    try {

                        in = context.getResources().openRawResource(soundObject.getItemId());

                        Log.i(LOG_TAG, "Saving sound " + soundObject.getItemName());

                        out = new FileOutputStream(file);

                        byte[] buffer = new byte[1024];

                        int len;

                        while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                            out.write(buffer, 0, len);
                        }

                    } catch (FileNotFoundException e) {

                        Log.e(LOG_TAG, "Failed to find file: " + e.getMessage());

                    } catch (IOException e) {

                        Log.e(LOG_TAG, "Failed to save file: " + e.getMessage());

                    } finally {

                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Failed to close InputStream: " + e.getMessage());
                        }

                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Failed to close OutputStream: " + e.getMessage());
                        }
                    }

                    if (item.getItemId() == R.id.action_send) {

                        try {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {

                                String authority = context.getPackageName() + ".fileprovider";

                                Uri contentUri = FileProvider.getUriForFile(context, authority, file);

                                final Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.putExtra(Intent.EXTRA_STREAM, contentUri);

                                intent.setType("audio/mp3");

                                context.startActivity(
                                        Intent.createChooser(intent,
                                                context.getResources()
                                                        .getString(R.string.share_sound_title)));
                            } else {

                                final Intent intent = new Intent(Intent.ACTION_SEND);

                                Uri fileUri = Uri.parse(file.getAbsolutePath());
                                intent.putExtra(Intent.EXTRA_STREAM, fileUri);

                                intent.setType("audio/mp3");

                                context.startActivity(
                                        Intent.createChooser(intent,
                                                context.getResources()
                                                        .getString(R.string.share_sound_title)));
                            }

                        } catch (IllegalArgumentException | NullPointerException e) {

                            Log.e(LOG_TAG, "Failed to share sound: " + e.getMessage());
                        }
                    }

                    if (item.getItemId() == R.id.action_ringtone) {

                        if (!settingsPermissionGranted(context)) {

                            Toast.makeText(context, R.string.perm_write_settings_error, Toast.LENGTH_SHORT)
                                    .show();

                            return true;

                        }

                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(context, R.style.popup_theme);

                        builder.setTitle("Set as...");
                        builder.setItems(new CharSequence[]{"Ringtone", "Notification", "Alarm"},
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        switch (which) {

                                            case 0:

                                                changeSystemAudio(context, RingtoneManager.TYPE_RINGTONE, file);
                                                break;

                                            case 1:

                                                changeSystemAudio(context, RingtoneManager.TYPE_NOTIFICATION, file);
                                                break;

                                            case 2:

                                                changeSystemAudio(context, RingtoneManager.TYPE_ALARM, file);
                                                break;

                                            default:
                                        }
                                    }
                                });
                        builder.create();
                        builder.show();
                    }

                }

                if (item.getItemId() == R.id.action_favorite) {

                    DatabaseHandler databaseHandler = DatabaseHandler
                            .getInstance(context.getApplicationContext());

                    if (context instanceof FavouriteActivity) {

                        databaseHandler.removeFavorite(context, soundObject);

                    } else {

                        databaseHandler.addFavorite(soundObject);

                    }
                }

                return true;
            }
        });

        popup.show();
    }

    private static void changeSystemAudio(Context context, int type, File file) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, file.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.ARTIST, "HandOfBlood");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        values.put(MediaStore.Audio.Media.IS_ALARM, true);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);
        values.put(MediaStore.Audio.Media.IS_PODCAST, false);

        final Uri baseUri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
        Uri toneUri = getUriForExistingTone(context, baseUri, file.getAbsolutePath());
        if (toneUri == null) {
            toneUri = context.getContentResolver().insert(baseUri, values);
        }
        RingtoneManager.setActualDefaultRingtoneUri(context, type, toneUri);
    }

    private static Uri getUriForExistingTone(Context context, Uri uri, String filePath) {

        Cursor cursor = null;
        try {

            cursor = context.getContentResolver()
                    .query(uri,
                            new String[] {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA},
                            MediaStore.MediaColumns.DATA + " = ?",
                            new String[] {filePath},
                            null, null);

            if (cursor != null && cursor.getCount() != 0) {

                cursor.moveToFirst();
                int mediaPos = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                return Uri.parse(uri.toString() + "/" + mediaPos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
