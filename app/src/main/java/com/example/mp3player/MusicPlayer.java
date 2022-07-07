package com.example.mp3player;

import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayer {

    private State state;
    private ArrayList<Song> playList;
    private MediaPlayer player;
    private int playList_ix;
    private static MusicPlayer instance = new MusicPlayer();

    private MusicPlayer()
    {
        //MusicPlayer is being initialized
        state = State.INIT;
        player = new MediaPlayer();

        playList_ix = -1;
    }

    public void loadPlayList(ArrayList<Song> list)
    {
        //once a playlist is loaded then the MusicPlayer is ready and in the idle state
        playList = list;
        state = State.IDLE;
    }

    public void playSong(int ix)
    {
        if(ix == playList_ix || state == State.INIT || ix < 0 || ix > playList.size())
            return;

        player.reset();
        try {

            playList_ix = ix;
            player.setDataSource(playList.get(playList_ix).path);
            player.prepare();

            player.start();

            state = State.PLAYING;
        }
        catch (IOException e)
        {
            System.out.println("DEBUG: AN IOEXCEPTION WAS THROWN TRYING TO LOAD SONG " +
                  playList.get(playList_ix).name + " ALONG PATH " + playList.get(playList_ix).path);
            return;
        }
    }

    public void pause()
    {
        if (state == State.PLAYING)
        {
            player.pause();
            state = State.PAUSED;
        }
    }

    public void resume()
    {
        if(state == State.PAUSED)
        {
            player.start();
            state = State.PLAYING;
        }
    }

    public String getPlayingSongName()
    {
        if(state != State.IDLE && state != State.INIT)
            return playList.get(playList_ix).name;

        return null;
    }

    public State getState()
    {
        return state;
    }

    public int getPlayListIndex()
    {
        return playList_ix;
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

    public int getCurrentTime()
    {
        if(state != State.INIT && state != State.IDLE)
            return player.getCurrentPosition();

        return -1;
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
 * INTERRUPTED: The Audio focus has changed to something else and will return.
 * LOWERED: The MediaPlayer has lowered the volume and will raise it momentarily, i.e. the gps is talking
 * */
enum State{
    INIT, IDLE, PAUSED, PLAYING, INTERRUPTED, LOWERED
}
