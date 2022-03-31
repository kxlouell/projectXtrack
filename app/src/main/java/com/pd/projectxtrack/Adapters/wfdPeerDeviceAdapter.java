package com.pd.projectxtrack.Adapters;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import com.pd.projectxtrack.R;

public class wfdPeerDeviceAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<WifiP2pDevice> object;

    public wfdPeerDeviceAdapter(@NonNull Context context, @NonNull List<WifiP2pDevice> object) {
        super(context, R.layout.wfd_peer_devices);
        this.context = context;
        this.object = object;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater)context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.wfd_peer_devices, parent, false);
        TextView peername_textView = (TextView) row.findViewById(R.id.peer_devicename);
        peername_textView.setText(object.get(position).deviceName);
        return row;
    };
}
