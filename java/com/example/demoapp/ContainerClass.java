package com.example.demoapp;

import android.net.wifi.p2p.WifiP2pManager;

public class ContainerClass {
    public static final String TAG = "Container Class";

    static MainActivity mainActivity;
    static WifiP2pManager manager;
    static WifiP2pManager.Channel channel;


    public static WifiP2pManager getManager() {
        return manager;
    }

    public static WifiP2pManager.Channel getChannel() {
        return channel;
    }

    public static void setWifivariables(WifiP2pManager manager, WifiP2pManager.Channel channel) {
        ContainerClass.manager = manager;
        ContainerClass.channel = channel;
    }

    public static void setMainActivity(MainActivity mainActivity) {
        ContainerClass.mainActivity = mainActivity;
    }

    public static void destroyMainActivityReference() {
        mainActivity = null;
    }

    public static MainActivity getMainActivity() {
        return mainActivity;
    }
}