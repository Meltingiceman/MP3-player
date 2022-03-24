package com.example.mp3player;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;


public class FileHandler {

    public static final String DATA_FILE_NAME = "data.json";
    public static final String SETTINGS_FILE_NAME = "settings.json";
    public static final String DATA_FOLDER_NAME = "appData";
    public static final String MUSIC_FOLDER_NAME = "music";
    private String root;
    File dataFolder;
    File dataFile;
    File settingsFile;

    public FileHandler(String r)
    {

        root = r;
        String dataPath = root + File.separator + DATA_FOLDER_NAME;
        String dataFilePath = dataPath + File.separator + DATA_FILE_NAME;
        dataFolder = new File(dataPath);
        dataFile = new File(dataFilePath);
        settingsFile = new File(root + File.separator + SETTINGS_FILE_NAME);

    }

    public boolean init()
    {
        if(!dataFolder.exists() || !dataFolder.isDirectory() || !dataFile.exists())
        {
            dataFolder.delete();
            dataFolder.mkdir();

            //create an empty json file
            boolean success = initJSON();

            if(!success)
            {

                return false;
            }
        }

        File musicFolder = new File( root + File.separator + MUSIC_FOLDER_NAME );
        if(!musicFolder.exists())
        {
            musicFolder.mkdir();
        }

        return true;
    }

    public boolean initSettings()
    {
        if(!settingsFile.exists())
        {
            settingsFile = new File(root + File.separator + SETTINGS_FILE_NAME);

            JSONObject settingsRoot = new JSONObject();


        }

        return false;
    }

    public boolean initJSON()
    {
        JSONObject root = new JSONObject();
        JSONArray musicList = new JSONArray();
        try {
            root.put("playLists", musicList);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("path in initJSON: " + this.root + File.separator + DATA_FOLDER_NAME + File.separator + DATA_FILE_NAME);

        try {
            FileWriter writer = new FileWriter(this.root + File.separator + DATA_FOLDER_NAME + File.separator + DATA_FILE_NAME);
            writer.write(root.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void writeToJSON(ArrayList<PlayList> list_of_playLists) {
        String dataPath = root + File.separator + DATA_FOLDER_NAME;

        try {
            JSONObject root = new JSONObject();
            JSONArray playListArray = new JSONArray();

            JSONObject playListItem;

            for (int i = 0; i < list_of_playLists.size(); i++) {
                playListItem = new JSONObject();
                playListItem.put("PlayListName", list_of_playLists.get(i).playListName);

                JSONArray songList = new JSONArray();
                JSONObject song;
                for (int j = 0; j < list_of_playLists.get(i).songList.size(); j++) {
                    song = new JSONObject();
                    song.put("Name", list_of_playLists.get(i).songList.get(j).name);
                    song.put("Path", list_of_playLists.get(i).songList.get(j).path);

                    songList.put(song);
                }

                playListItem.put("songList", songList);
                playListArray.put(playListItem);
            }

            root.put("playLists", playListArray);

            FileWriter writer = new FileWriter(dataPath + File.separator + DATA_FILE_NAME);
            writer.write(root.toString());
            writer.close();

        } catch (JSONException | IOException e) {
            e.printStackTrace();

        }
    }

    public ArrayList<PlayList> loadPlayLists() throws JSONException
    {
        ArrayList<PlayList> list_of_playLists = new ArrayList<PlayList>();

        Scanner jsonScanner;
        String jsonString = "";
        try {
            System.out.println("PATH: " + root + File.separator + DATA_FOLDER_NAME + File.separator + DATA_FILE_NAME);
            jsonScanner = new Scanner(new File(root + File.separator + DATA_FOLDER_NAME + File.separator + DATA_FILE_NAME));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
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

            JSONArray songList = playLists.getJSONObject(i).getJSONArray("songList");

            System.out.println("\tsongList size: " + songList.length());

            for(int j = 0; j < songList.length(); j++)
            {
                list_of_playLists.get(i).songList.add(j, new Song());
                list_of_playLists.get(i).songList.get(j).name = songList.getJSONObject(j).getString("Name");
                list_of_playLists.get(i).songList.get(j).path = songList.getJSONObject(j).getString("Path");
            }
        }

        return list_of_playLists;
    }

    public void copyFile(String inputPath, String inputFile, String outputPath)
    {
        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;
        }

        catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    public void moveFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file

            File f = new File(inputPath + inputFile);
            boolean deleted;

            if(f.exists()) {
                deleted = f.delete();
                System.out.println("THE FILE EXISTS!");
            }
            else {
                System.out.println("THE FILE DOESN\'T EXIST!");
                deleted = false;
            }


            if(deleted)
            {
                System.out.println("DELETED");
            }
            else
            {
                System.out.println("COUDLDN\'T delete FILE, SADGE");
            }
        }

        catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }
}
