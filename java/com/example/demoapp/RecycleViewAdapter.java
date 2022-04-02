package com.example.demoapp;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.LinkedList;
import java.util.List;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.MyViewHolder> {
    public static final String TAG = "Adapter";

    static List<String> audiofileNames;
    static List<String> audioFilePaths;
    static Context context;

    static int removeIndex = -1;



    public RecycleViewAdapter(List<String> audiofileNames, List<String> audioFilePaths, Context context) {
        this.audiofileNames = new LinkedList<>();
        this.audiofileNames.addAll(audiofileNames);
        this.audioFilePaths = audioFilePaths;
        RecycleViewAdapter.context = context;
    }

    public static void removeFileIfNeeded() {
        if (removeIndex > -1) {
            audioFilePaths.remove(removeIndex);
            audiofileNames.remove(removeIndex);
            Log.d(TAG, "removeFileIfNeeded: Called and removed position " + removeIndex);
        }
        removeIndex = -1;
    }

    public static void setRemoveIndex(int position) {
        removeIndex = position;
        Log.d(TAG, "setRemoveIndex: Called on position " + position);
        removeFileIfNeeded();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_line_audio_file, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        Log.d(TAG, "onCreateViewHolder: Called");
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tv_file_name.setText(audiofileNames.get(position));
        holder.position = position;
        holder.fileName = audiofileNames.get(position);
        holder.absoluteFilePath = audioFilePaths.get(position);
        Log.d(TAG, "onBindViewHolder: Called position " + position);
    }


    @Override
    public int getItemCount() {
        return audiofileNames.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_file_name;
        Button btn_play_file;
        Button btn_delete_file;
        int position;
        String fileName;
        String absoluteFilePath;

        MediaPlayer mediaPlayer;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_file_name = itemView.findViewById(R.id.tv_file_name);
            btn_play_file = itemView.findViewById(R.id.btn_play_file);
            btn_delete_file = itemView.findViewById(R.id.btn_delete_file);

            btn_play_file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: Clicked play button on position " + position +
                            "\n which is file " + fileName);

                    try {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(absoluteFilePath);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        Log.d(TAG, "onClick: Recording Playing");
                        Toast.makeText(context.getApplicationContext(), "Recording Playing", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();

                    }

                }
            });

            btn_delete_file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: Clicked delete button on position " + position +
                            "\n which is file " + fileName);
                    setRemoveIndex(position);
                    AudioMessagesActivity.deleteAudioFile(absoluteFilePath);

                }
            });
        }


    }
}
