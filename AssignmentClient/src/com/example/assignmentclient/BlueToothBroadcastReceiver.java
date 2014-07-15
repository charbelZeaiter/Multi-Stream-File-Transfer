package com.example.assignmentclient;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;
import android.widget.TextView;

/**
 * This Class is used to listen for Bluetooth broadcasts, search and ultimately perform Bluetooth connection.
 */
public class BlueToothBroadcastReceiver extends BroadcastReceiver{
	
	private WifiP2pManager myWifiDirectManager;
    private Channel myWifiDirectChannel;
    private Activity myActivity;
    private BluetoothAdapter myBluetoothAdapter;
    private String wifiDirectGroupOwnerIPAddress;
	
	private TextView textView1;
	private TextView textView2;
	
	public BlueToothBroadcastReceiver(Activity aActivity, WifiP2pManager aManager, Channel aChannel, BluetoothAdapter aBluetoothAdapter, String aWifiDirectGroupOwnerIPAddress)
	{
		this.myWifiDirectManager = aManager;
	    this.myWifiDirectChannel = aChannel;
	    this.myActivity = aActivity;
	    this.myBluetoothAdapter = aBluetoothAdapter;
	    this.wifiDirectGroupOwnerIPAddress = aWifiDirectGroupOwnerIPAddress;
		
		this.textView1 = (TextView) this.myActivity.findViewById(R.id.textView1);
		this.textView2 = (TextView) this.myActivity.findViewById(R.id.textView2);
		
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// Getting action string.
		String action = intent.getAction();
		
		// Checking for action types.
		if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            
			// Action: Bluetooth device found (After Bluetooth device discovery).
			
			this.textView1.setText("Bluetooth device found!");
			
			// Get the BluetoothDevice object from the Intent
            BluetoothDevice targetBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            
            // Check that Bluetooth device matches the one we are looking for.
            if(targetBluetoothDevice != null && targetBluetoothDevice.getUuids() != null && (targetBluetoothDevice.getName().matches(".*I9300T.*") || targetBluetoothDevice.getName().equals("HUAWEI U8950N-51")))
            {
            	this.textView1.setText("Found the required Bluetooth device!");
	            Log.i("CHARBEL", "Found the required Bluetooth device!: "+targetBluetoothDevice.getName());
	            Log.i("CHARBEL", "discovered-address: "+targetBluetoothDevice.getAddress());
	            
	            // Make sure to turn off discovery now that we have found the target device.
            	this.myBluetoothAdapter.cancelDiscovery();
            	
            	Log.i("CHARBEL", "After 'cancel discovery!': ");
            	
            	// Unregister Bluetooth broadcast receiver.
            	this.myActivity.unregisterReceiver(this);
            	
            	// Execute Client background Multi-Threaded transfer. 
	            ClientAsyncTask newClientAsyncTask = new ClientAsyncTask(myActivity, myWifiDirectManager, myWifiDirectChannel, myBluetoothAdapter, this.wifiDirectGroupOwnerIPAddress, targetBluetoothDevice);
	            newClientAsyncTask.execute();
            }
            
        }
		else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
			
			// Action: Bluetooth adapter state changed.
			
			// Get state from intent.
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -5);
			
			this.textView2.setText("State not on yet !");
			Log.i("CHARBEL", "State: "+state);
			
			// Check that state is 'on'.
			if(state == BluetoothAdapter.STATE_ON)
			{	
				Log.i("CHARBEL", "State: "+state);
				Log.i("CHARBEL", "Just about to start Bluetooth discovery !");
				this.textView2.setText("Just about to start Bluetooth discovery !");
				
				// Start discovery
				this.myBluetoothAdapter.startDiscovery();
			}
			
		}
		
		
	}

		
}
