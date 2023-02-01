package com.example.mp3player;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AddSongDialogFragment extends DialogFragment
{

    public interface AddSongDialogListener
    {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    AddSongDialogListener listener;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        try {
            listener = (AddSongDialogListener) context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(getActivity().toString() + " must implement listener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Dialog_Alert);
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View dialog_layout = inflater.inflate(R.layout.dialog_add_playlist, null);

        builder.setView(dialog_layout)
            .setPositiveButton("Create playlist", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    listener.onDialogPositiveClick(AddSongDialogFragment.this);
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    listener.onDialogNegativeClick(AddSongDialogFragment.this);
                }
            });

        return builder.create();
    }
}
