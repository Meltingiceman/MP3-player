package com.example.mp3player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlayListView extends AppCompatActivity {

    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;

    private static int playList_ix;
    private ActivityResultLauncher<Intent> edit_launcher;
    private MusicIntentReceiver receiver;
    public static boolean changeState;

    private TextView time;
    private TextView songPlaying;
    private SeekBar progressBar;
    private ImageButton playPauseButton;

    private RecyclerView songList;
    private SongAdapter listAdapter;
    AudioManager.OnAudioFocusChangeListener focusChangeListener;

    private final Handler progressHandler = new Handler();
    private ArrayList<Song> playList;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        System.out.println("DEBUG: BACK BTN PRESSED");
        MusicPlayer.getInstance().pause();
        MusicPlayer.getInstance().reset();
//        if (audioFocusRequest != null) {
//
////            mediaPlayer.pause();   //don't know if pausing is necessary for releasing
////            mediaPlayer.release();
////            mediaPlayer = null;
//
//            audioManager.abandonAudioFocusRequest(audioFocusRequest);
//        }

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list_view);
        changeState = false;
        Intent intent = getIntent();
        playList_ix = intent.getIntExtra("PlayListIndex", -1);
        playList = MainActivity.list_of_playLists.get(playList_ix).songList;

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //UI elements
        TextView playListName = findViewById(R.id.playListName);
        songList = findViewById(R.id.songList);
        playPauseButton = findViewById(R.id.playPause_btn);
        progressBar = findViewById(R.id.seekBar);
        songPlaying = findViewById(R.id.songPlaying);
        time = findViewById(R.id.time);

        //load the selected playlist into the MusicPlayer
        MusicPlayer.getInstance().loadPlayList(playList);
        MusicPlayer.getInstance().setOnCompletionListener(mediaPlayer -> notifyStateChange());

        playListName.setText(MainActivity.list_of_playLists.get(playList_ix).playListName);

        //creates and attaches the adapter that the recyclerView
        createAdapter();



        receiver = new MusicIntentReceiver();
        edit_launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    ArrayList<PlayList> list_of_playLists = MainActivity.list_of_playLists;
                    System.out.println("Name back from activity: " + list_of_playLists.get(playList_ix).playListName);

                    playListName.setText(list_of_playLists.get(playList_ix).playListName);
                    //numSongs.setText(Integer.toString(playList.size())  + " song(s) in playlist");
                    //mediaPlayer.reset();

                }
        );

        //Code to be executed continuously int the background on another thread
        PlayListView.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(MusicPlayer.getInstance().getState() != State.INIT &&
                        MusicPlayer.getInstance().getState() != State.INTERRUPTED) {

                    if(changeState)
                    {
                        changeState = false;
                        notifyStateChange();
                    }

                    int mCurrentPos = MusicPlayer.getInstance().getCurrentTime() / 1000;
                    progressBar.setProgress(mCurrentPos);

                    if (MusicPlayer.getInstance().getState() == State.IDLE) {

                        time.setText("--:--/--:--");
                    } else {
                        String timer = convertTime(MusicPlayer.getInstance().getCurrentTime()) + "/" +
                                convertTime(MusicPlayer.getInstance().getSongMax());
                        time.setText(timer);
                    }

                    progressHandler.postDelayed(this, 1000);

                    //Don't really know why this is needed but without this items in the recyclerview disappear
                    //listAdapter.notifyDataSetChanged();
                }
            }
        });

        //listeners for the seekbar that represents the progress in a song
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(MusicPlayer.getInstance().getState() != State.IDLE &&
                        MusicPlayer.getInstance().getState() != State.INIT && fromUser) {

                    MusicPlayer.getInstance().goToTime(progress * 1000);

                    String timer = convertTime(MusicPlayer.getInstance().getCurrentTime()) + "/" +
                            convertTime(MusicPlayer.getInstance().getSongMax());
                    time.setText(timer);

                    //Don't really know why this is needed but without this items in the recyclerview disappear
                    //listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                MusicPlayer.getInstance().pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                MusicPlayer.getInstance().resume();


            }
        });

