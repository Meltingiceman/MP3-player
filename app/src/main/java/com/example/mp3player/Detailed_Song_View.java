/*
    Activity is called when editing/creating a song
 */

package com.example.mp3player;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Detailed_Song_View extends AppCompatActivity {

    private CheckBox deleteOption;
    private ListView downloads_list;
    private ListView folder_list;
    private ArrayList<String> list_of_files;
    private String download_route;
    private String song_selected = null;
    private String song_name = null;
    private EditText route;

    private boolean delete;
    private int selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_song_view);

        Intent thisIntent = getIntent();
        download_route = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        boolean editing = thisIntent.getBooleanExtra("editing", false);
        selectedItem = -1;

        route = findViewById(R.id.File_URL);
        route.setText(download_route);

        downloads_list = findViewById(R.id.downloads_view);

        if (editing) {
            //get editing data
        }
        else {
            //fill in with blank data for new addition to the playlist (if any)

        }


        ImageButton backBtn = (ImageButton) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                finish();
            }
        });

        displayFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
    }

    //when the user clicks the save button
    public void save_click(View view)
    {
        EditText name = findViewById(R.id.songName);
        song_name = name.getText().toString();
        if(song_name.length() == 0 || song_selected == null)
        {
            Toast.makeText(getApplicationContext(), "Select a SONG and NAME.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent result = new Intent();
        result.putExtra("songName", song_name);
        Song addition = new Song();
        addition.name = song_name;
        MainActivity.handler.copyFile(
                download_route + File.separator,
                song_selected.toString(),
                getFilesDir().toString() + File.separator + FileHandler.MUSIC_FOLDER_NAME + File.separator
        );

        result.putExtra("songRoute",
                getFilesDir().toString() + File.separator + FileHandler.MUSIC_FOLDER_NAME + File.separator +
                        song_selected.toString()
        );

        result.putExtra("editing", getIntent().getBooleanExtra("editing", false));

        setResult(RESULT_OK, result);
        finish();
    }

    public void refreshList(View view)
    {
        String path = route.getText().toString();
        displayFiles(path);
    }

    protected void displayFiles(String path)
    {
//        System.out.println("DEBUG: STARTING DISPLAYFILES WITH PATH: " + path);
        File location = new File(path);
        File musicFolder = new File(getFilesDir() + File.separator + MainActivity.MUSIC_FOLDER_NAME);
        String[] pathNames = location.list();
        String[] music_folder_pathNames = musicFolder.list();
        ArrayList<File> downloadFiles = new ArrayList<File>();
        ArrayList<String> music_files = new ArrayList<String>();

        if(music_folder_pathNames != null) {

            for (String song : music_folder_pathNames) {
                File f = new File(musicFolder.toString() + File.separator + song);

                //only thing in this folder should be files
                music_files.add(f.getName());
            }
        }

        for (String pathname : pathNames) {
            File f = new File(path + File.separator + pathname);

            if(filterPath(f.toString()))
                downloadFiles.add(f);
        }

        ArrayAdapter<String> folder_adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item, music_files);
        FileAdapter download_adapter = new FileAdapter(this, R.layout.music_file_list_item, downloadFiles);

        downloads_list.setAdapter(download_adapter);
//        folder_list.setAdapter(folder_adapter);

        //when an item from the download list gets clicked
        downloads_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                view.setSelected(true);
                selectedItem = i;
                if(downloadFiles.get(i).isDirectory())
                {
                    String route_so_far = route.getText().toString();

                    if(route_so_far.charAt(route_so_far.length() - 1) != File.separatorChar)
                        route_so_far += File.separatorChar;

                    route.setText( route_so_far + downloadFiles.get(i).getName() );
                    refreshList(null);

                }
                else
                {

                    TextView tView = findViewById(R.id.song_selected);

                    //song selected will be at the ith position
                    song_selected = downloadFiles.get(i).getName();

                    tView.setText("Song selected: " + song_selected);
                    delete = true;
                }
            }
        });
    }

    //a method used to filter out files that aren't normally used for audio files
    private boolean filterPath(String path)
    {
        return !(path.contains(".zip") || path.contains(".docx") || path.contains(".jpg") ||
                    path.contains(".gba") || path.contains(".pdf") || path.contains(".txt"));
    }

    public void navigate_to_downloads(View view)
    {
        route.setText(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
        refreshList(null);
    }

    public void navigate_to_data(View view)
    {
        route.setText(getFilesDir() + File.separator + MainActivity.MUSIC_FOLDER_NAME);
        refreshList(null);
    }

     //a file adapter used to display the contents of file locations
     private class FileAdapter extends ArrayAdapter <File>
    {
        protected Context context;
        protected int layoutResourceId;
        protected ArrayList<File> files;

        public FileAdapter(Context context, int resource, List<File> list)
        {
            super(context, resource, list);
            this.context = context;
            layoutResourceId = resource;

            files = (ArrayList<File>) list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            FileHolder holder;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new FileHolder();
                holder.fileName = (TextView) row.findViewById(R.id.text);

                row.setTag(holder);
            } else {
                holder = (FileHolder) row.getTag();
            }

            File file = files.get(position);

            if (file.isDirectory())
            {
                ImageView icon = row.findViewById(R.id.icon);
                icon.setImageResource(R.drawable.folder_icon);
            }

            System.out.println("\t\t CHECKING FOR SELECTED ITEM");
            if(selectedItem == position)
            {

                row.setBackgroundColor(getResources().getColor(R.color.purple_700));
            }

            String text = files.get(position).getName();
            holder.fileName.setText(text);

            return row;
        }

        private class FileHolder
        {
            public TextView fileName;
        }
    }
}

