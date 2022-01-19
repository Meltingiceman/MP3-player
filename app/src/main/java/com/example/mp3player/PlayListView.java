package com.example.mp3player;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.*;

public class PlayListView extends AppCompatActivity {

    private enum State{
        IDLE, PAUSED, PLAYING
    }

    private int playList_ix;
    private ActivityResultLauncher<Intent> edit_launcher;
    private MusicIntentReceiver receiver;

    TextView time;
    TextView songPlaying;
    SeekBar progressBar;
    MediaPlayer mediaPlayer;
    ListView songList;
    AudioManager.OnAudioFocusChangeListener focusChangeListener;

    boolean playing = false;

    private Handler progressHandler = new Handler();
    private ArrayList<Song> playList;
    private int playingIx;
    boolean firstTime = false;
    boolean init;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list_view);
        playingIx = -1;
        init = true;

        Intent intent = getIntent();
        playList_ix = intent.getIntExtra("PlayListIndex", -1);

        receiver = new MusicIntentReceiver();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        progressBar = findViewById(R.id.seekBar);
        progressBar.setMax(0);


        time = findViewById(R.id.time);
        TextView playListName = findViewById(R.id.playListName);
        songPlaying = findViewById(R.id.songPlaying);
        songList = findViewById(R.id.songList);
        ImageButton gear = findViewById(R.id.edit_btn);

        //set the playList name and playList
        playListName.setText(MainActivity.list_of_playLists.get(playList_ix).playListName);
        playList = MainActivity.list_of_playLists.get(playList_ix).songList;

        focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {
                switch (i)
                {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        mediaPlayer.start();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        mediaPlayer.pause();
                        break;
                }
            }
        };

        //creating the list of songs
        createAdapter();

        //media players actions to perform upon completion
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(firstTime)
                {
                    playing = false;
                    progressBar.setProgress(0);

                    nextSong();
                }
                firstTime = true;
                //no need to call swapPlayPause since the playing will carry over to the next song
            }
        });

        edit_launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        ArrayList<PlayList> list_of_playLists = MainActivity.list_of_playLists;
                        System.out.println("Name back from activity: " + list_of_playLists.get(playList_ix).playListName);

                        playListName.setText(list_of_playLists.get(playList_ix).playListName);
                        //numSongs.setText(Integer.toString(playList.size())  + " song(s) in playlist");
                        mediaPlayer.reset();
                        playingIx = -1;
                        createAdapter();


                    }
                }
        );

        PlayListView.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null)
                {
                    int mCurrentPos = mediaPlayer.getCurrentPosition() / 1000;
                    progressBar.setProgress(mCurrentPos);

                    if(init)
                    {
                        time.setText("--:--/--:--");
                    }
                    else
                    {
                        String timer = getCurrentTime() + "/" + getMaxTime();
                        time.setText(timer);
                    }

                    ImageButton playPauseBtn = findViewById(R.id.playPause_btn);
                    if(mediaPlayer.isPlaying())
                    {
                        playPauseBtn.setImageResource(R.drawable.pause_icon);
                    }
                    else
                    {
                        playPauseBtn.setImageResource(R.drawable.play_icon);
                    }

                }

                progressHandler.postDelayed(this, 1000);
            }
        });



        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer != null && fromUser)
                {
                    mediaPlayer.seekTo(progress * 1000);
                    String timer = getCurrentTime() + "/" + getMaxTime();
                    time.setText(timer);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pause_no_icon_change();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if(playing)
                    play();


            }
        });
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(receiver, filter);
        super.onResume();
    }

    protected void createAdapter()
    {

        //create the adapter
        SongAdapter arrayAdapter = new SongAdapter(this,
                R.layout.simple_song_list_item,  //the layout that is used
                playList
        );

        //set the adapter
        songList.setAdapter(arrayAdapter);
        songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(playingIx == i && mediaPlayer.isPlaying())
                    return;

                //Toast.makeText(getApplicationContext(), "This will play item " + i + ".", Toast.LENGTH_LONG).show();

                //DEBUG
                System.out.println(playList.get(i).path);

                playSong(i);

            }
        });

    }

    private void nextSong()
    {
        if(playingIx < playList.size() - 1)
        {
            playingIx++;
        }
        else
            playingIx = 0;

        playSong(playingIx);
    }

    private void playSong(int ix)
    {
        try {
            //need to reset the media player before changing the data source

            mediaPlayer.reset();
            mediaPlayer.setDataSource(playList.get(ix).path);
            mediaPlayer.prepare();

            //divide by 1000 here to go from milliseconds to seconds
            progressBar.setMax(mediaPlayer.getDuration() / 1000);
            time.setText(getCurrentTime() + "/" + getMaxTime());
            play();

            playingIx = ix;
            songPlaying.setText("Now playing: " + playList.get(playingIx).name);
            init = false;

        } catch (IOException e) {
            e.printStackTrace();
            playing = false;
        }

    }

    public void shuffle_btn_click(View view)
    {

        if(!mediaPlayer.isPlaying()) //if mediaPlayer is NOT playing
        {
            Collections.shuffle(playList);
            createAdapter();

            //DEBUG
            for(int i = 0; i < playList.size(); i++)
            {
                System.out.println(playList.get(i).name);
            }

            playingIx = 0;
            playSong(playingIx);
        }
        else
        {
            Song temp = playList.get(playingIx);
            ArrayList<Song> tempList = new ArrayList<Song>();
            tempList.add(temp);

            System.out.println("Song playing is \"" + temp.name + "\"\nindex is " + playingIx);

            Collections.shuffle(playList);

            for(int i = 0; i < playList.size(); i++)
            {
                System.out.println(playList.get(i).name);
                if(!playList.get(i).name.equals(temp.name))
                {
                    tempList.add(playList.get(i));
                }
            }

            playList = tempList;
            playingIx = 0;
            createAdapter();
        }

    }

    //when the edit playlist button is pressed
    public void edit_plList_btnClick(View view)
    {
        if(mediaPlayer.isPlaying()) {
//            mediaPlayer.pause();
//            swapPlayPause();
            pause();
        }

        Intent intent = new Intent(this, Edit_Playlist.class);
        intent.putExtra("playlist_index", playList_ix);
        edit_launcher.launch(intent);

        ArrayList<PlayList> list_of_playLists = MainActivity.list_of_playLists;
        System.out.println("Name back from activity: " + list_of_playLists.get(playList_ix).playListName);

    }

    public void skip_right_click(View view)
    {
        if(playingIx == -1)
            return;

        nextSong();
    }

    public void skip_left_click(View view)
    {
        if(playingIx == -1)
            return;

        if(playingIx > 0)
            playingIx--;
        else
            playingIx = playList.size() - 1;

        playSong(playingIx);

    }

    public void playPause_btn_click(View view)
    {

        if(mediaPlayer.isPlaying())
            pause();
        else
            play();

    }

    private void pause()
    {
        mediaPlayer.pause();
        ImageButton playPause_btn = findViewById(R.id.playPause_btn);

        playPause_btn.setImageResource(R.drawable.play_icon);
        playing = false;
    }

    private void pause_no_icon_change()
    {
        mediaPlayer.pause();
        playing = false;
    }

    private void play()
    {
        //need to request audio focus before playing
        if(requestFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        {
            mediaPlayer.start();
            ImageButton playPause_btn = findViewById(R.id.playPause_btn);

            playPause_btn.setImageResource(R.drawable.pause_icon);
            playing = true;
        }
    }

    private int requestFocus()
    {
        System.out.println("Requesting access!");
        AudioManager manager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();


        AudioFocusRequest request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build();


        return manager.requestAudioFocus(request);
    }



    private void swapPlayPause()
    {
        ImageButton playPause_btn = findViewById(R.id.playPause_btn);
        if(mediaPlayer.isPlaying())
        {
            playPause_btn.setImageResource(R.drawable.play_icon);
        }
        else
        {
            playPause_btn.setImageResource(R.drawable.pause_icon);
        }

        //swap the value of playing
        playing = !playing;
    }

    private String getCurrentTime()
    {
        String result = "";

        int timeSeconds = mediaPlayer.getCurrentPosition() / 1000;
        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;

        result = minutes + ":";

        if(seconds < 10)
            result += "0";

        result += seconds;

        return result;
    }

    private String getMaxTime()
    {
        String result = "";
        int timeSeconds = mediaPlayer.getDuration() / 1000;

        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;

        result = minutes + ":";

        if(seconds < 10)
            result += "0";

        result += seconds;

        return result;
    }

    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d(TAG, "Headset is unplugged");
                        pause();
                        break;
                    case 1:
                        Log.d(TAG, "Headset is plugged");
                        break;
//                    default:
//                        Log.d(TAG, "I have no idea what the headset state is");
                }
            }
        }
    }
}

// a song adapter used for the listview displaying the songs in the playlist
class SongAdapter extends ArrayAdapter<Song>
{
    protected Context context;
    protected int layoutResourceId;
    protected ArrayList<Song> data = null;

    int viewId = 0;

    public SongAdapter(Context context, int resource, List<Song> list)
    {
        super(context, resource, list);

        this.context = context;
        layoutResourceId = resource;
        data = (ArrayList) list;
    }

    public SongAdapter(Context context, int resource, List<Song> list, int id)
    {
        super(context, resource, list);

        this.context = context;
        layoutResourceId = resource;
        data = (ArrayList) list;

        viewId = id;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SongHolder holder;
        View row = convertView;

        if(row == null)
        {
            System.out.println("in if");
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new SongHolder();

            if(viewId == 0)
                holder.songName = (TextView) row.findViewById(R.id.name);
            else
                holder.songName = (TextView) row.findViewById(viewId);

            row.setTag(holder);
        }
        else
        {
            System.out.println("in else");
            holder = (SongHolder) row.getTag();
        }

        Song item = data.get(position);

        holder.songName.setText(item.name);
        return row;
    }

    private class SongHolder
    {
        public TextView songName;
    }
}