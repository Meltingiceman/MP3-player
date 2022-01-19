package com.example.mp3player;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class AddPlaylist extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_playlist);

        //displayFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());

    }

    public void create_click(View view)
    {
        EditText box = findViewById(R.id.name_box);
//        EditText routeBox = findViewById(R.id.folder_box);

        String name = box.getText().toString();
//        String route = routeBox.getText().toString();

        Intent intent = new Intent();
        intent.putExtra(MainActivity.playListName_key, name);
//        intent.putExtra(MainActivity.playListRoute_key, route);
        setResult(RESULT_OK, intent);

        finish();
    }

//    protected void displayFiles(String path)
//    {
//        System.out.println("DEBUG: STARTING DISPLAYFILES WITH PATH: " + path);
//        File location = new File(path);
//        String[] pathNames = location.list();
//        ArrayList<String> possible_files = new ArrayList<String>();
//
//        if(pathNames != null) {
//            System.out.println("DEBUG: PATHNAMES NOT NULL. HAS " + pathNames.length + " ITEMS");
//            for (String pathname : pathNames) {
//                System.out.println(path + "/" + pathname);
//                File f = new File(path + "/" + pathname);
//
//                if(f.isFile())
//                {
//                    possible_files.add(f.getName());
//                }
//            }
//        }
//        else
//        {
//            System.out.println("pathNames is null");
//        }
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item, possible_files);
//        ListView listView = (ListView) findViewById(R.id.file_list);
//        listView.setAdapter(adapter);
//
//        //onClick listener
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(getApplicationContext(), "Item " + i + " Pressed.", Toast.LENGTH_LONG).show();
//            }
//        });
//
//        System.out.println("DEBUG: ENDING DISPLAYFILES");
//    }
}