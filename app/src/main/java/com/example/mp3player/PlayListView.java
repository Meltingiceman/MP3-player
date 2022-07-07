package com.example.mp3player;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayListView extends AppCompatActivity {

    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;

    private static int playList_ix;
    private static int songIx = -1;
    private ActivityResultLauncher<Intent> edit_launcher;
    private MusicIntentReceiver receiver;

    private static TextView time;
    private static TextView songPlaying;
    private static SeekBar progressBar;
    private static ImageButton playPauseButton;

    private RecyclerView songList;
    private static SongAdapter listAdapter;
    AudioManager.OnAudioFocusChangeListener focusChangeListener;

    private final Handler progressHandler = new Handler();
    private ArrayList<Song> playList;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        System.out.println("DEBUG: BACK BTN PRESSED");

        if (audioFocusRequest != null) {
//            mediaPlayer.pause();   //don't know if pausing is necessary for releasing
//            mediaPlayer.release();
//            mediaPlayer = null;

            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        }

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list_view);

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

        playListName.setText(MainActivity.list_of_playLists.get(playList_ix).playListName);

        createAdapter();

        receiver = new MusicIntentReceiver();
//        AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                .setUsage(AudioAttributes.USAGE_MEDIA)
//                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                .build();
//
//        mediaPlayer.setAudioAttributes(audioAttributes);
//        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        progressBar = findViewById(R.id.seekBar);
//        progressBar.setMax(0);
//
//      set it so that when the user presses up on the audio it raises media volume


//        //variables for the various views that display information
//        time = findViewById(R.id.time);

//        songPlaying = findViewById(R.id.songPlaying);
//        songList = findViewById(R.id.songList);
//        ImageButton gear = findViewById(R.id.edit_btn);
//
//        //set the playList name and playList

//        playList = MainActivity.list_of_playLists.get(playList_ix).songList;
//
//        focusChangeListener = i -> {
//            System.out.println("AUDIO_FOCUS_CHANGE");
//            switch (i)
//            {
//                case AudioManager.AUDIOFOCUS_GAIN:
//                    if(state == State.INTERRUPTED)
//                        play();
//                    break;
//                case AudioManager.AUDIOFOCUS_LOSS:
//                    mediaPlayer.release();
//                    audioManager.abandonAudioFocusRequest(audioFocusRequest);
//                    break;
//                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
//                    pause(true);
//                    break;
//            }
//        };
//
//        //creating the list of songs
//        createAdapter();
//
////        //media players actions to perform upon completion
////        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
////            if(firstTime)
////            {
////                progressBar.setProgress(0);
////
////                nextSong();
////            }
////            firstTime = true;
////            //no need to call swapPlayPause since the playing will carry over to the next song
////        });
//
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

        PlayListView.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(MusicPlayer.getInstance().getState() != State.INIT &&
                        MusicPlayer.getInstance().getState() != State.INTERRUPTED) {

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
                    listAdapter.notifyDataSetChanged();
                }
            }
        });
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
                    listAdapter.notifyDataSetChanged();
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
        songList.setAdapter(listAdapter);
        songList.setLayoutManager(new LinearLayoutManager(this));
    }

    public static void notifyStateChange()
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

        listAdapter.notifyDataSetChanged();
    }

//    public void playSong(int ix)
//    {
//        try {
//            //need to reset the media player before changing the data source
//
//            mediaPlayer.reset();
//            mediaPlayer.setDataSource(playList.get(ix).path);
//            mediaPlayer.prepare();
//
//            //divide by 1000 here to go from milliseconds to seconds
//            progressBar.setMax(mediaPlayer.getDuration() / 1000);
//            time.setText(getCurrentTime() + "/" + getMaxTime());
//            play();
//
//            playingIx = ix;
//            songPlaying.setText("Now playing: " + playList.get(playingIx).name);
//            init = false;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    public void shuffle_btn_click(View view)
    {
        MusicPlayer.getInstance().shuffle();
        notifyStateChange();
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
//
//    private void pause(boolean interrupted)
//    {
//        System.out.println("DEBUG: PAUSE");
//        mediaPlayer.pause();
//        ImageButton playPause_btn = findViewById(R.id.playPause_btn);
//
//        playPause_btn.setImageResource(R.drawable.play_icon);
//
//        if(interrupted)
//            state = State.INTERRUPTED;
//        else
//            state = State.PAUSED;
//    }
//
//    private void pause_no_icon_change()
//    {
//        mediaPlayer.pause();
//    }
//
//    private void play()
//    {
//        //need to request audio focus before playing
//        if(requestFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
//        {
//            mediaPlayer.start();
//            ImageButton playPause_btn = findViewById(R.id.playPause_btn);
//
//            playPause_btn.setImageResource(R.drawable.pause_icon);
//            state = State.PLAYING;
//        }
//    }

    private int requestFocus()
    {
        audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();


        audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build();

        return audioManager.requestAudioFocus(audioFocusRequest);
    }

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