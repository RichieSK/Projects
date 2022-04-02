package com.example.demoapp;

import static com.example.demoapp.App.CHANNEL_ID_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final int PORT_NO = 46852;
    private static String connectedDeviceName;
    private static String currentDeviceName;
    private static String currentDeviceAddress;

    private static String randomString;
    TextView statustxt;
    //Button discoverbtn;
    Button wifibtn;
    Button sendbtn;
    Button startJob;
    Button stopJob;
    TextView tvbox;
    WifiManager wifiManager;
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
   // BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;
    ListView listView;

    static final int MESSAGE_READ = 1;
    static final int MESSAGE_FINISHED = 2;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    ServerThread serverThread;
    ClientThread clientThread;
    SendReceive sendReceive;

    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;

    ServerSocket serverSocket;

    private NotificationManagerCompat notificationManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statustxt = findViewById(R.id.statustext);
       // discoverbtn = findViewById(R.id.discoverbtn);
        startJob = findViewById(R.id.startJob);
        stopJob = findViewById(R.id.stopJob);
        listView = findViewById(R.id.listview);
        wifibtn = findViewById(R.id.wifibtn);
        sendbtn = findViewById(R.id.sendbtn);
        tvbox = findViewById(R.id.tvbox);

        try {
            serverSocket = new ServerSocket(PORT_NO);
            serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
       // broadcastReceiver = new WiFiDirectBroadcastReceiver(manager, channel, this);

        notificationManager = NotificationManagerCompat.from(this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        Log.d(TAG, "onCreate: called oncreate");
        randomString = new RandomString(8).nextString();


//        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this, MainActivity.class));

        wifibtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    wifibtn.setText("Wifi ON");
                } else {
                    wifiManager.setWifiEnabled(true);
                    wifibtn.setText("Wifi OFF");
                }
            }
        });
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PackageManager.PERMISSION_GRANTED);
//        discoverbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "onClick: discovery btn clicked");
//                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                            PackageManager.PERMISSION_GRANTED);
//                }
//                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
//                    @SuppressLint("SetTextI18n")
//                    @Override
//                    public void onSuccess() {
//                        statustxt.setText("Discovery Started..");
//                    }
//
//                    @SuppressLint("SetTextI18n")
//                    @Override
//                    public void onFailure(int i) {
//                        statustxt.setText("Discovery Starting Failed..");
//                    }
//                });
//            }
//        });


        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.d(TAG, "onClick: sendbtn clicked..");
                    File f = new File(getRecordingFilePath());
                    //File f = new File("Phone storage/Document/Word to PDF/test.txt");
                    byte[] msg = getBytes(f);
