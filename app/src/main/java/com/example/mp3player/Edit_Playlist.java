package com.example.mp3player;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private PlayList editingPlaylist;
    private PlayList deepCopy;
    private ActivityResultLauncher<Intent> edit_launcher;
    private ActivityResultLauncher<Intent> permission_launcher;

    private EditSongAdapter adapter;
    private ArrayList<Song> adapterDisplayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_playlist);

        Intent tmp = getIntent();
        boolean editing = tmp.getBooleanExtra("editing", false);
        adapterDisplayList = new ArrayList<Song>();

        if (editing)  //if editing an existing playlist
        {
            String plName = tmp.getStringExtra("playlist_name");
            playList_ix = searchPlaylist(plName);
            editingPlaylist = MainActivity.list_of_playLists.get(playList_ix);

            deepCopy = new PlayList();

            deepCopy.playListName = editingPlaylist.playListName;
            deepCopy.checked = editingPlaylist.checked;

            for (Song s : editingPlaylist.songList) {
                Song newSong = new Song();
                newSong.name = s.name;
                newSong.path = s.path;

                deepCopy.songList.add(newSong);
                adapterDisplayList.add(newSong);
            }

            fillDisplay(deepCopy);
        }

        //TODO: Support adding a playlist



        edit_launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent resultingIntent = result.getData();
                        String songName = resultingIntent.getStringExtra("songName");
                        String songRoute = resultingIntent.getStringExtra("songRoute");
                        boolean editing = resultingIntent.getBooleanExtra("editing", false);

                        if (!editing) {
                            Song addition = new Song();
                            addition.name = songName;
                            addition.path = songRoute;

                            deepCopy.songList.add(addition);
                        }

                        //update the list
                        createAdapter();


                    }
                }
        );

        initComponents();

        //----------------------------------DEBUG--------------------------------------------
        Log.d("Edit_Playlist_deepCopy", "deep copy name is " + deepCopy.playListName +
                " and size is " + deepCopy.songList.size());

        for (int i = 0; i < deepCopy.songList.size(); i++) {
            Log.d("Edit_Playlist_deepCopy", "(" + i + "): " + deepCopy.songList.get(i).name );

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

    public void initComponents()
    {
        initButtons();
        EditText searchBar = findViewById(R.id.edit_playlist_search_bar);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterDisplayList(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void filterDisplayList(String s)
    {
        adapterDisplayList.clear();

        adapterDisplayList.addAll(deepCopy.songList);
        adapterDisplayList.removeIf(song -> s.length() > 0 && !song.name.toLowerCase().contains(s));

        adapter.notifyDataSetChanged();
    }

    public void initButtons()
    {
        ImageButton addBtn = findViewById(R.id.edit_playlist_add_song);
        ImageButton deleteBtn = findViewById(R.id.edit_playlist_trash_btn);
        Button cancelBtn = findViewById(R.id.edit_playlist_cancel);
        Button confirmBtn = findViewById(R.id.edit_playlist_confirm);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSong_click(view);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: need to update the song buttons to support selecting mutliple
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //save the updated playlist
                MainActivity.list_of_playLists.set(playList_ix, deepCopy);
                MainActivity.handler.writeToJSON(MainActivity.list_of_playLists);
                finish();
            }
        });
    }

    public void fillDisplay(PlayList pl)
    {
        EditText ed = findViewById(R.id.edit_playlist_playListName);
        ed.setText(pl.playListName);
        createAdapter();
    }

    protected void createAdapter()
    {
//        //refresh the display list with a deep copy of the selected playlist
//        adapterDisplayList = deepCopy.songList;

        RecyclerView list = findViewById(R.id.edit_music_list);

        adapter = new EditSongAdapter(this, adapterDisplayList);

        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(this));

    }

    public void addSong_click(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
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
            } else {
                startEditIntent();
            }
        }
        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

            System.out.println("ASKING FOR PERMISSION");
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        else
        {
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

            Song song = deepCopy.songList.get(position);

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
        editingPlaylist.songList.remove(position);
        MainActivity.handler.writeToJSON(MainActivity.list_of_playLists);
        createAdapter();

    }
}