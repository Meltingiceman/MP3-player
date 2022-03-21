package com.example.mp3player;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity
                            implements AddSongDialogFragment.AddSongDialogListener{

    public static final String playListName_key = "PLAYLIST_ADD";
    public static final String playListRoute_key = "PLAYLIST_ROUTE";
    public static ArrayList<PlayList> list_of_playLists;
    public static FileHandler handler;
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

        //display the musicList
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
        playListClick_launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK)
                    {
                        //NEED TO THINK OF OTHER THINGS TO DO HERE (IF ANY)


                        //write to JSON to write any changed to the list of playlists
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

    protected void loadPlayLists(String filePath) throws JSONException {
        Scanner jsonScanner;
        String jsonString = "";
        try {
            jsonScanner = new Scanner(new File(filePath + File.separator + DATA_FILE_NAME));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        while(jsonScanner.hasNext())
        {
            jsonString += jsonScanner.nextLine();
        }

        JSONObject obj = new JSONObject(jsonString);
        JSONArray playLists = obj.getJSONArray("playLists");
        list_of_playLists = new ArrayList<PlayList>();

        for(int i = 0; i < playLists.length(); i++)
        {

            list_of_playLists.add(i, new PlayList());
            list_of_playLists.get(i).playListName = playLists.getJSONObject(i).getString("PlayListName");

//            System.out.println("NAME: " + list_of_playLists.get(i).playListName);

//            System.out.println(playLists.getJSONObject(i).getJSONArray("songList"));

            JSONArray songList = playLists.getJSONObject(i).getJSONArray("songList");

            for(int j = 0; j < songList.length(); j++)
            {
                list_of_playLists.get(i).songList.add(j, new Song());
                list_of_playLists.get(i).songList.get(j).name = songList.getJSONObject(j).getString("Name");
                list_of_playLists.get(i).songList.get(j).path = songList.getJSONObject(j).getString("path");
            }
        }
    }

    //when the plus button is clicked
    public void add_playList_btnClick(View view)
    {

        DialogFragment newFragment = new AddSongDialogFragment();
        newFragment.show(getSupportFragmentManager(), "add_playlist");


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

        PlayListAdapter arrayAdapter = new PlayListAdapter(this, R.layout.playlist_button, list_of_playLists);

        view.setAdapter(arrayAdapter);
        Intent intent = new Intent(this, PlayListView.class);

        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(getApplicationContext(), "Item " + i + " Pressed.", Toast.LENGTH_LONG).show();

                intent.putExtra("PlayListIndex", i);
                playListClick_launcher.launch(intent);

            }
        });
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
        popup.setOnMenuItemClickListener(menuItem -> onOptionsItemSelected(menuItem));

        popup.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.settings:
                testClick();
                return true;
            case R.id.fileManager:
                testClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private class PlayListAdapter extends ArrayAdapter<PlayList>
    {
        Context context;
        int layoutResourceId;
        ArrayList<PlayList> data = null;

        public PlayListAdapter(Context context, int resource, List<PlayList> list)
        {
            super(context, resource, list);

            this.context = context;
            layoutResourceId = resource;
            data = (ArrayList) list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            PlayListHolder holder = null;
            View row = convertView;
            if(row == null)
            {
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new PlayListHolder();
                holder.playListName = (TextView) row.findViewById(R.id.name);
                holder.itemAmount = (TextView) row.findViewById(R.id.itemCount);

                row.setTag(holder);
            }
            else
            {
                holder = (PlayListHolder) row.getTag();
            }

            PlayList item = data.get(position);
            int amount = item.songList.size();
            //System.out.println("IN ADAPTER: " + item.playListName);

            holder.playListName.setText(item.playListName);
            holder.itemAmount.setText(Integer.toString(amount) + " item(s)");

            ImageButton deleteBtn = row.findViewById(R.id.delete_playlist_btn);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    list_of_playLists.remove(position);
                    handler.writeToJSON(list_of_playLists);
                    displayList();
//                    Toast.makeText(getApplicationContext(), "Delete pressed!", Toast.LENGTH_LONG).show();


                }
            });

//            row.setClickable(true);
//            row.setFocusable(true);

            return row;
        }

        private class PlayListHolder
        {
            public TextView playListName;
            public TextView itemAmount;
        }
    }
}

class PlayList
{
    public String playListName;
    public ArrayList<Song> songList;

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
