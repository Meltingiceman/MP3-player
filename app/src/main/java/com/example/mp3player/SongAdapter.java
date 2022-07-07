package com.example.mp3player;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

//The adapter used in the PlayListView class that is used by te recyclerview there
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    Song[] songs;
    Context context;

    public SongAdapter(Context ct, Song[] s)
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
        holder.songName.setText(songs[position].name);

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("DEBUG: CLICK AT POSITION " + holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.length;
    }

    public class SongViewHolder extends RecyclerView.ViewHolder{

        TextView songName;
        ConstraintLayout mainLayout;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            songName = itemView.findViewById(R.id.name);
            mainLayout = itemView.findViewById(R.id.SongRow);
        }
    }
}
