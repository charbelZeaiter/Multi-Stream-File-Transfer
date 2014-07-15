package com.example.assignmentclient;

import java.util.Collection;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import android.widget.TextView;

/**
 * This class listens for other WiFi-Direct peers with certain MAC addresses and connects to them.
 */
public class WifiDirectPeerListListener implements PeerListListener {
	
    final private WifiP2pManager myWifiDirectManager;
    final private Channel myWifiDirectChannel;
    final private Activity myWifiDirectActivity;
	
 	private final TextView textView1;
 	private final TextView textView2;
 	
 	private String NICTA_DEVICE_MAC;
 	private String PERSONAL_DEVICE_MAC;
 	
 	
	public WifiDirectPeerListListener(WifiP2pManager aManager, Channel aChannel, Activity aActivity){
		super();
	    this.myWifiDirectManager = aManager;
	    this.myWifiDirectChannel = aChannel;
	    this.myWifiDirectActivity = aActivity;
	        
		// Handle on views.
	    this.textView1 = (TextView) this.myWifiDirectActivity.findViewById(R.id.textView1);
	    this.textView2 = (TextView) this.myWifiDirectActivity.findViewById(R.id.textView2);
	    
	    // Hard-coding devices to search for.
	    this.NICTA_DEVICE_MAC = "0a:7a:4c:ad:e7:21";
	    this.PERSONAL_DEVICE_MAC ="5e:0a:5b:c0:25:73";
	}
	
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		
		// Store a new list of peers.
		Collection<WifiP2pDevice> myList = peers.getDeviceList();
		
		// Check if list is empty.
		if(myList.isEmpty())
		{
			Log.i("Charbel", "Peer List is Empty");
			textView2.setText("Peer List is Empty");
		}
		
		// Loop through device list to find required device to connect to.
		for(WifiP2pDevice listEntry : myList ){   
			
			// Log info data.
			Log.i("Charbel", "Name: "+listEntry.deviceName+", Device Address: "+listEntry.deviceAddress);
			textView1.setText("Name: "+listEntry.deviceName+", Device Address: "+listEntry.deviceAddress);
			
			// Check and only to connect to specified MAC addresses.
			if(listEntry.deviceAddress.equals(NICTA_DEVICE_MAC) || listEntry.deviceAddress.equals(PERSONAL_DEVICE_MAC)){
				
				// Get a handle on the device.
				final WifiP2pDevice device = listEntry;
				
				// Configure connection settings to the device.
				WifiP2pConfig config = new WifiP2pConfig();
				config.deviceAddress = device.deviceAddress;
				config.groupOwnerIntent = 0;
				
				// Connect to device and create Connection listener.
				myWifiDirectManager.connect(myWifiDirectChannel, config, new ActionListener() {
				
				    @Override
				    public void onSuccess() {
				        
				    	// Success logic
				    	
				    	textView1.setText("Connected to > Name: "+device.deviceName+", Device Address: "+device.deviceAddress);   
						Log.i("Charbel", "Connected to > Name: "+device.deviceName+", Device Address: "+device.deviceAddress);			
						
				    }

				    @Override
				    public void onFailure(int reason) {
				        
				    	// Failure logic
				    	
				    	textView1.setText("Failed to Connect to > Name: "+device.deviceName+", Device Address: "+device.deviceAddress);   
						Log.i("Charbel", "Failed to Connect to > Name: "+device.deviceName+", Device Address: "+device.deviceAddress);
				    }
				    
				});
				
			}
			
		}
		
		
	}

	
	
	
	
}
