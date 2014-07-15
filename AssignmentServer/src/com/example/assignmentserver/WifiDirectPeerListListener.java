package com.example.assignmentserver;

import java.util.Collection;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import android.widget.TextView;

/**
 * This class listens for other WiFi-Direct peers.
 */
public class WifiDirectPeerListListener implements PeerListListener {
	
    private WifiP2pManager myWifiDirectManager;
    private Channel myWifiDirectChannel;
    private Activity myWifiDirectActivity;
    private WifiDirectBroadcastReceiver myWifiDirectBroadcastReceiver;
	
 	private final TextView tv1;
 	private final TextView tv2;
 	
 	
	public WifiDirectPeerListListener(WifiP2pManager aManager, Channel aChannel, Activity aActivity, WifiDirectBroadcastReceiver aWifiBroadcastReceiver){
		super();
	    this.myWifiDirectManager = aManager;
	    this.myWifiDirectChannel = aChannel;
	    this.myWifiDirectActivity = aActivity;
	    this.myWifiDirectBroadcastReceiver = aWifiBroadcastReceiver;
	        
		// Handle on views.
	    this.tv1 = (TextView) this.myWifiDirectActivity.findViewById(R.id.textView1);
	    this.tv2 = (TextView) this.myWifiDirectActivity.findViewById(R.id.textView2);
	}
	
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {

		// Store the new list of peers.
		Collection<WifiP2pDevice> myList = peers.getDeviceList();
		
		// Check if list is empty.
		if(myList.isEmpty())
		{
			Log.i("Charbel", "Wifi-Direct List Empty");
			tv2.setText("Wifi-Direct List Empty");
		}
		else
		{
			this.tv1.setText("Found Wifi-Direct peers!");
			Log.i("Charbel", "Found Wifi-Direct peers!");
		}
		
	}

	
	
	
	
}