//                    while (sendReceive == null) {
//                        try {
//                            Thread.sleep(50);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    if (sendReceive == null) {
                        Toast.makeText(getApplicationContext(), "Please try to send again.", Toast.LENGTH_LONG).show();
                    } else {
                        sendReceive.write(msg);
                        Toast.makeText(getApplicationContext(), "Data Sent Successfully", Toast.LENGTH_LONG).show();
                    }

                    Toast.makeText(getApplicationContext(), "Data Sent Successfully", Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Data not sent", Toast.LENGTH_LONG).show();
                } catch (Throwable e) {
                    StringWriter errors = new StringWriter();
                    e.printStackTrace(new PrintWriter(errors));
                    Toast.makeText(getApplicationContext(), errors.toString(), Toast.LENGTH_LONG).show();
                }


            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.d(TAG, "onItemClick: listview clicked...");
                final WifiP2pDevice device = deviceArray[position];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PackageManager.PERMISSION_GRANTED);
                }
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess: connected with " + device.deviceName);
                        statustxt.setText("Connected " + device.deviceName);
                        connectedDeviceName = device.deviceName;
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "onFailure: Failed to connect "+device.deviceName);
                        statustxt.setText("Not connected");
                    }
                });
            }
        });
        if (isMicrophonepresent()) {
            getMicrophonePermission();
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

    }

    public void btnRecordPressed(View v) {

        try {

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mediaRecorder.setOutputFile(getRecordingFilePath());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.prepare();
            mediaRecorder.start();

            Toast.makeText(this, "Recording", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void btnStopPressed(View v) {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        Toast.makeText(this, "Recording Stopped", Toast.LENGTH_LONG).show();


    }

    public void btnPlayPressed(View v) {
        try {

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getRecordingFilePath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            Toast.makeText(this, "Recording Playing", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    private boolean isMicrophonepresent() {
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    private void getMicrophonePermission() {
        int MICROPHONE_PERMISSION_CODE = 25;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
    }

    private String getRecordingFilePath() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File sentDirectory = new File(musicDirectory.getAbsolutePath() + "/Sent");
        sentDirectory.mkdir();
        //File file = new File(musicDirectory, "test.txt");
        File file = new File(sentDirectory, randomString+ ".amr");
        return file.getAbsolutePath();
    }

    private String getRecievedFilePath() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File receivedDirectory = new File(musicDirectory.getAbsolutePath() + "/Received");
        receivedDirectory.mkdir();
        File file = new File(receivedDirectory, randomString + ".amr");
        return file.getAbsolutePath();
    }


    public static byte[] getBytes(File f) throws IOException {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(f);
        int read;
        while ((read = fis.read(buffer)) != -1) {
            os.write(buffer, 0, read);
        }
        fis.close();
        byte[] bytearr = os.toByteArray();
        os.close();
        byte[] fileLength = bigIntToByteArray(bytearr.length);
        byte[] fileByteArr = new byte[fileLength.length + bytearr.length];
        System.arraycopy(fileLength, 0, fileByteArr, 0, fileLength.length);
        System.arraycopy(bytearr, 0, fileByteArr, fileLength.length, bytearr.length);
        Log.d(TAG, "getBytes: Total size of file is " + fileByteArr.length +
                "\nSize of fileLength is " + fileLength.length +
                "\nThe size of the file is " + ByteBuffer.wrap(fileByteArr, 0,  4).getInt());
        return fileByteArr;
    }

    private static byte[] bigIntToByteArray( final int i ) {
        //Convert Integer to a byte array of size 4
        BigInteger bigInt = BigInteger.valueOf(i);
        byte[] bigIntArr = bigInt.toByteArray();
        byte[] byteArr = new byte[4];
        System.arraycopy(bigIntArr, 0, byteArr, 4-bigIntArr.length, bigIntArr.length);
        return byteArr;
    }

    public static synchronized void toFile(byte[] data, File destination) {
        try (FileOutputStream fos = new FileOutputStream(destination, true)) {
            fos.write(data);
            fos.flush();
            Log.d(TAG, "Written Packet of size: " + data.length);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.d(TAG, "Error in Recieve" + errors.toString());

        }
    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            Log.d(TAG, "handleMessage: Handler called ");
            if (message.what == MESSAGE_READ) {
                byte[] readBuffer = Arrays.copyOfRange((byte[]) message.obj, 0, message.arg1);
                File destination = new File(getRecievedFilePath());
                toFile(readBuffer, destination);
                Log.d(TAG, "Finished reading packet " + message.arg2);
            }
            return false;
        }
    });

    public class ServerThread extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        public ServerThread(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            try {
                Log.d(TAG, "run: serverthread run called ");
                //serverSocket = new ServerSocket(46352);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public class ClientThread extends Thread {
        Socket socket;
        String hostAdd;

        public ClientThread(InetAddress hostAddress) {
            Log.d(TAG, "ClientThread: Client constructor called ");
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            Log.d(TAG, "run: Clientthread run called ");
            int retries = 20;
            for (int i = 0; i < retries; i++) {
                try {
                    socket.connect(new InetSocketAddress(hostAdd, PORT_NO), 1000);

                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                    Log.d(TAG, "run: client thread finished ");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    socket = new Socket();
                }
            }


        }
    }



    public class SendReceive extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;
        public static final String TAG = "Packets";

        public SendReceive(Socket skt) {
            Log.d(TAG, "SendReceive: Sendreceive constructor called ");
            socket = skt;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                Log.d(TAG, "SendReceive: Streams initialized");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            Log.d(TAG, "run: Inside the serverthread run ");
            byte[] buffer = new byte[32000];
            int bytes, packetNo = 0, totalSize = 0, presentSize = 0;
            //boolean firstRead = true;

            while (socket != null) {
                try {

                    bytes = inputStream.read(buffer);
                    if (totalSize == 0 && bytes > 0) {
                        totalSize = ByteBuffer.wrap(buffer, 0, 4).getInt();
                        //Log.d(TAG, "Total size of file is " + totalSize + " bytes");
                        //firstRead = false;
                        buffer = Arrays.copyOfRange(buffer, 4, buffer.length);
                        handler.obtainMessage(MESSAGE_READ, bytes - 4, ++packetNo, buffer.clone()).sendToTarget();
                        Log.d(TAG, "Reading packet " + packetNo);
                        presentSize += bytes - 4;
                    } else if (bytes > 0 && presentSize < totalSize) {
                        handler.obtainMessage(MESSAGE_READ, bytes, ++packetNo, buffer.clone()).sendToTarget();
                        Log.d(TAG, "Reading packet " + packetNo);
                        presentSize += bytes;
                        Log.d(TAG, "Read " +presentSize + " bytes so far.");
                    }
                    if (totalSize != 0 && totalSize == presentSize) {
                        socket.close();
                        //Toast.makeText(getApplicationContext(), "File received. Socket closed", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Received file. Socket closed.");
                        receivedMessageNotification();
                        packetNo = 0;
                        totalSize = 0;
                        presentSize = 0;
                        break;
                    }
                } catch (IOException e) {
                    StringWriter errors = new StringWriter();
                    e.printStackTrace(new PrintWriter(errors));
                    Log.d(TAG, errors.toString());
                }

            }
        }

        public void write(byte[] bytes) {

            try {
                Log.d(TAG, "write: Inside the sendrecieve write");
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            int packetSize = 1024;
                            double nosofpackets = Math.ceil(((double) bytes.length) / packetSize);
                            int i;
                            for (i = 0; i < nosofpackets; ++i) {
                                byte[] mybytearray;
                                try {
                                    if (i == nosofpackets - 1) {
                                        mybytearray = Arrays.copyOfRange(bytes, i * packetSize, bytes.length);
//                                        Log.d(TAG, "This packet goes from " +
//                                                (i * packetSize) + " to " + bytes.length);
                                    } else {
                                        mybytearray = Arrays.copyOfRange(bytes, i * packetSize, (i + 1) * packetSize);
//                                        Log.d(TAG, "This packet goes from " +
//                                                (i * packetSize) + " to " + ((i + 1) * packetSize));
                                    }
                                    outputStream.write(mybytearray, 0, mybytearray.length);
                                    outputStream.flush();
                                    Log.d(TAG, "Packet" + i + "of size " + mybytearray.length + "Successfully Written" +
                                            "\n The total length of bytes array is " + bytes.length);

                                } catch (ArrayIndexOutOfBoundsException e) {
                                    // do nothing
                                    Log.d(TAG, "Array Out of bounds in packet " + i + ". It goes from " +
                                            (i * packetSize) + " to " + ((i + 1) * packetSize) +
                                            "\n The total length of bytes array is " + bytes.length);
                                }
                                //Toast.makeText(getApplicationContext(), "Successfully written packet", Toast.LENGTH_LONG).show();


                            }Log.d(TAG, "run: Finished writing packets");

                        } catch (IOException e) {
                            StringWriter errors = new StringWriter();
                            e.printStackTrace(new PrintWriter(errors));
                            Log.d(TAG, errors.toString());
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();
                thread.join();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Socket Closing Thread: Socket closed state is " + socket.isClosed());
                        Log.d(TAG, "Socket Closing Thread: Socket connected state is " + socket.isConnected());
                        if (!socket.isClosed()) {

                            while(socket.isConnected()) {
                                try {
                                    if (socket.getInputStream().read() == -1) {
                                        Log.d(TAG, "Socket Closing Thread: Socket connection closed on the other end");
                                        break;
                                    } else {
                                        Thread.sleep(1000);
                                    }
                                } catch(InterruptedException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            restartResources();
                            Log.d(TAG, "Finished sending. Closed socket.");
                        } else {
                            restartResources();
                            Log.d(TAG, "Finished sending. Socket was already closed.");
                        }
                    }
                }).start();

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerlist) {
            Log.d(TAG, "onPeersAvailable: Peers available..");
            if (!peerlist.getDeviceList().equals(peers)) {
                peers.clear();
                Log.d(TAG, "onPeersAvailable: new peers list available");
                peers.addAll(peerlist.getDeviceList());

                deviceNameArray = new String[peerlist.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerlist.getDeviceList().size()];

                int index = 0;
                for (WifiP2pDevice device : peerlist.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                    Log.d(TAG, "onPeersAvailable: " + device.deviceName+ " available");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.custom_text_view, deviceNameArray);
                listView.setAdapter(adapter);
                if (peers.size() == 0) {
                    Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            Log.d(TAG, "onConnectionInfoAvailable: Called connection info listener");
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                Log.d(TAG, "onConnectionInfoAvailable: Host connected");
                statustxt.setText("Host " + connectedDeviceName);
                serverThread = new ServerThread(serverSocket);
                serverThread.start();
            } else if (wifiP2pInfo.groupFormed) {
                Log.d(TAG, "onConnectionInfoAvailable: Client connected");
                statustxt.setText("Client " + connectedDeviceName);
                clientThread = new ClientThread(groupOwnerAddress);
                clientThread.start();
            }
            else{
                Log.d(TAG, "onConnectionInfoAvailable: No group formed");
            }
        }
    };


    /* register the broadcast receiver with the intent values to be matched */

    @Override
    protected void onResume() {
        super.onResume();
        //registerReceiver(broadcastReceiver, intentFilter);
        Log.d(TAG, "onResume: Inside main activity on resume");
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(broadcastReceiver);
        Log.d(TAG, "onPause: Inside main activity on Pause");
    }

//    public void startService(View v) {
//        Log.d(TAG, "startService: Service started");
//        String input = "Service Started";
//
//        Intent serviceIntent = new Intent(this, ExampleService.class);
//        serviceIntent.putExtra("inputExtra", input);
//
//        startService(serviceIntent);
//    }

    public void startService(View v) {
        String input = "Discovery in Progress";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PackageManager.PERMISSION_GRANTED);
        }

        //ContainerClass container = new ContainerClass(this, manager, channel);
        ContainerClass.setWifivariables(manager, channel);
        ContainerClass.setMainActivity(this);
        Intent serviceIntent = new Intent(this, ExampleService.class);
        serviceIntent.putExtra("inputExtra", input);
        startService(serviceIntent);
    }

    public void stopService(View v) {
        Log.d(TAG, "stopService: Service stopped");
        Intent serviceIntent = new Intent(this, ExampleService.class);
        stopService(serviceIntent);
//        manager.cancelConnect(channel, null);
//       // channel.close();
//       // unregisterReceiver(broadcastReceiver);
//        //Restart everything
//        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
//        channel = manager.initialize(this, getMainLooper(), null);
//        connectedDeviceName = "";
    }

    public void receivedMessageNotification() {
        Log.d(TAG, "receivedMessageNotification: Inside recieve notification");
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, activityIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_1)
                .setContentTitle("Discovery Service")
                .setContentText("You have received a new message")
                .setSmallIcon(R.drawable.ic_message)
                .setColor(Color.BLUE)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();
        notificationManager.notify(2, notification);
        restartResources();
    }

    //private void restartResources() {
//        stopService(stopJob);
//        manager.cancelConnect(channel, null);
//        channel.close();
//        //unregisterReceiver(broadcastReceiver);
//        //Restart everything
//        startService(startJob);
//        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
//        channel = manager.initialize(this, getMainLooper(), null);
//        broadcastReceiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
//        registerReceiver(broadcastReceiver, intentFilter);
//        connectedDeviceName = "";
//        Log.d(TAG, "restartResources: called restart resources..");
    private void restartResources() {
        randomString = new RandomString(8).nextString();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PackageManager.PERMISSION_GRANTED);
        }

        manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group != null && manager != null && channel != null) {
                    manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "removeGroup onSuccess -");
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d(TAG, "removeGroup onFailure -" + reason);
                        }
                    });
                }
            }
        });
        manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: cancel connect successfull");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure: cancel connect failed "+reason);
            }
        });
        stopService(stopJob);
        //channel.close();

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        
        //Restart everything
        startService(startJob);
        connectedDeviceName = "";
    }

    @Override
    protected void onDestroy() {
        if (!serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ContainerClass.destroyMainActivityReference();
        super.onDestroy();

    }

    public static void setConnectedDeviceName(String connectedDeviceName) {
        MainActivity.connectedDeviceName = connectedDeviceName;
    }

    public void viewMessagesActivity(View view) {
        Intent intent = new Intent(this, AudioMessagesActivity.class);
        startActivity(intent);
    }

    public static String getCurrentDeviceName() {
        return currentDeviceName;
    }

    public static void setCurrentDeviceName(String currentDeviceName) {
        MainActivity.currentDeviceName = currentDeviceName;
    }

    public static String getCurrentDeviceAddress() {
        return currentDeviceAddress;
    }

    public static void setCurrentDeviceAddress(String currentDeviceAddress) {
        MainActivity.currentDeviceAddress = currentDeviceAddress;
    }
}