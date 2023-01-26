package com.example.mp3player;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
                            implements AddSongDialogFragment.AddSongDialogListener{

    public static final String playListName_key = "PLAYLIST_ADD";
    public static final String playListRoute_key = "PLAYLIST_ROUTE";
    public static ArrayList<PlayList> list_of_playLists;
    public static ArrayList<PlayList> displayList;
    public static FileHandler handler;

    private PlayListAdapter arrayAdapter;
    private ActivityResultLauncher<Intent> add_btn_launcher;
    private ActivityResultLauncher<Intent> playListClick_launcher;

    public static final String DATA_FILE_NAME = "data.json";
    public static final String DATA_FOLDER_NAME = "appData";
    public static final String MUSIC_FOLDER_NAME = "music";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new FileHandler(getFilesDir().toString());
        boolean success = handler.init();

//        if(success)
//            Toast.makeText(getApplicationContext(), "JSON created!", Toast.LENGTH_LONG).show();
//        else
//            Toast.makeText(getApplicationContext(), "JSON FAILED!", Toast.LENGTH_LONG).show();

        //load the playlist

        try {
            list_of_playLists = handler.loadPlayLists();
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Failed to load JSON data.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        //initialize the components on the UI
        initComponents();

        //create and display the musicList
        displayList();

        //DEBUG
        //displays the list of playlists
        for(int i = 0; i < list_of_playLists.size(); i++)
        {
            System.out.println("DEBUG: " + list_of_playLists.get(i).playListName);
        }

        //create the ActivityResultLauncher(s)

        //Result action of adding a new playlist
        add_btn_launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK)
                    {
                        Intent data = result.getData();
                        //Toast.makeText(getApplicationContext(), data.getStringExtra(playListName_key), Toast.LENGTH_LONG).show();

                        PlayList pList = new PlayList();
                        pList.playListName = data.getStringExtra(playListName_key);
                        list_of_playLists.add(pList);

                        handler.writeToJSON(list_of_playLists);
                        displayList();
                    }
                }
            }
        );

        //Result action for clicking a playList.
        //TODO: need to re do (or completely delete) this so it works with the new UI
        playListClick_launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK)
                    {
                        //NEED TO THINK OF OTHER THINGS TO DO HERE (IF ANY)


                        //write any changes in the playlists to the JSON
                        handler.writeToJSON(list_of_playLists);
                        displayList();
                    }
                }
        });



    }

    protected boolean initializeJson(String pathName)
    {
        JSONObject root = new JSONObject();
        JSONArray musicList = new JSONArray();
        try {
            root.put("playLists", musicList);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        try {
            FileWriter writer = new FileWriter(pathName + File.separator + DATA_FILE_NAME);
            writer.write(root.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //when the plus button is clicked
    public void add_playList_btnClick(View view)
    {

        DialogFragment newFragment = new AddSongDialogFragment();
        newFragment.show(getSupportFragmentManager(), "add_playlist");
    }

    public void initComponents()
    {
        //Initialize the buttons on the main UI
        initButtons();

        EditText search = findViewById(R.id.main_search_bar);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("STATE", "Before text changed");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("TEXTSTATE", "on text changed");
                System.out.println("on text Changed");
                filterDisplayList(charSequence.toString().toLowerCase());
//                String searchText = charSequence.toString().toLowerCase();
//
//                displayList.clear();
//                displayList.addAll(list_of_playLists);
//
//                displayList.removeIf(playlist ->
//                        searchText.length() > 0 && !playlist.playListName.toLowerCase().contains(searchText));
//
//                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("STATE", "After text changed");


            }
        });
    }

    public void initButtons()
    {
        //select all button
        View selectAllBtn = findViewById(R.id.select_all_btn);
        selectAllBtn.setOnClickListener((View v) -> {

        });

        //trash button
        View trashBtn = findViewById(R.id.main_trash_btn);
        trashBtn.setOnClickListener((View v) -> {

        });

        //shuffle button
        View shuffleBtn = findViewById(R.id.main_shuffle_btn);
        shuffleBtn.setOnClickListener((View v) -> {

        });

        //play button
        View playButton = findViewById(R.id.main_play_btn);
        playButton.setOnClickListener((View v) -> {

            String indexes = "";
            for(int i = 0; i < list_of_playLists.size(); i++)
            {
                if(list_of_playLists.get(i).checked)
                {
                    indexes += (i + ",");
                }
            }

            if(indexes.length() > 0)
            {
                Intent intent = new Intent(this, PlayListView.class);
                intent.putExtra("Indexes", indexes);

                playListClick_launcher.launch(intent);
            }

        });

    }

    public void filterDisplayList(String searchText)
    {

        displayList.clear();
        displayList.addAll(list_of_playLists);

        displayList.removeIf(playlist ->
                searchText.length() > 0 && !playlist.playListName.toLowerCase().contains(searchText));

        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog)
    {
        Dialog d = dialog.getDialog();
        EditText editText = (EditText)d.findViewById(R.id.PL_Name);

        String name = editText.getText().toString();

        if(name.length() != 0)
        {
            PlayList playList = new PlayList();
            playList.playListName = name;
            list_of_playLists.add(playList);
            handler.writeToJSON(list_of_playLists);
            displayList();
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog)
    {
        Dialog d = dialog.getDialog();
        d.cancel();
    }

    //takes the contents of the list_of_playLists arraylist and displays it
    //in the listView
    protected void displayList()
    {
        ListView view = findViewById(R.id.playList_View);

        if(list_of_playLists == null)
        {
            System.out.println("list is null!!!!");
            return;
        }

        displayList = new ArrayList<>();

        String searchText = ( (EditText)findViewById(R.id.main_search_bar) ).getText().toString();

        displayList.addAll(list_of_playLists);

        for (PlayList playList: list_of_playLists) {
            if(searchText.length() != 0 && !playList.playListName.contains(searchText))
            {
                displayList.remove(playList);
            }
        }

        arrayAdapter = new PlayListAdapter(this, R.layout.playlist_button, displayList);

        System.out.println("The item is: " + arrayAdapter.getItem(0));
        view.setAdapter(arrayAdapter);



        //set the listener for each item in the list
//        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//
//                //get the View for the checkbox
//                CheckBox checkBox = (CheckBox) view.findViewById(R.id.playList_checkbox);
//
//                //if the checkbox is checked then uncheck it otherwise check it
//                checkBox.setChecked(!checkBox.isChecked());
//
//                list_of_playLists.get(i).checked = checkBox.isChecked();
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_settings_menu, menu);
        return true;
    }

    //when the more/settings button is clicked
    public void optionOnClick(View view)
    {

        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.main_settings_menu, popup.getMenu());

        /*A work around method to get the menu to function correctly.
            Basically overrides the itemClick listener of the PopupMenu that's created in this method
            to call the OnOptionsItemSelected method.*/
        popup.setOnMenuItemClickListener(this::onOptionsItemSelected);

        popup.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.settings:
                launchSettings();
                return true;
            case R.id.fileManager:
                launchFileManager();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void launchSettings()
    {
        testClick();
    }

    public void launchFileManager()
    {
        testClick();
    }

    //just a test click I use so I know things are working correctly
    public void testClick()
    {
        Toast.makeText(getApplicationContext(), "test click!", Toast.LENGTH_SHORT).show();
        System.out.println("Test Click!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    public static ArrayList<PlayList> getList_of_playLists()
    {
        return list_of_playLists;
    }

    //when this activity resumes from anything else
    @Override
    public void onResume()
    {
        super.onResume();
        displayList();
    }

    private static class PlayListAdapter extends ArrayAdapter<PlayList>
    {
        Context context;
        int layoutResourceId;
        ArrayList<PlayList> data = null;
        private static ArrayList<String> checked;



        public PlayListAdapter(Context context, int resource, List<PlayList> list)
        {
            super(context, resource, list);

            this.context = context;
            layoutResourceId = resource;
            data = (ArrayList) list;
            checked = new ArrayList<>();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            PlayListHolder holder = null;
            View row = convertView;
            TextView textViewName;
            TextView textViewAmt;
            CheckBox itemCheckBox;
            String plName = null;

//            if(row == null)
//            {
                    LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                    row = inflater.inflate(layoutResourceId, parent, false);

                    holder = new PlayListHolder();
                    holder.playListName = row.findViewById(R.id.name);
                    holder.itemAmount = row.findViewById(R.id.itemCount);
                    holder.playListCheck = row.findViewById(R.id.playList_checkbox);

//                row.setTag(holder);
//            }
//            else
//            {
//                Log.d("ARRAYADAPTER", "Reusing display?");
//                holder = (PlayListHolder) row.getTag();
//
//
//            }

            PlayList item = data.get(position);
            int amount = item.songList.size();

            holder.playListName.setText(item.playListName);
            holder.itemAmount.setText(Integer.toString(amount) + " item(s)");
            plName = holder.playListName.getText().toString();

            ImageButton playlist_settings = row.findViewById(R.id.playList_settings_btn);

            playlist_settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(view, position);
                }
            });

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //get the View for the checkbox
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.playList_checkbox);

                    //if the checkbox is checked then uncheck it otherwise check it
                    checkBox.setChecked(!checkBox.isChecked());

                }
            });

            holder.playListCheck.setOnCheckedChangeListener(null);
            holder.playListCheck.setChecked(checked.contains(holder.playListName.getText().toString()));

            PlayListHolder finalHolder = holder;
            holder.playListCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b)
                        checked.add(finalHolder.playListName.getText().toString());
                    else
                        checked.remove(finalHolder.playListName.getText().toString());
                }
            });

            Log.d("ARRAYADAPTER", position + " is " + plName);
            Log.d("ARRAYADAPTER", plName + ": " + checked.contains(plName));

            return row;
        }

        //display the popup menu for each item
        private void showPopupMenu(View view, int pos)
        {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.playlist_button_menu);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    return PlayListAdapter.this.onMenuItemClick(menuItem, pos);
                }
            });

            popupMenu.show();
        }

        public boolean onMenuItemClick(MenuItem item, int pos)
        {

            if(item.getItemId() == R.id.playlist_popup_edit)
            {
                //TODO: Start the edit playlist activity
                Intent intent = new Intent(context, Edit_Playlist.class);
                intent.putExtra("playlist_index", pos);


                return true;
            }
            else if(item.getItemId() == R.id.playlist_popup_delete)
            {
                //TODO: delete playlist functionality goes here
                return true;
            }
            return false;
        }

        public static void clearChecked()
        {
            checked.clear();
        }

        //TODO: make a way to get the indexes of the checked items

        private class PlayListHolder
        {
            public TextView playListName;
            public TextView itemAmount;
            public CheckBox playListCheck;
        }
    }
}

class PlayList
{
    public String playListName;
    public ArrayList<Song> songList;
    public boolean checked = false;

    public PlayList()
    {
        songList = new ArrayList<Song>();
    }

    public String toString()
    {
        return playListName;
    }
}

class Song
{
    public String name;
    public String path;
}
