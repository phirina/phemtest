package com.example.phtest.ui.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.phtest.R;

import java.util.ArrayList;
import java.util.Set;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    CheckBox enable_bt, visible_bt;
    ImageView search_bt;
    ListView listView;
//    LinearLayout listView;

    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevice;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        enable_bt = root.findViewById(R.id.enable_bt);
        visible_bt = root.findViewById(R.id.visible_bt);
        search_bt = root.findViewById(R.id.search_bt);
        listView = root.findViewById(R.id.list_view);

        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA == null) {
            Toast.makeText(getActivity(), "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        if (BA.isEnabled()) {
            enable_bt.setChecked(true);
        }
        enable_bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    BA.disable();
                    Toast.makeText(getActivity(), "Turned off", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intentOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intentOn, 0);
                    Toast.makeText(getActivity(), "Turned on", Toast.LENGTH_SHORT).show();
                }
            }
        });

        visible_bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(getVisible, 0);
                    Toast.makeText(getActivity(), "Visible for 2 min", Toast.LENGTH_SHORT).show();
                }
            }
        });

        search_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                list();
            }
        });


        return root;
    }

    private void list() {
//        Intent intent = new Intent(BluetoothDevice.ACTION_FOUND);
//        final ArrayList list = new ArrayList();
//        String action = intent.getAction();
//        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//            BluetoothDevice curDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            list.add(curDevice.getName());
//        }
        pairedDevice = BA.getBondedDevices();
//        Intent intent = new Intent();
//        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//
        ArrayList list = new ArrayList();

        for (BluetoothDevice bt : pairedDevice) {
            list.add(bt.getName());
        }
        Toast.makeText(getActivity(), "Showing devices", Toast.LENGTH_SHORT).show();
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
//        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list);
//        listView.setAdapter(adapter);
    }

    public String getLocalBluetoothName() {
        if (BA == null) {
            BA = BluetoothAdapter.getDefaultAdapter();
        }
        String name = BA.getName();
        if (name == null) {
            name = BA.getAddress();
        }

        return name;
    }
}