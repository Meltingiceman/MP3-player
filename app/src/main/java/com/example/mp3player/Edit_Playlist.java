package com.example.mp3player;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Edit_Playlist extends AppCompatActivity {

    private int playList_ix;
    private PlayList deepCopy;
    private ActivityResultLauncher<Intent> edit_launcher;

    private EditSongAdapter adapter;
    private ArrayList<Song> adapterDisplayList;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_playlist);

        Intent tmp = getIntent();
        boolean editing = tmp.getBooleanExtra("editing", false);
        adapterDisplayList = new ArrayList<>();

        if (editing)  //if editing an existing playlist
        {
            String plName = tmp.getStringExtra("playlist_name");
            playList_ix = searchPlaylist(plName);
            PlayList editingPlaylist = MainActivity.list_of_playLists.get(playList_ix);

            editingPlaylist.songList.sort(new SongComparator());

            //create a deep copy of the playlist
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

            //fill display with data from the playlist
            fillDisplay(deepCopy);
        }

        edit_launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

                Intent resultingIntent = result.getData();

                boolean backPressed = (resultingIntent == null);
                if(backPressed)
                    return;

                boolean editing1 = resultingIntent.getBooleanExtra("editing", false);

                Log.d("editing", "Editing value is: " + editing1);

                if (!editing1) {
                    String songName = resultingIntent.getStringExtra("songName");
                    String songRoute = resultingIntent.getStringExtra("songRoute");

                    Song addition = new Song();
                    addition.name = songName;
                    addition.path = songRoute;

                    addSong(addition);

                    //don't know where the new item will go after being sorted
                    adapter.notifyDataSetChanged();
                }
                else
                {
                    int ix = resultingIntent.getIntExtra("songIndex", -1);
                    String newSongName = resultingIntent.getStringExtra("songName");
                    String newSongRoute = resultingIntent.getStringExtra("songRoute");

                    updateSong(ix, newSongName, newSongRoute);
                    adapter.notifyItemChanged(ix);
                }

                //update the list

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

    //initializes all the interactive components on the UI
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

    //filter the display based on what is in the searchbar
    @SuppressLint("NotifyDataSetChanged")
    private void filterDisplayList(String s)
    {
        adapterDisplayList.clear();

        adapterDisplayList.addAll(deepCopy.songList);
        adapterDisplayList.removeIf(song -> s.length() > 0 && !song.name.toLowerCase().contains(s));

        adapter.notifyDataSetChanged();
    }

    //initialize the buttons on the display
    public void initButtons()
    {
        ImageButton addBtn = findViewById(R.id.edit_playlist_add_song);
        ImageButton deleteBtn = findViewById(R.id.edit_playlist_trash_btn);
        Button cancelBtn = findViewById(R.id.edit_playlist_cancel);
        Button confirmBtn = findViewById(R.id.edit_playlist_confirm);

        //when the add button is clicked then call the addSong_click method
        addBtn.setOnClickListener(this::addSong_click);

        deleteBtn.setOnClickListener(view -> removeCheckedSongs());

        //cancel button listener
        cancelBtn.setOnClickListener(view -> finish());

        //confirm button listener
        confirmBtn.setOnClickListener(view -> {
            //save the updated playlist
            deepCopy.playListName = ((EditText)findViewById(R.id.edit_playlist_playListName)).getText().toString();

            MainActivity.list_of_playLists.set(playList_ix, deepCopy);
            MainActivity.handler.writeToJSON(MainActivity.list_of_playLists);
            finish();
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

                popup.setNegativeButton("Ok", (dialogInterface, i) -> {
                    finishAffinity();
                    startActivity(permissionIntent);
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

    //removes all the checked songs from the playlist
    private void removeCheckedSongs()
    {
        ArrayList<String> checked = adapter.getChecked();
        int songListSize = deepCopy.songList.size();

        //sort the list just to be safe
        deepCopy.songList.sort(new SongComparator());

        for(String str:checked)
        {
            int ix = binarySearch(0, songListSize - 1, str);

            if(ix != -1) {
                removeSongAt(ix);
                adapter.notifyItemRemoved(ix);
            }
        }
    }

    private void removeSongAt(int ix)
    {
        deepCopy.songList.remove(ix);
        adapterDisplayList.remove(ix);
    }

    private void addSong(Song song)
    {
        deepCopy.songList.add(song);
        adapterDisplayList.add(song);

        deepCopy.songList.sort(new SongComparator());
        adapterDisplayList.sort(new SongComparator());
    }

    private void updateSong(int index, String name, String route)
    {
        adapterDisplayList.get(index).name = deepCopy.songList.get(index).name = name;
        adapterDisplayList.get(index).path = deepCopy.songList.get(index).path = route;

        deepCopy.songList.sort(new SongComparator());
        adapterDisplayList.sort(new SongComparator());
    }

    //recursive binary search for finding a songName in the deep copy
    public int binarySearch(int left, int right, String query)
    {
        if(right >= left)
        {
            int mid = left + (right - left)/2;

            //if what we're looking for is in the middle
            if( deepCopy.songList.get(mid).isName(query))
                return mid;

            if( deepCopy.songList.get(mid).compareToIgnoreCase(query) > 0 )
                return binarySearch(left, mid - 1, query);

            return binarySearch(mid + 1, right, query);
        }

        return -1;
    }

    private void startEditIntent()
    {
        Intent sendingIntent = new Intent(this, Detailed_Song_View.class);
        sendingIntent.putExtra("editing", false);
        edit_launcher.launch(sendingIntent);
    }

    private class EditSongAdapter extends RecyclerView.Adapter<EditSongViewHolder>
    {
        ArrayList<Song> songList;
        ArrayList<String> checked;
        Context context;

        public EditSongAdapter(Context ct, ArrayList<Song> s)
        {
            context = ct;
            songList = s;
            checked = new ArrayList<>();
        }

        @NonNull
        @Override
        public EditSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.edit_pl_list_item, parent, false);

            EditSongViewHolder viewHolder = new EditSongViewHolder(view);

            view.setOnClickListener(view1 -> viewHolder.checked.setChecked(!viewHolder.checked.isChecked()));

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull EditSongViewHolder holder, int position, @NonNull List<Object> payloads) {

            super.onBindViewHolder(holder, position, payloads);

            holder.checked.setOnCheckedChangeListener(null);
            holder.checked.setChecked(checked.contains(holder.songName.getText().toString()));
            holder.checked.setOnCheckedChangeListener((compoundButton, b) -> {
                if(b)
                    checked.add(holder.songName.getText().toString());
                else
                    checked.remove(holder.songName.getText().toString());
            });

            holder.deleteBtn.setOnClickListener(view -> removeSongAt(position));

            holder.editBtn.setOnClickListener(view -> {
                Intent songEditIntent = new Intent(context, Detailed_Song_View.class);
                songEditIntent.putExtra("songName", songList.get(position).name);
                songEditIntent.putExtra("songRoute", songList.get(position).path);
                songEditIntent.putExtra("songIndex", position);
                songEditIntent.putExtra("editing", true);

                edit_launcher.launch(songEditIntent);
            });
        }

        public ArrayList<String> getChecked(){ return checked; }

        @Override
        public void onBindViewHolder(@NonNull EditSongViewHolder holder, int position) {
            holder.songName.setText( songList.get(position).name );
        }

        @Override
        public int getItemCount() {
            return songList.size();
        }
    }
}
class EditSongViewHolder extends RecyclerView.ViewHolder
{
    TextView songName;
    CheckBox checked;
    ImageButton editBtn;
    ImageButton deleteBtn;
    public EditSongViewHolder(@NonNull View itemView) {
        super(itemView);

        songName = itemView.findViewById(R.id.songName);
        checked = itemView.findViewById(R.id.edit_playlist_item_checkbox);
        editBtn = itemView.findViewById(R.id.edit_sng_btn);
        deleteBtn = itemView.findViewById(R.id.delete_song_btn);

    }
}