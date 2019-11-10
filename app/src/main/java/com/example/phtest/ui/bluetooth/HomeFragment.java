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
import android.os.Message;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phtest.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class HomeFragment extends Fragment {


    private static final UUID BTMODULEUUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    public static ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    public BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    private HomeViewModel homeViewModel;
    CheckBox enable_bt;
    Button paired_btn, scan_btn, on_btn, off_btn;
    ListView listView;
//    LinearLayout listView;
    BluetoothSocket temp = null;
    static UUID UUID = java.util.UUID.randomUUID();

    private ArrayList<BluetoothDevice> pairedDevice = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                pairedDevice.add(device);

            }

        }

    };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler(){
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
                    Toast.makeText(getActivity(), "Connected to Device: ", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Connection Failed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        enable_bt = root.findViewById(R.id.enable_bt);
        paired_btn = root.findViewById(R.id.paired_btn);
        scan_btn = root.findViewById(R.id.scan_btn);
        on_btn = root.findViewById(R.id.On_btn);
        off_btn = root.findViewById(R.id.Off_btn);
        listView = root.findViewById(R.id.list_view);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        if (bluetoothAdapter.isEnabled()) {
            enable_bt.setChecked(true);
        }
        enable_bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    bluetoothAdapter.disable();
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

        on_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if((getmConnectedThread() != null)) //First check to make sure thread created
                    getmConnectedThread().write("1");
            }
        });


        off_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if((getmConnectedThread() != null)) //First check to make sure thread created
                    getmConnectedThread().write("0");
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
                for (BluetoothDevice device: pairedDevice){
                    list.add((device.getName() != null) ? device.getName() : device.getAddress());
                }
                ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list);
                listView.setAdapter(adapter);
                getActivity().registerReceiver(bReciever, filter);
                bluetoothAdapter.startDiscovery();

            }
        });



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = (String)adapterView.getItemAtPosition(i);
                Toast.makeText(getActivity(), name + " was selected", Toast.LENGTH_SHORT).show();
                BluetoothDevice device = (BluetoothDevice) pairedDevice.toArray()[i];

                if(!bluetoothAdapter.isEnabled()) {
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

                        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

                        try {
                            mBTSocket = createBluetoothSocket(device);
                        } catch (IOException e) {
                            fail = true;
                            Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                        // Establish the Bluetooth socket connection.
                        try {
                            mBTSocket.connect();
                        } catch (IOException e) {
                            try {
                                fail = true;
                                mBTSocket.close();
                                mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                        .sendToTarget();
                            } catch (IOException e2) {
                                //insert code to deal with this
                                Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if(fail == false) {
                            mConnectedThread = new ConnectedThread(mBTSocket);
                            mConnectedThread.start();

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
        getActivity().unregisterReceiver(bReciever);
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
        }
        try {
            if (temp != null) {
                temp.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
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


    public void connect(View v) {
        Intent serverIntent = new Intent();
        startActivityForResult(serverIntent, 1);
    }

    public void discoverable(View v) {
        ensureDiscoverable();
    }

    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void list() {
        Set<BluetoothDevice> onlyPairedDevice = bluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevice != null) {
            for (BluetoothDevice bt : pairedDevice) {
                list.add((bt.getName() != null) ? bt.getName() : bt.getAddress());
            }
            for (BluetoothDevice bt : onlyPairedDevice) {
                pairedDevice.add(bt);
                list.add((bt.getName() != null) ? bt.getName() : bt.getAddress());
            }
        }

        Toast.makeText(getActivity(), "Showing devices", Toast.LENGTH_SHORT).show();
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
    }

    public ConnectedThread getmConnectedThread(){
        return mConnectedThread;
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

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.available();
                if(bytes != 0) {
                    buffer = new byte[1024];
                    SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                    bytes = mmInStream.available(); // how many bytes are ready to be read?
                    bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget(); // Send the obtained bytes to the UI activity
                }
            } catch (IOException e) {
                e.printStackTrace();

                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(String input) {
        byte[] bytes = input.getBytes();           //converts entered String into bytes
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
}