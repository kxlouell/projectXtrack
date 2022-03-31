package com.pd.projectxtrack.WiFiTools;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.pd.projectxtrack.BroadcastRecievers.WiFiBroadcastReciever;
import com.pd.projectxtrack.MainActivity;
import com.pd.projectxtrack.R;

import java.util.ArrayList;
import java.util.List;

import static com.pd.projectxtrack.MainActivity.MY_PERMISSIONS_ACCESS_COARSE_LOCATION;

public class WifiDiscovery extends HandlerThread {

    public static final String TAG = "WifiDiscovery";
    public static final int INIT_WORK = 0;
    public static final int START_SCAN = 1;
    public static final int START_REGISTRATION = 2;

    private MainActivity mainActivity;
    private Context context;
    private WiFiBroadcastReciever wifiScanReceiver;
    private WifiManager wifiManager;
    public Handler handler;
    private StringBuilder sb;

    public WifiDiscovery(String name, int priority, MainActivity mainActivity) {
        super(name, priority);
        //mainActivityWeakReference = new WeakReference<MainActivity>(mainActivity);
        this.mainActivity = mainActivity;
        context = this.mainActivity.getApplicationContext();
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case INIT_WORK:
                        initWork();
                        break;
                    case START_SCAN:
                        startScan();
                        break;
                }

            }
        };

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.scanWf.setEnabled(true);
                mainActivity.scanWf.setText("Scan WiFi");
            }
        });

    }

    public void initWork(){
        Log.d(TAG, "initWork: Called!");
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiScanReceiver = new WiFiBroadcastReciever(mainActivity, wifiManager);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
        startScan();

    }

    private void startScan(){
        Log.d(TAG, "startScan: Called!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(context, "version> = marshmallow", Toast.LENGTH_SHORT).show();
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "location turned off", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
            } else {
                Toast.makeText(context, "location turned on", Toast.LENGTH_SHORT).show();
                Toast.makeText(context, "scanning", Toast.LENGTH_SHORT).show();
                boolean success = wifiManager.startScan();
                Log.d(TAG, "startScan: Scanning:"+success);
                if (!success) {
                    scanFailure();
                }else{
                    scanSuccess();
                }
            }
        } else {
            boolean success = wifiManager.startScan();
            Log.d(TAG, "startScan: Scanning:"+success);
            if (!success) {
                scanFailure();
            }else{
                scanSuccess();
            }
        }


    }

    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        Toast.makeText(context,"Scanning",Toast.LENGTH_SHORT).show();

    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        sb = new StringBuilder();
        List<ScanResult> wifiList = wifiManager.getScanResults();
        System.out.println(TAG+"OnScanFailed: "+wifiList);
        ArrayList<String> deviceList = new ArrayList<>();
        for (ScanResult scanResult : wifiList) {
            sb.append("\n").append(scanResult.SSID).append(" - ").append(scanResult.capabilities);
            deviceList.add(scanResult.SSID + " - " + scanResult.capabilities);
        }
        Toast.makeText(context, sb, Toast.LENGTH_SHORT).show();
        ArrayAdapter arrayAdapter = new ArrayAdapter(context, R.layout.wifi_peer_devices, deviceList.toArray());
        Toast.makeText(context,"Scanning Failed",Toast.LENGTH_SHORT).show();
    }


}
