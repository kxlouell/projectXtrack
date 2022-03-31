package com.pd.projectxtrack.BroadcastRecievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.LongDef;

import com.pd.projectxtrack.MainActivity;
import com.pd.projectxtrack.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class WiFiBroadcastReciever extends BroadcastReceiver {

    public static final String TAG = "WifiBroadcastReciever";
    private WifiManager wifiManager;
    private StringBuilder sb;
    private MainActivity mainActivity;

    public WiFiBroadcastReciever(MainActivity mainActivity, WifiManager wifiManager) {
        this.wifiManager = wifiManager;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Called!");
        String action = intent.getAction();
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            Log.d(TAG, "onReceive: Scan Result Available");
            sb = new StringBuilder();
            List<ScanResult> wifiList = wifiManager.getScanResults();
            System.out.println(TAG +"onReceive: "+wifiList);
            ArrayList<String> deviceList = new ArrayList<>();
            for (ScanResult scanResult : wifiList) {
                sb.append("\n").append(scanResult.SSID).append(" - ").append(scanResult.capabilities);
                deviceList.add(scanResult.SSID + " - " + scanResult.capabilities);
            }
            Toast.makeText(mainActivity, sb, Toast.LENGTH_SHORT).show();
            ArrayAdapter arrayAdapter = new ArrayAdapter(context, R.layout.wifi_peer_devices, R.id.wifi_peer_devicename,deviceList.toArray());
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.wfListView.setAdapter(arrayAdapter);
                }
            });
        }
    }
}
