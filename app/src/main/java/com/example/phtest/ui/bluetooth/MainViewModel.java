package com.example.phtest.ui.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainViewModel extends ViewModel {

    private Set<BluetoothDevice> paired;
    private Set<BluetoothDevice> unPaired;
    private BluetoothDevice connectedDevice;
    private BluetoothAdapter bluetoothAdapter;
    public ConnectedThread connectedThread;
    private BluetoothSocket bluetoothSocket = null; // bi-directional client-to-client data path
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    public final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            unPaired = new HashSet<>();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                unPaired.add(device);

            }

        }

    };





    public MainViewModel() {
    }

    public Set<BluetoothDevice> getPaired() {
        return paired;
    }

    public void setPaired(Set<BluetoothDevice> paired) {
        this.paired = paired;
    }

    public Set<BluetoothDevice> getUnPaired() {
        return unPaired;
    }

    public void setUnPaired(Set<BluetoothDevice> unPaired) {
        this.unPaired = unPaired;
    }

    public BluetoothDevice getConnectedDevice() {
        return connectedDevice;
    }

    public void setConnectedDevice(BluetoothDevice connectedDevice) {
        this.connectedDevice = connectedDevice;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public ConnectedThread getConnectedThread() {
        return connectedThread;
    }

    public void setConnectedThread() {
        this.connectedThread =  new ConnectedThread(getBluetoothSocket());
        connectedThread.start();
    }

    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    public void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
    }


    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket ;
        private final InputStream mmInStream ;
        private final OutputStream mmOutStream ;
        public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
        private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

        // The Handler that gets information back from the BluetoothChatService



        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

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
                    if (bytes != 0) {
                        buffer = new byte[1024];
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
//                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                                .sendToTarget(); // Send the obtained bytes to the UI activity
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
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}