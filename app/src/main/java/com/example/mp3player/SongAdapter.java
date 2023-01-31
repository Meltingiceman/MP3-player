package com.example.mp3player;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

//The adapter used in the PlayListView class that is used by te recyclerview there
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> implements
        ItemMoveCallback.ItemTouchHelperContract{

    ArrayList<Song> songs;
    Context context;

    public SongAdapter(Context ct, ArrayList<Song> s)
    {
        context = ct;
        songs = s;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.simple_song_list_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        holder.songName.setText(songs.get(holder.getAdapterPosition()).name);

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("DEBUG: CLICK AT POSITION " + holder.getAdapterPosition());
                MusicPlayer.getInstance().playSong(holder.getAdapterPosition());
                //PlayListView.notifyStateChange();
                PlayListView.changeState = true;
            }
        });


    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        Collections.swap(songs, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);

        System.out.println("DEBUG FROMPOSITION: " + fromPosition);
        System.out.println("DEBUG TOPOSITION: " + toPosition);

        System.out.println("DEBUG IN ONROWMOVED: ");
        for(int i = 0;i < songs.size(); i++)
        {
            System.out.println(songs.get(i).name);
        }

        MusicPlayer.getInstance().notifySwap(fromPosition, toPosition);


    }

    @Override
    public void onRowSelected(SongViewHolder myViewHolder) {
        myViewHolder.mainLayout.setBackgroundColor(Color.BLUE);
    }

    @Override
    public void onRowClear(SongViewHolder myViewHolder) {
        myViewHolder.mainLayout.setBackgroundColor(Color.WHITE);
    }

    public class SongViewHolder extends RecyclerView.ViewHolder{

        TextView songName;
        RelativeLayout mainLayout;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            songName = itemView.findViewById(R.id.edit_playlist_title);
            mainLayout = itemView.findViewById(R.id.SongRow);
        }
    }
}
