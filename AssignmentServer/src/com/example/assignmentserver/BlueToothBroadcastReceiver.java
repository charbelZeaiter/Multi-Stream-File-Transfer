package com.example.assignmentserver;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

/**
 * This Class is used to listen for Bluetooth broadcasts, search and ultimately perform Bluetooth connection.
 */

public class BlueToothBroadcastReceiver extends BroadcastReceiver{
	
	private Activity myActivity;
	private BluetoothAdapter mBluetoothAdapter;
	private FileServerAsyncTask myFileServerAsyncTask;
	
	TextView textView1;
	TextView textView2;
	
	public BlueToothBroadcastReceiver(Activity aActivity,  BluetoothAdapter aBluetoothAdapter, FileServerAsyncTask aFileServerAsyncTask)
	{
		this.myActivity = aActivity;
		this.mBluetoothAdapter = aBluetoothAdapter;
		this.myFileServerAsyncTask = aFileServerAsyncTask;
		
		this.textView1 = (TextView) myActivity.findViewById(R.id.textView1);
		this.textView2 = (TextView) myActivity.findViewById(R.id.textView2);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// Get intent action.
		String action = intent.getAction();
		
		// Check type of intent action.
		if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
			
			// Get state from intent.
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -5);
						
			this.textView2.setText("State not on yet !");
			Log.i("CHARBEL", "State: "+state);
			
			// Check if stat is 'on'.
			if(state == BluetoothAdapter.STATE_ON)
			{	
				this.textView2.setText("Bluetooth ON and ready to create Socket!");
				Log.i("CHARBEL", "State: "+state);
				Log.i("CHARBEL", "++++++++++++++++++++++++++++++++++++++");
				Log.i("CHARBEL", "+++++Bluetooth ready to create Socket!");
				
				// Unregister Bluetooth broadcast receiver.
				this.myActivity.unregisterReceiver(this);
				
				// Execute server background Multi-Threaded receive. 
				this.myFileServerAsyncTask.execute();
			}
			
			
		}

		
	}

		
}
