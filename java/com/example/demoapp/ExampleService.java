package com.example.demoapp;


import static com.example.demoapp.App.CHANNEL_ID_1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


//public class ExampleService extends Service {
//    public static final String TAG = "ExampleService";
//    private volatile boolean threadRunning;
//    WifiP2pManager manager;
//    WifiP2pManager.Channel channel;
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
//        channel = manager.initialize(this, getMainLooper(), null);
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        String input = intent.getStringExtra("inputExtra");
//        Log.d(TAG, "Reached service");
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0, notificationIntent, 0);
//
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_1)
//                .setContentTitle("Example Service")
//                .setContentText(input)
//                .setSmallIcon(R.drawable.ic_android)
//                .build();
//        Log.d(TAG, "Created Notification");
//        startForeground(1, notification);
//        /*if (!threadRunning) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    for (int i = 0; i < 10000; i++) {
//                        threadRunning = true;
//                        Log.d(TAG, "run: " + i);
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            threadRunning = false;
//                            e.printStackTrace();
//                        }
//                    }
//                    threadRunning = false;
//                }
//            }).start();
//        }*/
//    new Thread(new Runnable() {
//        @Override
//        public void run() {
//            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        PackageManager.PERMISSION_GRANTED);
//            }
//            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
//                @SuppressLint("SetTextI18n")
//                @Override
//                public void onSuccess() {
//                    Toast.makeText(getApplicationContext(),"Discovery Started",Toast.LENGTH_LONG).show();
//                }
//
//                @SuppressLint("SetTextI18n")
//                @Override
//                public void onFailure(int i) {
//                    Toast.makeText(getApplicationContext(),"Discovery Started Failed",Toast.LENGTH_LONG).show();
//                }
//            });
//        }
//    }).start();
//        return START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//}

public class ExampleService extends Service {
    public static final String TAG = "ExampleService";
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    MainActivity mainActivity;
    BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = ContainerClass.getManager();
        channel = ContainerClass.getChannel();
        mainActivity = ContainerClass.getMainActivity();
        broadcastReceiver = new WiFiDirectBroadcastReceiver(manager, channel, mainActivity);
        registerReceiver(broadcastReceiver, intentFilter);
        mainActivity = null;
        ContainerClass.setMainActivity(null);
        Log.d(TAG, "Reached service");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_1)
                .setContentTitle("Discovery Service")
                .setContentText(input)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_android)
                .build();
        Log.d(TAG, "Created Notification");
        startForeground(1, notification);

        new Thread(new Runnable() {
            private static final String TAG = "Discovery Thread";

            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                Log.d(TAG, "run: Discovery Thread run entered");

                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess: Discovery Started.");
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onFailure(int i) {
                        Log.d(TAG, "onFailure: Discovery Failed. Reason " + i);
                    }
                });
            }
        }).start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);


        manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: Stopped discovery");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure: Failed to stop discovery. Reason " + reason);
            }
        });

        //channel.close();
        channel = null;
        manager = null;
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}