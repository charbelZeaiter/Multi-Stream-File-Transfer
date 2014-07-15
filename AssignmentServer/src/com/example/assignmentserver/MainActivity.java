package com.example.assignmentserver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * This is a server side application which utilizes both WiFi-Direct & Bluetooth wireless interfaces to simultaneously receive some data
 * across from another phone. This file is the main activity which starts everything and registers the WiFi-Direct broadcast receiver.
 * 
 * NOTE 1:		Setup of wireless interfaces is sequential. This application sets up WiFi-Direct first then Bluetooth. 
 * 				Transmission of data wireless-ly on both interfaces is then concurrent.
 * 
 * NOTE 2: 		Bluetooth seems to corrupt the data that is sent over large transmissions. This is an issue that I
 * 		 		have tried many a time to fix. But so far I cannot find a solution online. As a result, data that is
 * 				transfered via bluetooth slightly corrupts the image. To minimize the corruption I am transmitting 1 
 * 				byte at a time which makes Bluetooth considerable slow. The image however still remains display-able,
 *      		depicting these effects. One solution I thought of was to implement my on reliable transport protocol
 *       		on top of the bluetooth socket. But I obviously don't have the time to do this.
 *       
 *       		This application can transmit video, however it will not play if a single byte is corrupted. An Image 
 *       		can still be displayed even with corrupt bytes in the center. Hence why this application transmits an 
 *       		image by default. Transmitting an image will also allow for faster/non tedious demo-ing. A video file
 *       		will take some time with no display-able end result.
 *  
 * @author Charbel Zeaiter
 * 
 * Id:						z3419481
 * Subject:					COMP4336
 * Last-Modified: 			20/10/13
 *
 *
 */

public class MainActivity extends Activity {
	
	private WifiP2pManager myWifiDirectManager;
    private Channel myWifiDirectChannel;
    private BroadcastReceiver myWifiDirectReceiver;
    
    private TextView textView1;
 	private TextView textView2;
 	
    private IntentFilter mIntentFilter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Portrait only.
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		// Get a handle on views.
		this.textView1 = (TextView) findViewById(R.id.textView1);
		this.textView2 = (TextView) findViewById(R.id.textView2);
		
		// Register application with WiFi-Direct P2p framework.
		this.myWifiDirectManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
	    this.myWifiDirectChannel = this.myWifiDirectManager.initialize(this, getMainLooper(), null);
	    this.myWifiDirectReceiver = new WifiDirectBroadcastReceiver(myWifiDirectManager, myWifiDirectChannel, this);
	    
	    // Create a list of Intents (Filtered Intents) that the Broadcast Receiver pays attention to.
	    this.mIntentFilter = new IntentFilter();
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		// WiFi-Direct Discovering peers.
	    myWifiDirectManager.discoverPeers(this.myWifiDirectChannel, new WifiP2pManager.ActionListener() {
	        @Override
	        public void onSuccess() {
	            Log.i("Charbel", "Discover peers function > Success!");
	            textView1.setText("Discover peers function > Success!");
	        }

	        @Override
	        public void onFailure(int reasonCode) {
	        	Log.i("Charbel", "Discover peers function > Failure! > "+reasonCode);
	        	textView1.setText("Discover peers function > Failure!");
	        }
	    });

	}
	
	// Register the broadcast receiver & intent values to be matched on activity start up.
	@Override
	protected void onResume() {
	    super.onResume();
	    registerReceiver(myWifiDirectReceiver, mIntentFilter);
	}
	
}


	