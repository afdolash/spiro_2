package com.pens.afdolash.spiro.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pens.afdolash.spiro.R;

import java.util.ArrayList;
import java.util.List;

public class BluetoothActivity extends AppCompatActivity {
    public static final String BLUETOOTH_TAG = BluetoothActivity.class.getSimpleName();

    // View
    private TextView tvDiscover;
    private RecyclerView recyclerView;
    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private DeviceAdapter deviceAdapter;

    // State of Bluetooth
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(bluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(BLUETOOTH_TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(BLUETOOTH_TAG, "broadcastReceiver: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(BLUETOOTH_TAG, "broadcastReceiver: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(BLUETOOTH_TAG, "broadcastReceiver: STATE TURNING ON");
                        break;
                }
            }

            if (action.equals(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(bluetoothAdapter.EXTRA_SCAN_MODE, bluetoothAdapter.ERROR);

                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(BLUETOOTH_TAG, "broadcastReceiver: Discoverability Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(BLUETOOTH_TAG, "broadcastReceiver: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(BLUETOOTH_TAG, "broadcastReceiver: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(BLUETOOTH_TAG, "broadcastReceiver: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(BLUETOOTH_TAG, "broadcastReceiver: Connected.");
                        break;
                }
            }

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                bluetoothDevices.add(device);
                deviceAdapter.notifyDataSetChanged();
                Log.d(BLUETOOTH_TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
            }

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDED:
                        Log.d(BLUETOOTH_TAG, "broadcastReceiver: BOND_BONDED.");
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.d(BLUETOOTH_TAG, "broadcastReceiver: BOND_BONDING.");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.d(BLUETOOTH_TAG, "broadcastReceiver: BOND_NONE.");
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        // Initialization view
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        tvDiscover = (TextView) findViewById(R.id.tv_discover);

        // Initialization bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceAdapter = new DeviceAdapter(this, bluetoothDevices);

        // Show list of paired bluetooth
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(deviceAdapter);

        // When text discover clicked
        tvDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverBluetooth();
                Toast.makeText(BluetoothActivity.this, "Discovering...", Toast.LENGTH_SHORT).show();
            }
        });
        discoverBluetooth();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableBluetooth();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * Enabling bluetooth
     */
    private void enableBluetooth() {
        if (bluetoothAdapter == null) {
            Log.d(BLUETOOTH_TAG, "enableBluetooth: Does not have BT capabilities.");
        }

        if(!bluetoothAdapter.isEnabled()){
            Log.d(BLUETOOTH_TAG, "enableBluetooth: enabling BT.");

            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcastReceiver, BTIntent);
            return;
        }
    }

    /**
     * Discover bluetooth
     */
    private void discoverBluetooth() {
        Log.d(BLUETOOTH_TAG, "discoverBluetooth: Looking for unpaired devices.");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Log.d(BLUETOOTH_TAG, "discoverBluetooth: Canceling discovery.");

            checkBTPermission();
            bluetoothDevices.clear();
            bluetoothAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiver, discoverDevicesIntent);
        } else {
            checkBTPermission();
            bluetoothDevices.clear();
            bluetoothAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiver, discoverDevicesIntent);
        }
    }

    /**
     * Check permission
     */
    private void checkBTPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(BLUETOOTH_TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }
}
