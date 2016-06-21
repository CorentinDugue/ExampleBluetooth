package ahc.examplebluetooth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;

import bluetoothStuff.*;



public class MainActivity extends AppCompatActivity {

    public BluetoothDevice device;                          //device to be connected to
    public ConnectThread connectThread;                     //connection thread
    private ManageConnectedThread mmManagegedConnection;    //send & receive thread

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);              //set layout
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //always keep orientation in portrait
        initialiseBluetooth();                              //Bluetooth stuff
    }

    //Initialise bluetooth stuff
    private void initialiseBluetooth() {
        Intent intent = getIntent();
        String deviceAddress = intent.getStringExtra(BluetoothActivity.DEVICE_ADDRESS); //gets passed device MAC address
        device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
        Log.e("Device Address is", deviceAddress);
    }

    //Need to start connection thread again when awake
    @Override
    protected void onResume() {
        super.onResume();
        UUID deviceUUID = device.getUuids()[0].getUuid();
        Log.e("UUID", deviceUUID + "");
        connectThread = new ConnectThread(device, deviceUUID);
        connectThread.start();
    }

    //stop connection thread to save battery when application closed
    @Override
    protected void onStop() {
        super.onStop();
        if (mmManagegedConnection != null)
            mmManagegedConnection.cancel();
        if (connectThread.getSocket() != null)
            connectThread.cancel();
        try {
            connectThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //sends response through bluetooth, firstly gets a socket
    public void sendResponse(int sendWord) {
        if (connectThread.getSocket() != null && connectThread.getSocket().isConnected()) { //check if device is still connected
            if (mmManagegedConnection == null) {
                mmManagegedConnection = new ManageConnectedThread(connectThread.getSocket());   //get bluetooth socket
            }
                mmManagegedConnection.write(sendWord); //send the word
        } else {
            finish();
        }
    }

    /** Called when the user clicks the Send button */
public void sendMessage(View view) {
    // Do something in response to button
    EditText editText = (EditText) findViewById(R.id.edit_message);
    // String message = editText.getText().toString();
    int message = Integer.parseInt(editText.getText().toString());
    Log.e("Message entered", message + "");
    sendResponse(message);
}

}
