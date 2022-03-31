package com.pd.projectxtrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;

//WifiP2p
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pd.projectxtrack.WiFiTools.WifiDiscovery;
import com.pd.projectxtrack.WifiDirectTools.P2pDiscovery;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public static final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 110;
    public static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 111;

    public ListView wfdListView, wfListView;
    public TextView wfStatusTextView, wfdStatusTextView;
    public Button scanWf, scanWFD;

    //Classes
    private P2pDiscovery p2pDiscovery;
    private WifiDiscovery wifiDiscovery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
        exqListener();
        p2pDiscovery.start();
        wifiDiscovery.start();
    }

    public void initialWork() {
        p2pDiscovery = new P2pDiscovery("P2pDiscovery", Thread.NORM_PRIORITY, this);
        wifiDiscovery = new WifiDiscovery("WiFiDiscovery", Thread.NORM_PRIORITY, this);

        wfdListView = (ListView) findViewById(R.id.recyclerviewWFD);
        wfListView = (ListView) findViewById(R.id.recyclerviewWF);
        wfdStatusTextView = (TextView) findViewById(R.id.wfdStatusTextView);
        wfStatusTextView = (TextView) findViewById(R.id.wfStatusTextView);
        scanWf = (Button) findViewById(R.id.scanWF);
        scanWFD = (Button) findViewById(R.id.scanWFD);
    }

    public void exqListener() {
        scanWFD.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: "+s);
                if(s.toString().equals("Initializing..")) {
                    Log.d(TAG, "onTextChanged: Sending Init_work and Start_Reg");
                    p2pDiscovery.handler.obtainMessage(P2pDiscovery.INIT_WORK).sendToTarget();
                    p2pDiscovery.handler.obtainMessage(P2pDiscovery.START_REGISTRATION).sendToTarget();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        scanWFD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(scanWFD.getText().toString().equals("Retry")){
                    p2pDiscovery.handler.obtainMessage(P2pDiscovery.START_REGISTRATION).sendToTarget();
                }else if(scanWFD.getText().toString().equals("Scan WFD")){
                    p2pDiscovery.handler.obtainMessage(P2pDiscovery.DISCOVER_SERVICE).sendToTarget();
                }
            }
        });

        scanWf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiDiscovery.handler.obtainMessage(WifiDiscovery.START_SCAN).sendToTarget();
            }
        });

        scanWf.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                wifiDiscovery.handler.obtainMessage(WifiDiscovery.INIT_WORK).sendToTarget();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "permission Access Coarse Location granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "permission Access Coarse Location not granted", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "permission Access Fine Location granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "permission Access Fine Location not granted", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}