//        //register the receiver and intent at the end of onCreate
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(receiver, filter);

    }

    private String convertTime(int time)
    {
        String res = "";

        int maxTime = time / 1000;

        int minutes = maxTime / 60;
        int seconds = maxTime % 60;

        res += minutes + ":";

        if(seconds < 10)
            res += "0";

        res += seconds;

        return res;
    }

    protected void createAdapter()
    {
        listAdapter = new SongAdapter(this, playList);

        ItemTouchHelper.Callback callback = new ItemMoveCallback(listAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(songList);

        songList.setAdapter(listAdapter);
        songList.setLayoutManager(new LinearLayoutManager(this));
    }

    public void notifyStateChange()
    {

        //if the state change was PAUSED/IDLE --> PLAYING
        if(MusicPlayer.getInstance().getState() == State.PLAYING)
        {
            playPauseButton.setImageResource(R.drawable.pause_icon);

            String _time = "0:00/";
            int maxTime = MusicPlayer.getInstance().getSongMax() / 1000;

            int minutes = maxTime / 60;
            int seconds = maxTime % 60;

            _time += minutes + ":";

            if(seconds < 10)
                _time += "0";

            _time += seconds;
            time.setText(_time);

            progressBar.setMax(MusicPlayer.getInstance().getSongMax() / 1000);

            songPlaying.setText("Playing: " + MusicPlayer.getInstance().getPlayingSongName());

        }
        //if the state change was PLAYING --> PAUSED
        else if(MusicPlayer.getInstance().getState() == State.PAUSED)
        {
            playPauseButton.setImageResource(R.drawable.play_icon);
        }

        //listAdapter.notifyDataSetChanged();
    }

    public void shuffle_btn_click(View view)
    {
        MusicPlayer.getInstance().shuffle();

        if (MusicPlayer.getInstance().getPreviousState() == State.IDLE) {
            notifyStateChange();
        }
//        else{
//            listAdapter.notifyDataSetChanged();
//        }

    }

    //when the edit playlist button is pressed
    public void edit_plList_btnClick(View view)
    {
        if(MusicPlayer.getInstance().getState() == State.PLAYING)
        {
            MusicPlayer.getInstance().pause();
        }

        Intent intent = new Intent(this, Edit_Playlist.class);
        intent.putExtra("playlist_index", playList_ix);
        edit_launcher.launch(intent);

        ArrayList<PlayList> list_of_playLists = MainActivity.list_of_playLists;
        System.out.println("Name back from activity: " + list_of_playLists.get(playList_ix).playListName);

    }

    public void skip_right_click(View view)
    {
        MusicPlayer.getInstance().nextSong();
        notifyStateChange();
    }

    public void skip_left_click(View view)
    {
        MusicPlayer.getInstance().previousSong();
        notifyStateChange();
    }

    public void playPause_btn_click(View view)
    {
        //if the MusicPlayer is playing
        if(MusicPlayer.getInstance().getState() == State.PLAYING)
        {
            MusicPlayer.getInstance().pause();
            notifyStateChange();
        }
        //if the MusicPlayer is paused
        else if(MusicPlayer.getInstance().getState() == State.PAUSED)
        {
            MusicPlayer.getInstance().resume();
            notifyStateChange();
        }
        else if(MusicPlayer.getInstance().getState() == State.IDLE)
        {
            MusicPlayer.getInstance().playSong(0);
            notifyStateChange();
        }
    }

//    private int requestFocus()
//    {
//        audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
//        AudioAttributes playbackAttributes = new AudioAttributes.Builder()
//                .setUsage(AudioAttributes.USAGE_MEDIA)
//                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                .build();
//
//
//        audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
//                .setAudioAttributes(playbackAttributes)
//                .setAcceptsDelayedFocusGain(true)
//                .setOnAudioFocusChangeListener(focusChangeListener)
//                .build();
//
//        return audioManager.requestAudioFocus(audioFocusRequest);
//    }

    private class MusicIntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG) && MusicPlayer.getInstance().getState() != State.IDLE) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        System.out.println("DEBUG: ON RECEIVE");
                        MusicPlayer.getInstance().pause();
                        break;
                }
            }
        }
    }
}