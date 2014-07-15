package com.example.assignmentclient;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
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
    private Activity myWifiDirectActivity;
    
 	private final TextView textView1;
 	private final TextView textView2;
 	
 	
    public WifiDirectBroadcastReceiver(WifiP2pManager aWifiDirectManager, Channel aWifiDirectChannel, Activity aActivity) {
        
    	super();
        
    	this.myWifiDirectManager = aWifiDirectManager;
        this.myWifiDirectChannel = aWifiDirectChannel;
        this.myWifiDirectActivity = aActivity;
        
	    // Get a handle on views.
	    this.textView1 = (TextView) this.myWifiDirectActivity.findViewById(R.id.textView1);
	    this.textView2 = (TextView) this.myWifiDirectActivity.findViewById(R.id.textView2);
    }
    
    
    @Override
    public void onReceive(Context context, Intent intent) {
        
    	// Get action from intent.
    	String action = intent.getAction();
    	
    	// Checking which action and determining code to execute.
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
	    	PeerListListener myPeerListListener = new WifiDirectPeerListListener(this.myWifiDirectManager, this.myWifiDirectChannel, this.myWifiDirectActivity);
				
			// Register peer list listener with WiFi Manager.
	    	this.myWifiDirectManager.requestPeers(this.myWifiDirectChannel, myPeerListListener);
	    	
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            
        	// Check to see if WiFi-Direct connection state has changed.
        	
        	// Respond to new connection or disconnections
        	NetworkInfo newNetworkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        	
        	Log.i("CHARBEL", "Network State: "+newNetworkInfo.getState());
        	Log.i("CHARBEL", "Network Type: "+newNetworkInfo.getType());
        	Log.i("CHARBEL", "Network Type human friendly: "+newNetworkInfo.getType());
        	
        	// Check for connected connection.
        	if(newNetworkInfo.getState().equals(NetworkInfo.State.CONNECTED))
        	{
        		// Get IP address of Group Owner.
        		WifiP2pInfo newWifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
        		String wifiDirectGroupOwnerIPAddress = newWifiP2pInfo.groupOwnerAddress.getHostAddress();
        		
        		Log.i("CHARBEL", "Group owner ip: "+wifiDirectGroupOwnerIPAddress);
        		Log.i("CHARBEL", "###########################");
        		Log.i("CHARBEL", "CAN NOW CONNECT BLUETOOTH!!");
        		
        		// Unregister WiFi-Direct receiver now that it is connected (No other WiFi-Direct interruptions).
        		this.myWifiDirectActivity.unregisterReceiver(this);
        		
        		// Moving to set up Bluetooth now.
        		
        		// Getting a handle on Bluetooth adapter.
	        	BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	        		
	        	// Check that Bluetooth is off before we turn it on.
        		if( !myBluetoothAdapter.isEnabled() )
        		{        			
	        		
        			this.textView2.setText("About to set up bluetooth");
	        		Log.i("Charbel", "About to set up bluetooth");		
	        		
	        		// Going to automatically enable BlueTooth through discover-ability.
	        		Intent discoverableIntent = new	Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	        		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
	        		this.myWifiDirectActivity.startActivity(discoverableIntent);
	        		
	        		// Register the BroadcastReceiver
	        		IntentFilter myFilter = new IntentFilter();
	        		myFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
	        		myFilter.addAction(BluetoothDevice.ACTION_FOUND);
	        		
	        		// Creating BlueTooth broadcast receiver.
	        		BlueToothBroadcastReceiver myBlueToothBroadcastReceiver = new BlueToothBroadcastReceiver(this.myWifiDirectActivity, this.myWifiDirectManager, this.myWifiDirectChannel, myBluetoothAdapter, wifiDirectGroupOwnerIPAddress);
	        		
	        		// Register Bluetooth receiver.
	        		this.myWifiDirectActivity.registerReceiver(myBlueToothBroadcastReceiver, myFilter); 
	        		
	        		this.textView2.setText("registered bluetooth broadcast receiver!");
        		
        		}
        	}
        	
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }
    
    
}




