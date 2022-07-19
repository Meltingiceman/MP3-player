package com.example.mp3player;

import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.MediaPlayer;
import android.media.AudioManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

//Meant to be a Singleton object
public class MusicPlayer {

    private State state;
    private State previousState;

    private ArrayList<Song> playList;
    private MediaPlayer player;
    private int song_ix;
    private static MusicPlayer instance = new MusicPlayer();



    private MusicPlayer()
    {
        //MusicPlayer is being initialized
        state = State.INIT;
        previousState = null;
        player = new MediaPlayer();

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        player.setAudioAttributes(attributes);
    }

    public void loadPlayList(ArrayList<Song> list) {
        //once a playlist is loaded then the MusicPlayer is ready and in the idle state
        playList = list;
        //state = State.IDLE;
        changeState(State.IDLE);

        song_ix = -1;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener)
    {
        player.setOnCompletionListener(listener);
    }

    public void playSong(int ix)
    {
        if(ix == song_ix || state == State.INIT || ix < 0 || ix > playList.size()) {
            System.out.println("DEBUG: MUSICPLAYER IS NOT INITIALIZED RETURNING");
            System.out.println("SONG_IX: " +song_ix);
            System.out.println("IX: " + ix);
            System.out.println("STATE: " + state);
            return;
        }
        player.reset();
        try {

            song_ix = ix;
            player.setDataSource(playList.get(song_ix).path);
            player.prepare();

            player.start();

            //state = State.PLAYING;
            changeState(State.PLAYING);
        }
        catch (IOException e)
        {
            System.out.println("DEBUG: AN IOEXCEPTION WAS THROWN TRYING TO LOAD SONG " +
                  playList.get(song_ix).name + " ALONG PATH " + playList.get(song_ix).path);
            return;
        }

        player.setOnCompletionListener(mediaPlayer -> {
            System.out.println("DEBUG: PLAYING NEXT SONG");
            MusicPlayer.this.nextSong();
            PlayListView.changeState = true;
        });
    }

    public void pause(boolean interrupted) {
        if (interrupted) {
            changeState(State.INTERRUPTED);
        }

        pause();
    }

    public void pause()
    {
        if (state == State.PLAYING)
        {
            player.pause();
            //state = State.PAUSED;
            changeState(State.PAUSED);
        }
    }

    public void resume()
    {
        if(state == State.PAUSED || state == State.INTERRUPTED)
        {
            player.start();
            //state = State.PLAYING;
            changeState(State.PLAYING);
        }
    }

    public String getPlayingSongName()
    {
        if(state != State.IDLE && state != State.INIT)
            return playList.get(song_ix).name;

        return null;
    }

    //returns the state of the MusicPlayer
    public State getState()
    {
        return state;
    }

    public State getPreviousState()
    {
        return previousState;
    }

    public int getPlayListIndex()
    {
        return song_ix;
    }

    public int getPlaylistLength()
    {
        return playList.size();
    }

    public int getSongMax()
    {
        if(state != State.INIT && state != State.IDLE)
            return player.getDuration();

        return -1;
    }

    //get the current time on the music
    public int getCurrentTime()
    {
        if(state != State.INIT && state != State.IDLE)
            return player.getCurrentPosition();

        return -1;
    }

    public void goToTime(int time)
    {
        player.seekTo(time);
    }

    public void nextSong()
    {
        //if the musicplayer hasn't been played or isn't initialized then ignore the call
        if(state == State.INIT || state == State.IDLE)
            return;

        System.out.println("DEBUG PLAYSONG SONG_IX: " + song_ix);

        if(song_ix == playList.size() - 1)
        {
            playSong(0);
        }
        else
        {
            playSong(song_ix + 1);
        }
    }

    public void previousSong()
    {
        //if the musicplayer hasn't been played or isn't initialized then ignore the call
        if(state == State.INIT || state == State.IDLE)
            return;

        //DON'T change song_ix here, let playSong change song_ix
        if(song_ix <= 0)
        {
            playSong(playList.size() - 1);
        }
        else
        {
            playSong(song_ix - 1);
        }
    }

    public void shuffle()
    {
        /* If the MusicPlayer is playing then move the currently playing song to the top of the playList
        * and shuffle the rest of the list
        * */
        if(state == State.PLAYING)
        {
            System.out.println("DEBUG: SHUFFLING WHILE PLAYING");
            Song temp = playList.get(song_ix);

            //shuffle the playList
            Collections.shuffle(playList);

            //look for the index of the currently playing song
            for(int i = 0; i < playList.size(); i++)
            {
                if(playList.get(i).name.equals(temp.name))
                {
                    //swap the playing song to the top of the list and exit the loop
                    Collections.swap(playList, 0, i);
                    break;
                }
            }

            song_ix = 0;
            //DON'T playSong() here or it will restart the current playing song
        }
        /* If the musicPlayer is not playing then just shuffle the list and play the first song */
        else if(state == State.PAUSED || state == State.IDLE)
        {
            System.out.println("DEBUG: SHUFFLING WHILE PAUSED OR IDLE");
            Collections.shuffle(playList);
            song_ix = -1;
            playSong(0);
        }
    }

    public void reset()
    {
        song_ix = -1;
        state = State.IDLE;
        previousState = null;
    }

    // a method used internally to change the state of the music player and keeping track of the previous state
    private void changeState(State newState)
    {
        if(newState == state)
            return;

        previousState = state;
        state = newState;
    }

    //code to be executed by the MusicPlayer if the order of the songs are changed
    public void notifySwap(int fromPosition, int toPosition)
    {
        if(fromPosition == song_ix && (state == State.PLAYING || state == State.PAUSED))
        {
            song_ix = toPosition;
        }
    }

    public static MusicPlayer getInstance()
    {
        return instance;
    }
}
/*
 * IDLE: the activity just started and hasn't played any music yet.
 * PAUSED: the mediaplayer has been paused by the user.
 * PLAYING: The MediaPlayer is playing music.
 * INTERRUPTED: The Audio focus has changed to something else and will return. This usually happens.
 *                 when something like a phone call occurs.
 * LOWERED: The MediaPlayer has lowered the volume and will raise it momentarily, i.e. the gps is talking.
 * */
enum State{
    INIT, IDLE, PAUSED, PLAYING, INTERRUPTED, LOWERED
}
