package com.example.mp3player;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class Edit_Playlist extends AppCompatActivity {

    private int playList_ix;
    private ActivityResultLauncher<Intent> edit_launcher;
    private ActivityResultLauncher<Intent> permission_launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_playlist);

        Intent tmp = getIntent();
        boolean editing = tmp.getBooleanExtra("editing", false);

        if(editing)  //if editing an existing playlist
        {
            String plName = tmp.getStringExtra("playlist_name");
            playList_ix = searchPlaylist(plName);
            fillDisplay(MainActivity.list_of_playLists.get(playList_ix));
        }
        else        // creating a new playlist
        {

        }
    }

    protected int searchPlaylist(String plName)
    {
        for(int i = 0; i < MainActivity.list_of_playLists.size(); i++)
        {
            if(MainActivity.list_of_playLists.get(i).playListName.equals(plName))
            {
                return i;
            }
        }

        return -1;
    }

    public void fillDisplay(PlayList pl)
    {

        EditText ed = findViewById(R.id.edit_playlist_playListName);
        ed.setText(pl.playListName);
        createAdapter();
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
}