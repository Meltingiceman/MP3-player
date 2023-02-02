package com.example.mp3player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class  EditSongAdapter extends RecyclerView.Adapter<EditSongAdapter.EditSongViewHolder> {

    Song[] songList;
    Context context;

    public EditSongAdapter(Context ct, Song[] s)
    {
        context = ct;
        songList = s;
    }

    @NonNull
    @Override
    public EditSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.edit_pl_list_item, parent, false);
        return new EditSongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditSongViewHolder holder, int position) {
        holder.songName.setText( songList[position].name );
    }

    @Override
    public int getItemCount() {
        return songList.length;
    }

    public class EditSongViewHolder extends RecyclerView.ViewHolder
    {
        TextView songName;
        public EditSongViewHolder(@NonNull View itemView) {
            super(itemView);

            songName = itemView.findViewById(R.id.songName);
        }
    }


}
