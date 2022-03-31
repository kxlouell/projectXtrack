package com.pd.projectxtrack.WifiDirectTools;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.pd.projectxtrack.MainActivity;
import com.pd.projectxtrack.BroadcastRecievers.WiFiDirectBroadcastReciever;
import com.pd.projectxtrack.Adapters.*;
import com.pd.projectxtrack.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class P2pDiscovery extends HandlerThread {

    public static final String TAG = "P2pDiscovery";
    public static final int INIT_WORK = 0;
    public static final int DISCOVER_SERVICE = 1;
    public static final int START_REGISTRATION = 2;
    public Handler handler;
    private Map record;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Context context;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private HashMap<String, String> buddies;
    private List<WifiP2pDevice> peers;
    private MainActivity mainActivity;
    private WiFiDirectBroadcastReciever wfdBroadcastReciever;
    private IntentFilter intentFilter;
    private ConnectivityManager connectivityManager;
    private wfdPeerDeviceAdapter wfdPeerDeviceAdapter;

    public P2pDiscovery(String name, int priority, MainActivity mainActivity) {
        //will be run outside the class
        super(name, priority);
        this.mainActivity = mainActivity;
        context = this.mainActivity.getApplicationContext();

    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {

        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {

                switch (msg.what) {
                    case START_REGISTRATION:
                        startRegistration(android.os.Build.MODEL);
                        break;
                    case DISCOVER_SERVICE:
                        discoverService();
                        break;
                    case INIT_WORK:
                        initialWork();

                        break;
                }
            }
        };
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.scanWFD.setEnabled(false);
                mainActivity.scanWFD.setText("Initializing..");
            }
        });
    }



    private void initialWork(){
        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        buddies = new HashMap<String, String>();
        peers = new ArrayList<WifiP2pDevice>();
        wfdBroadcastReciever = new WiFiDirectBroadcastReciever(this,mainActivity,manager, channel,connectivityManager);
        channel = manager.initialize(context, getLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "Disconnected to WifiP2pManager");
            }
        });
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        context.registerReceiver(wfdBroadcastReciever,intentFilter);
        Log.d(TAG, "InitWork Done");

    }

    private void startRegistration(String name) {
        Log.d(TAG, "startRegistration: Called!");
        //  Create a string map containing information about your service.
        record = new HashMap();
        record.put("listenport", "5555");
        record.put("buddyname", name + (int) (Math.random() * 1000));
        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("Xtrack", "_presence._tcp", record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            Log.d(TAG, "startRegistration: requesting Permission");
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.scanWFD.setEnabled(true);
                    mainActivity.scanWFD.setText("Retry");
                }
            });
        }else {
            manager.addLocalService(channel, serviceInfo, new ActionListener() {
                @Override
                public void onSuccess() {
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainActivity.scanWFD.setEnabled(true);
                            mainActivity.scanWFD.setText("Scan WFD");
                        }
                    });
                    Log.d(TAG, "Local Service Added");
                }

                @Override
                public void onFailure(int arg0) {

                    Log.d(TAG, "Failed to Add Local Service");
                }
            });
        }

    }



    private void discoverService() {
        Log.d(TAG, "discoverService: Called!");
        peers.clear();
        DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {
            /* Callback includes:
             * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
             * record: TXT record dta as a map of key/value pairs.
             * device: The device running the advertised service.
             */
            @Override
            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {
                Log.d(TAG, "DnsSdTxtRecord available -" + record.toString());
                buddies.put(device.deviceAddress, (String) record.get("buddyname"));
            }
        };


        DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                // Update the device name with the human-friendly version from
                // the DnsTxtRecord, assuming one arrived.
                resourceType.deviceName = buddies
                        .containsKey(resourceType.deviceAddress) ? buddies
                        .get(resourceType.deviceAddress) : resourceType.deviceName;

                // Add to the custom adapter defined specifically for showing
                // wifi devices.
                if (peers.contains(resourceType)) {
                    Toast.makeText(context, "Peer already in List", Toast.LENGTH_SHORT);
                } else {
                    peers.add(resourceType);
                }
                Log.d(TAG, "onBonjourServiceAvailable " + instanceName);

                //wfdPeerDeviceAdapter = new wfdPeerDeviceAdapter(context, peers);
                ArrayAdapter arrayAdapter = new ArrayAdapter(context, R.layout.wfd_peer_devices, R.id.peer_devicename,peers.toArray());
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.wfdListView.setAdapter(arrayAdapter);
                    }
                });

            }


        };
        manager.setDnsSdResponseListeners(channel, servListener, txtListener);
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel,
                serviceRequest,
                new ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Success!
                        Log.d(TAG, "onSuccess: Service Request Success!");
                    }

                    @Override
                    public void onFailure(int code) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                        Log.d(TAG, "onFailure: Service Request Failed!");
                    }
                });
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            Log.d(TAG, "discoverService: returning requesting permission");
        } else {
            Log.d(TAG, "discoverService: in manager.discoverService");
            manager.discoverServices(channel, new ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(context, "Discovery Started", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int code) {
                    // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    switch(code){
                        case WifiP2pManager.P2P_UNSUPPORTED:
                            Toast.makeText(context, "P2P_UNSUPPORTED", Toast.LENGTH_SHORT).show();
                            break;
                            case WifiP2pManager.ERROR:
                                Toast.makeText(context, "P2P_ERROR", Toast.LENGTH_SHORT).show();
                            break;
                            case WifiP2pManager.BUSY:
                                Toast.makeText(context, "P2P_BUSY", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
    }


    @Override
    public void interrupt() {
        super.interrupt();
        context.unregisterReceiver(wfdBroadcastReciever);
    }
}
