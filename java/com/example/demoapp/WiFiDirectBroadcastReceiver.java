package com.example.demoapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.LinkedList;
import java.util.List;


public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    MainActivity mainActivity;
    public static final String TAG = "WiFiDirect";

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity mainActivity) {
        super();
        Log.d(TAG, "WiFiDirectBroadcastReceiver: Created");
        this.manager = manager;
        this.channel = channel;
        this.mainActivity = mainActivity;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Inside the WiFiDirectBroadcastReceiver onReceive");
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            Log.d(TAG, "onReceive: Wifi state changed");
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "Wifi Enabled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Wifi Not Enabled", Toast.LENGTH_LONG).show();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            Log.d(TAG, "onReceive: WiFiDirectBroadcastReceiver peers changed");
            if (manager != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PackageManager.PERMISSION_GRANTED);
                }
                manager.requestPeers(channel, mainActivity.peerListListener);
                Log.d(TAG, "onReceive: WiFiDirectBroadcastReceiver requested peers");
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "onReceive: Wifi P2P connection changed");

            // Respond to new connection or disconnections

            WifiP2pGroup group = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
            Log.d(TAG, "P2P connection changed: group status is "+(group != null));
            if (group != null) {
                WifiP2pDevice groupOwner = group.getOwner();
                Log.d(TAG, "P2P Group Formed: group size is "+ group.getClientList().size());
                //if (!MainActivity.getCurrentDeviceName().equals(groupOwner.deviceName)) {
                if (groupOwner!= null &&
                        !groupOwner.deviceAddress.equals(MainActivity.getCurrentDeviceAddress())) {
                    Log.d(TAG, "onReceive: Group Owner Name: "+ groupOwner.deviceName);
                    MainActivity.setConnectedDeviceName(groupOwner.deviceName);
                } else {

                    List<WifiP2pDevice> groupMembers = new LinkedList<>(group.getClientList());
                    for (WifiP2pDevice member : groupMembers) {
                        if(member!=null){
                            Log.d(TAG, "onReceive: Group Member Name: "+ member.deviceName);
                        }
                        if (member != null &&
                                !member.deviceAddress.equals(MainActivity.getCurrentDeviceAddress())) {
                            //if (MainActivity.getCurrentDeviceName().equals(member.deviceName)) {
                            Log.d(TAG, "onReceive: Connected to: "+ member.deviceName);
                            MainActivity.setConnectedDeviceName(member.deviceName);
                            break;
                        }
                    }
                }
            }
            if(manager != null) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {
                    Log.d(TAG, "onReceive: Called requestConnectionInfo");
                    manager.requestConnectionInfo(channel, mainActivity.connectionInfoListener);
                } else {
                    mainActivity.statustxt.setText("Device Disconnected");
                }
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "onReceive: This device P2P changed WiFiDirectBroadcastReceiver");
            // Respond to this device's wifi state changing
            WifiP2pDevice currentDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            Log.d(TAG, "P2P Device: Obtained device status " + (currentDevice != null));
            if (currentDevice != null) {
                Log.d(TAG, "P2P Device: The current device's name is " + currentDevice.deviceName);
                MainActivity.setCurrentDeviceName(currentDevice.deviceName);
                MainActivity.setCurrentDeviceAddress(currentDevice.deviceAddress);
            }
        }
        else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, 10000);
            if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED)
            {
                Log.d(TAG, "onReceive: Discovery Started");
            }
            else
            {
                // Wifi P2P discovery stopped.
                Log.d(TAG, "onReceive: Wifi P2P discovery stopped");
            }

        }
    }
}
