package com.example.demoapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_ID_1 = "DiscoveryChannel";
    public static final String CHANNEL_ID_2 = "ReceivedMessagesChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel discoverChannel = new NotificationChannel(
                    CHANNEL_ID_1,
                    "Discover Devices Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            discoverChannel.setDescription("Discovering Devices");

            NotificationChannel messagesChannel = new NotificationChannel(
                    CHANNEL_ID_2,
                    "Received Messages Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            discoverChannel.setDescription("You have received a new message");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(discoverChannel);
            manager.createNotificationChannel(messagesChannel);
        }
    }
}