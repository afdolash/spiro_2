package com.pens.afdolash.spiro.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pens.afdolash.spiro.R;
import com.pens.afdolash.spiro.main.MainActivity;

import java.util.List;

import static com.pens.afdolash.spiro.main.MainActivity.EXTRA_BLUETOOTH;
import static com.pens.afdolash.spiro.main.MainActivity.USER_PREF;

/**
 * Created by afdol on 5/17/2018.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.MyViewHolder> {

    private Context context;
    private List<BluetoothDevice> devices;

    // Shared preference
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
        this.context = context;
        this.devices = devices;

        preferences = context.getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    @Override
    public DeviceAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DeviceAdapter.MyViewHolder holder, int position) {
        final BluetoothDevice device = devices.get(position);

        holder.tvName.setText(device.getName());
        holder.tvAddress.setText(device.getAddress());
        holder.cardDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    try {
                        editor.putString(EXTRA_BLUETOOTH, device.getAddress());
                        editor.apply();

                        Intent intent = new Intent(context, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(context, "Bluetooth connection failed!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    device.createBond();
                    Toast.makeText(context, "Create bond!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName, tvAddress;
        public CardView cardDevice;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvAddress = (TextView) itemView.findViewById(R.id.tv_address);
            cardDevice = (CardView) itemView.findViewById(R.id.card_device);
        }
    }
}
