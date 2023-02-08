package com.example.mp3player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class  EditSongAdapter extends RecyclerView.Adapter<EditSongAdapter.EditSongViewHolder> {

    ArrayList<Song> songList;
    ArrayList<String> checked;
    Context context;

    public EditSongAdapter(Context ct, ArrayList<Song> s)
    {
        context = ct;
        songList = s;
        checked = new ArrayList<>();
    }

    @NonNull
    @Override
    public EditSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.edit_pl_list_item, parent, false);

        EditSongViewHolder viewHolder = new EditSongViewHolder(view);

        view.setOnClickListener(view1 -> viewHolder.checked.setChecked(!viewHolder.checked.isChecked()));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EditSongViewHolder holder, int position, @NonNull List<Object> payloads) {

        super.onBindViewHolder(holder, position, payloads);

        holder.checked.setOnCheckedChangeListener(null);
        holder.checked.setChecked(checked.contains(holder.songName.getText().toString()));
        holder.checked.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
                checked.add(holder.songName.getText().toString());
            else
                checked.remove(holder.songName.getText().toString());
        });
    }

    @Override
    public void onBindViewHolder(@NonNull EditSongViewHolder holder, int position) {
        holder.songName.setText( songList.get(position).name );
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class EditSongViewHolder extends RecyclerView.ViewHolder
    {
        TextView songName;
        CheckBox checked;
        public EditSongViewHolder(@NonNull View itemView) {
            super(itemView);

            songName = itemView.findViewById(R.id.songName);
            checked = itemView.findViewById(R.id.edit_playlist_item_checkbox);

        }
    }


}
