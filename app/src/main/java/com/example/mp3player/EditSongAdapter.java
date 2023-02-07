package com.example.mp3player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
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

        CheckBox c = view.findViewById(R.id.edit_playlist_item_checkbox);
        String plName = ( (TextView)view.findViewById(R.id.songName) ).getText().toString();
        EditSongViewHolder viewHolder = new EditSongViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                c.setChecked(!c.isChecked());
                viewHolder.checked.setChecked(!viewHolder.checked.isChecked());
            }
        });

        viewHolder.checked.setOnCheckedChangeListener(null);
        viewHolder.checked.setChecked(checked.contains(viewHolder.songName.toString().toLowerCase()));
        viewHolder.checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                    checked.add(viewHolder.songName.toString().toLowerCase());
                else
                    checked.remove(viewHolder.songName.toString().toLowerCase());
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EditSongViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        holder.checked.setOnCheckedChangeListener(null);
        holder.itemView.setOnClickListener(null);

        //TODO: I started working on this but I didn't finish (this was about the checkboxes not staying checked when searching)

    }

    @Override
    public void onBindViewHolder(@NonNull EditSongViewHolder holder, int position) {
        holder.songName.setText( songList.get(position).name );
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public class EditSongViewHolder extends RecyclerView.ViewHolder
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
