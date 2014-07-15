package com.example.assignmentserver;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import android.widget.TextView;

/**
 * A BroadcastReceiver that notifies of important WiFi p2p events.
 */

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
	
    private WifiP2pManager myWifiDirectManager;
    private Channel myWifiDirectChannel;
    private Activity myActivity;
    
 	private final TextView textView1;
 	private final TextView textView2;
    

    public WifiDirectBroadcastReceiver(WifiP2pManager aWifiDirectManager, Channel aWifiDirectChannel, Activity aActivity) {
        super();
        
        this.myWifiDirectManager = aWifiDirectManager;
        this.myWifiDirectChannel = aWifiDirectChannel;
        this.myActivity = aActivity;
        
	    // Get a handle on views.
	    this.textView1 = (TextView) this.myActivity.findViewById(R.id.textView1);
	    this.textView2 = (TextView) this.myActivity.findViewById(R.id.textView2);
    }

    
    @Override
    public void onReceive(Context context, Intent intent) {
        
    	// Get action string from intent.
    	String action = intent.getAction();
    	
    	// Check intent action.
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            
        	// Check to see if WiFi-Direct is enabled.
        	
        	// Get WiFi-Direct state.
        	int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
	        
	    	if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
	            
	    		// WiFi-Direct P2P is enabled
	    		textView1.setText("Wifi-Direct: Enabled!");
	        
	    	} else {
	            
	    		// WiFi-Direct P2P is not enabled
	        	textView1.setText("Wifi-Direct: Disabled");
	        
	    	}
	    	
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
        	
        	// Check to see if WiFi-Direct peers have changed.
        	
        	textView2.setText("Wifi-Direct Peers changed ");
        	
        	// Call WifiP2pManager.requestPeers() to get a list of current peers.
        	
	    	// First creating peerListListener.
	    	PeerListListener myPeerListListener = new WifiDirectPeerListListener(this.myWifiDirectManager, this.myWifiDirectChannel, this.myActivity, this);
				
			// Register peer list listener with Wi-Fi Manager.
	    	this.myWifiDirectManager.requestPeers(this.myWifiDirectChannel, myPeerListListener);
	    	
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            
        	// Check to see if WiFi-Direct connection state has changed.
        	
        	// Respond to new connection or disconnections
        	NetworkInfo newNetworkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        	
        	Log.i("CHARBEL", "Network State: "+newNetworkInfo.getState());
        	
        	// If connection is connected.
        	if(newNetworkInfo.getState().equals(NetworkInfo.State.CONNECTED))
        	{
        		// WiFi-Direct connected.
        		
        		// Now looking to turn on Bluetooth.
        		
        		Log.i("CHARBEL", "###########################");
        		Log.i("CHARBEL", "CAN NOW CONNECT BLUETOOTH!!");
        		
        		// Unregister WiFi-Direct receiver.
        		this.myActivity.unregisterReceiver(this);
        		
        		// Getting a handle on Bluetooth adapter.
	        	BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	        		
	        	// Check that Bluetooth is first off, to switch on.
        		if( !myBluetoothAdapter.isEnabled() ){
	        		
	        		this.textView2.setText("About to set up bluetooth");
	        		Log.i("Charbel", "About to set up bluetooth");
	        		
	        		// Create Server aSync task with all needed objects.
	        		FileServerAsyncTask myFileServerAsyncTask = new FileServerAsyncTask(this.myActivity, this.myWifiDirectManager, this.myWifiDirectChannel, this, myBluetoothAdapter);		
	        		
	        		// Going to automatically enable BlueTooth through discover-ability.	        		
	        		Intent discoverableIntent = new	Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	        		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
	        		this.myActivity.startActivity(discoverableIntent);
	        			
	        		// Register the BroadcastReceiver
	        		IntentFilter myFilter = new IntentFilter();
	        		myFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
	        						
	        		// Creating BlueTooth broadcast receiver.
	        		BlueToothBroadcastReceiver myBlueToothBroadcastReceiver = new BlueToothBroadcastReceiver(this.myActivity, myBluetoothAdapter, myFileServerAsyncTask);
	        					
	        		this.myActivity.registerReceiver(myBlueToothBroadcastReceiver, myFilter); 
	        		
	        		this.textView2.setText("registered Bluetooth broadcast receiver!");
        		}
        		
        	}
        	
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }
    
    
}




