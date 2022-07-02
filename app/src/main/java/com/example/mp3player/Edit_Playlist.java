package com.example.mp3player;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Edit_Playlist extends AppCompatActivity {

    private int playList_ix;
    private ActivityResultLauncher<Intent> edit_launcher;
    private ActivityResultLauncher<Intent> permission_launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_playlist);

        Intent tmp = getIntent();
        playList_ix = tmp.getIntExtra("playlist_index", -1);

//        Intent intent = getIntent();
//        int ix = intent.getIntExtra("playlist_index", -1);

        EditText name = findViewById(R.id.playListName);
        PlayList playList;

        if(playList_ix == -1)
        {
            name.setText("Could not find song");
            playList = null;
        }
        else
        {
            playList = MainActivity.list_of_playLists.get(playList_ix);
            name.setText(playList.playListName);
        }

        createAdapter();

        ImageButton backBtn = (ImageButton) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
//                System.out.println("I want to go back now.");
//                Toast.makeText(getApplicationContext(), "I want to go back now", Toast.LENGTH_LONG).show();
                playList.playListName = name.getText().toString();
                //MainActivity.handler.writeToJSON(MainActivity.list_of_playLists);
                //copy any changes made to the music list (ListView) to the arrayList

                finish();
            }
        });

        //setting up the launcher for the adding a new song activity
        edit_launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == RESULT_OK)
                        {
                            Intent data = result.getData();

                            Song addition = new Song();
                            addition.name = data.getStringExtra("songName");
                            addition.path = data.getStringExtra("songRoute");

                            //add the song to the playlist and write the changes to the JSON
                            MainActivity.list_of_playLists.get(playList_ix).songList.add(addition);
                            MainActivity.handler.writeToJSON(MainActivity.list_of_playLists);

                            System.out.println("Playlist info");

                            for(int i = 0; i < MainActivity.list_of_playLists.get(playList_ix).songList.size(); i++)
                            {
                                System.out.println("\t" + MainActivity.list_of_playLists.get(playList_ix).songList.get(i).name);
                                System.out.println("\t" + MainActivity.list_of_playLists.get(playList_ix).songList.get(i).path);
                            }

                            //display the new changes to the list
                            createAdapter();
                        }
                    }
                }
        );

        permission_launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent sendingIntent = new Intent(Edit_Playlist.this, Detailed_Song_View.class);
                        sendingIntent.putExtra("editing", false);
                        edit_launcher.launch(sendingIntent);
                    }
                }
        );
    }

    protected void createAdapter()
    {
        ArrayList<Song> plList = MainActivity.list_of_playLists.get(playList_ix).songList;
        Song[] tempList = new Song[plList.size()];

        for(int i = 0; i < plList.size(); i++)
        {
            tempList[i] = plList.get(i);
        }

        RecyclerView list = findViewById(R.id.edit_music_list);

        EditSongAdapter adapter = new EditSongAdapter(this, tempList);

        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(this));

    }

    public void addSong_click(View view)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!Environment.isExternalStorageManager())
            {
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                AlertDialog.Builder popup = new AlertDialog.Builder(this);
                popup.setMessage("Please allow this app to manage your files.");
                popup.setTitle("Permissions");
                popup.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAffinity();
                        startActivity(permissionIntent);
                    }
                });
                AlertDialog alert = popup.create();
                alert.show();
            }
            else
            {
                startEditIntent();
//                Intent sendingIntent = new Intent(this, Detailed_Song_View.class);
//                sendingIntent.putExtra("editing", false);
//                edit_launcher.launch(sendingIntent);
            }
        }
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                System.out.println("ASKING FOR PERMISSION");
                requestPermissions( new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },  1);
            }
            else
            {
                startEditIntent();
            }
        }
        else {
            startEditIntent();
        }
    }

    private void startEditIntent()
    {
        Intent sendingIntent = new Intent(this, Detailed_Song_View.class);
        sendingIntent.putExtra("editing", false);
        edit_launcher.launch(sendingIntent);
    }

    public void editSong (int position)
    {
        System.out.println("IN EDIT SONG");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!Environment.isExternalStorageManager())
            {
                System.out.println("ASKING FOR PERMISSION");
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(permissionIntent);
            }
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                System.out.println("ASKING FOR PERMISSION");
                requestPermissions( new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },  1);
            }
        }
        else {

            System.out.println("I GOT PERMISSION");

            Song song = MainActivity.list_of_playLists.get(playList_ix).songList.get(position);

            //sending the data to auto fill for editing
            Intent sendingIntent = new Intent(this, Detailed_Song_View.class);
            sendingIntent.putExtra("editing", true);
            sendingIntent.putExtra("songName", song.name);
            sendingIntent.putExtra("dataPath", song.path);

            edit_launcher.launch(sendingIntent);
        }
    }

    public void deleteSong(int position)
    {
        MainActivity.list_of_playLists.get(playList_ix).songList.remove(position);
        MainActivity.handler.writeToJSON(MainActivity.list_of_playLists);
        createAdapter();

    }

//    private class EditSongAdapter extends SongAdapter
//    {
//        public EditSongAdapter(Context context, int resource, List<Song> list, int id)
//        {
//            super(context, resource, list, id);
//        }
//
//        @NonNull
//        @Override
//        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//            View result = super.getView(position, convertView, parent);
//
//            ImageButton trashCan = result.findViewById(R.id.delete_song_btn);
//            ImageButton edit = result.findViewById(R.id.edit_sng_btn);
//
//            trashCan.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    //Toast.makeText(getApplicationContext(), "delete Click", Toast.LENGTH_SHORT).show();
//                    deleteSong(position);
//                }
//            });
//
//            edit.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    //Toast.makeText(getApplicationContext(), "Edit Click", Toast.LENGTH_SHORT).show();
//                    editSong(position);
//                }
//            });
//
//            return result;
//        }
//    }
}