package com.example.mp3player;

import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

//Meant to be a Singleton object
public class MusicPlayer {

    private State state;
    private ArrayList<Song> playList;
    private MediaPlayer player;
    private int song_ix;
    private static MusicPlayer instance = new MusicPlayer();

    private MusicPlayer()
    {
        //MusicPlayer is being initialized
        state = State.INIT;
        player = new MediaPlayer();

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                System.out.println("DEBUG: PLAYING NEXT SONG");
                nextSong();
                PlayListView.notifyStateChange();
            }
        });
    }

    public void loadPlayList(ArrayList<Song> list)
    {
        //once a playlist is loaded then the MusicPlayer is ready and in the idle state
        playList = list;
        state = State.IDLE;
        song_ix = -1;
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

            state = State.PLAYING;
        }
        catch (IOException e)
        {
            System.out.println("DEBUG: AN IOEXCEPTION WAS THROWN TRYING TO LOAD SONG " +
                  playList.get(song_ix).name + " ALONG PATH " + playList.get(song_ix).path);
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
            return playList.get(song_ix).name;

        return null;
    }

    //returns the state of the MusicPlayer
    public State getState()
    {
        return state;
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
        if(song_ix <= 0)
        {
            song_ix = playList.size() - 1;
        }
        else
        {
            song_ix--;
        }

        playSong(song_ix);
    }

    public void shuffle()
    {
        if(state == State.PLAYING)
        {
            System.out.println("DEBUG: SHUFFLING WHILE PLAYING");
            Song temp = playList.get(song_ix);
            ArrayList<Song> tempList = new ArrayList<Song>();

            tempList.add(temp);
            Collections.shuffle(playList);

            for(int i = 0; i < playList.size(); i++)
            {
                if(!playList.get(i).name.equals(temp.name))
                {
                    tempList.add(playList.get(i));
                }
            }

            playList = tempList;
            song_ix = 0;
            //DON'T playSong() here or it will restart the current playing song
        }
        else if(state == State.PAUSED || state == State.IDLE)
        {
            Collections.shuffle(playList);
            song_ix = 0;
            playSong(song_ix);
        }
    }

    public void reset()
    {
        song_ix = -1;
        state = State.IDLE;
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
