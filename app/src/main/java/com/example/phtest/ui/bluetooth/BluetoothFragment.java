package com.example.phtest.ui.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.phtest.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class BluetoothFragment extends Fragment {
    private static final UUID BTMODULEUUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    private MainViewModel mainViewModel;
    CheckBox enable_bt;
    Button paired_btn, scan_btn;
    ListView listView;
//    LinearLayout listView;

    public final Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg){
            if(msg.what == MESSAGE_READ){
                String readMessage = null;
                try {
                    readMessage = new String((byte[]) msg.obj, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
//                mReadBuffer.setText(readMessage);
            }

            if(msg.what == CONNECTING_STATUS){
                if(msg.arg1 == 1)
                    Toast.makeText(getActivity(), "Connected to Device: " + mainViewModel.getConnectedDevice().getName(), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Connection Failed", Toast.LENGTH_SHORT).show();
            }
        }
    };



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mainViewModel =
                ViewModelProviders.of((FragmentActivity) getActivity()).get(MainViewModel.class);
        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        enable_bt = root.findViewById(R.id.enable_bt);
        paired_btn = root.findViewById(R.id.paired_btn);
        scan_btn = root.findViewById(R.id.scan_btn);
        listView = root.findViewById(R.id.list_view);

        mainViewModel.setBluetoothAdapter(BluetoothAdapter.getDefaultAdapter());
        if (mainViewModel.getBluetoothAdapter() == null) {
            Toast.makeText(getActivity(), "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        if (mainViewModel.getBluetoothAdapter().isEnabled()) {
            enable_bt.setChecked(true);
        }
        enable_bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    mainViewModel.getBluetoothAdapter().disable();
                    Toast.makeText(getActivity(), "Turned off", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intentOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intentOn, 0);
                    Toast.makeText(getActivity(), "Turned on", Toast.LENGTH_SHORT).show();
                }
            }
        });

        paired_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list();
            }
        });


        scan_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkBTPermissions();

                IntentFilter filter = new IntentFilter();

                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                Toast.makeText(getActivity(), "Showing devices", Toast.LENGTH_SHORT).show();
                ArrayList<String> list = new ArrayList<>();
                for (BluetoothDevice device: mainViewModel.getUnPaired()){
                    list.add((device.getName() != null) ? device.getName() : device.getAddress());
                }
                ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list);
                listView.setAdapter(adapter);
                getActivity().registerReceiver(mainViewModel.bReciever, filter);
                mainViewModel.getBluetoothAdapter().startDiscovery();


            }
        });



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = (String)adapterView.getItemAtPosition(i);
                Toast.makeText(getActivity(), name + " was selected", Toast.LENGTH_SHORT).show();
                BluetoothDevice device = (BluetoothDevice) mainViewModel.getPaired().toArray()[i];

                if(!mainViewModel.getBluetoothAdapter().isEnabled()) {
                    Toast.makeText(getContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getActivity(), "Connecting...", Toast.LENGTH_SHORT).show();
                // Get the device MAC address, which is the last 17 chars in the View
                final String address = device.getAddress();
                final String name2 = device.getName();

                // Spawn a new thread to avoid blocking the GUI one
                new Thread()
                {
                    public void run() {
                        boolean fail = false;

                        BluetoothDevice device = mainViewModel.getBluetoothAdapter().getRemoteDevice(address);

                        try {
                            mainViewModel.setBluetoothSocket(createBluetoothSocket(device));
                        } catch (IOException e) {
                            fail = true;
                            Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                        // Establish the Bluetooth socket connection.
                        try {
                            mainViewModel.getBluetoothSocket().connect();
                        } catch (IOException e) {
                            try {
                                fail = true;
                                mainViewModel.getBluetoothSocket().close();
                                mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                        .sendToTarget();
                            } catch (IOException e2) {
                                //insert code to deal with this
                                Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if(fail == false) {
                            mainViewModel.setConnectedThread();
                            mainViewModel.setConnectedDevice(device);
                            mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name2)
                                    .sendToTarget();
                        }
                    }
                }.start();
            }
        });




        return root;
    }

    @Override
    public void onDestroy() {
        try {
            getActivity().unregisterReceiver(mainViewModel.bReciever);
        } catch (IllegalArgumentException e){
            //todo add information
        }
        if (mainViewModel.getConnectedThread() != null) {
            mainViewModel.getConnectedThread().cancel();
        }
        super.onDestroy();
    }

    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = getActivity().checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += getActivity().checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    private void list() {
        Set<BluetoothDevice> onlyPairedDevice = mainViewModel.getBluetoothAdapter().getBondedDevices();
        ArrayList list = new ArrayList();
        mainViewModel.setPaired(onlyPairedDevice);

        if (mainViewModel.getPaired() != null) {
            for (BluetoothDevice bt : mainViewModel.getPaired()) {
                list.add((bt.getName() != null) ? bt.getName() : bt.getAddress());
            }
        }

        Toast.makeText(getActivity(), "Showing devices", Toast.LENGTH_SHORT).show();
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BTMODULEUUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }



}