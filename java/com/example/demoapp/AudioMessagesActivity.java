package com.example.demoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AudioMessagesActivity extends AppCompatActivity {
    private static RecyclerView recyclerView;
    private static RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_messages);
        List<String> audioFilePaths = getAudioFilePaths();
        String[] audioFileNames = getAudioFileNames();

        recyclerView = findViewById(R.id.audio_messages_list);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new RecycleViewAdapter(Arrays.asList(audioFileNames), audioFilePaths, this);
        recyclerView.setAdapter(mAdapter);


    }

    private String[] getAudioFileNames() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File receivedDirectory = new File(musicDirectory.getAbsolutePath() + "/Received");
        receivedDirectory.mkdir();
        String[] fileNames = receivedDirectory.list();
        return fileNames;
    }

    private List<String> getAudioFilePaths() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File receivedDirectory = new File(musicDirectory.getAbsolutePath() + "/Received");
        File[] files = receivedDirectory.listFiles();
        List<String> absoluteFilePaths = new ArrayList<>();
        if(files!=null){
            for(File file : files){
                if(file.isFile()){
                    absoluteFilePaths.add(file.getAbsolutePath());
                }
            }
        }

        return absoluteFilePaths;
    }

    @SuppressLint("NotifyDataSetChanged")
    public static void deleteAudioFile(String absoluteFilePath) {
        File audioFile = new File(absoluteFilePath);
        try {
            audioFile.delete();
        } catch (SecurityException e) {
            Log.d("Delete File", "deleteFile: Cannot delete File");
            e.printStackTrace();
        }
        mAdapter.notifyDataSetChanged();
    }

}
