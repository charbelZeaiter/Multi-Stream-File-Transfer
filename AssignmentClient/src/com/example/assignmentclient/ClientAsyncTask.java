package com.example.assignmentclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.AssetManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

/**
 * This Class is used to Create and run a background thread for which two worker threads are created to perform
 * network transmission simultaneously.
 */

public class ClientAsyncTask extends AsyncTask<Void, String, String > {

 	private WifiP2pManager myWifiDirectManager;
    private Channel myWifiDirectChannel;
    private Activity myActivity;
    private BluetoothAdapter myBluetoothAdapter;
    private String wifiDirectGroupOwnerIPAddress;
    private BluetoothDevice myBluetoothDevice; 
    
    private static String HOST_IP;
	private static int PORT_NUMBER;
	
    private byte[] wifiDirectBuffer;
    private byte[] bluetoothBuffer;
    
    private byte[] wifiDirectBytes;
    private byte[] bluetoothBytes;
    
    private ByteArrayInputStream wifiDirectBAIS;
    private ByteArrayInputStream bluetoothBAIS;
    
    public ClientAsyncTask(Activity aActivity, WifiP2pManager aManager, Channel aChannel, BluetoothAdapter aBluetoothAdapter, String aWifiDirectGroupOwnerIPAddress, BluetoothDevice aBluetoothDevice) {
        
    	// Setting buffer sizes. (Note: Bluetooth buffer size must be 1 to minimize data corruption along the transfer. Resulting in slowness.)
    	this.wifiDirectBuffer = new byte[1024];
        this.bluetoothBuffer = new byte[1];
        
        this.myWifiDirectManager = aManager;
	    this.myWifiDirectChannel = aChannel;
	    this.myActivity = aActivity;
	    this.myBluetoothAdapter = aBluetoothAdapter;
	    this.wifiDirectGroupOwnerIPAddress = aWifiDirectGroupOwnerIPAddress;
	    this.myBluetoothDevice = aBluetoothDevice;
	    
	    HOST_IP = this.wifiDirectGroupOwnerIPAddress;
	    PORT_NUMBER = 60001;
	    
    }

    
    @Override
    protected String doInBackground(Void... params) {
        
    	// Split total bytes to send into two isolated byte stream for each wireless interface.
    	this.prepareStreams();
    	
    	// Create WiFi-Direct thread.
    	Thread wifiDirectThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
		    	try 
		    	{
		    		// Create socket.
				    Socket myWifiDirectSocket = new Socket();
					
				    // No binding yet.
					myWifiDirectSocket.bind(null);
					
					// Connect to Host address and Port number with a 10 second timeout.
				    myWifiDirectSocket.connect((new InetSocketAddress(HOST_IP, PORT_NUMBER)), 10000);
				    
				    publishProgress("1--About to start sending Wifi Direct bytes"); 
				    Log.i("Charbel", "1--About to start sending Wifi Direct bytes");
				    
				    // Get the WiFi-Direct output stream to write data to the socket.
				    OutputStream wifiDirectOutputStream = myWifiDirectSocket.getOutputStream();
				    
				    // Create thread lock for thread-safe sending.
				    Lock myLock = new ReentrantLock();
				    
				    // Read from byte input stream into buffer.
				    int byteNum = wifiDirectBAIS.read(wifiDirectBuffer);
				    
				    // Keep sending while there a bytes to send.
				    while (byteNum != -1) {
				    	
				    	try
				    	{
				    		// Lock thread.
				    		myLock.lock();
				    		
				    		// Write bytes to output stream (Send data out via WiFi-Direct).
				    		wifiDirectOutputStream.write(wifiDirectBuffer);
				    		
				    		// Read from byte input stream into buffer.
				    		byteNum = wifiDirectBAIS.read(wifiDirectBuffer);
				    	}
				    	finally
				    	{
				    		// Finally unlock thread.
				    		myLock.unlock();
				    	}
				    	
				    	// Sleep thread to give the Bluetooth thread a chance to send its bytes.
				    	try {
							Thread.sleep(3);
						} catch (InterruptedException e) {
							Log.e("CHARBEL", "1--InterruptException: "+e.getMessage());
							e.printStackTrace();
						}
				    }
				    
				    // Display some progress.
				    publishProgress("1--About to close WifiDirect Output stream"); 
				    Log.i("Charbel", "1--About to close WifiDirect Output stream");
				    
				    // Closing output stream and input stream.
				    wifiDirectOutputStream.close();
				    wifiDirectBAIS.close();
				    
				    // Update text view with notification message.
				    publishProgress("1--Wifi direct transfer done!!!!!");    
			        Log.e("CHARBEL", "1--Wifi direct transfer done!!!!");
			        
				    // Close socket.
				    myWifiDirectSocket.close();
			        
				} catch (IOException e) {
					Log.e("Charbel", "1--!!!!!I/O Exception!!!!!!!!!  "+e.getMessage());
					publishProgress("1--!!!!!I/O Exception!!!!!!!!!  "+e.getMessage());
				}
			}
		    
		});
    	
    	// Create Bluetooth thread.
    	Thread bluetoothTread = new Thread(new Runnable() {
			
			@Override
			public void run() {

		    	try 
		    	{
				    
				    // Use RFCOMM UUID.
		            UUID mUUId = UUID.fromString("00000003-0000-1000-8000-00805F9B34FB");
		            
		            publishProgress("2--About to create a bluetooth socket!");    
			        Log.e("CHARBEL", "2--About to create a bluetooth socket!");
		            
			        // Get Bluetooth socket.
		            BluetoothSocket myBluetoothSocket = myBluetoothDevice.createRfcommSocketToServiceRecord(mUUId);
					
		            Log.i("CHARBEL", "2--Client is About to connect ");
		            publishProgress("2--Client is About to connect ");
					
		            // Connect to server service socket.
		            myBluetoothSocket.connect();
					
		            // Get output stream.
		            OutputStream bluetoothOutputStream = myBluetoothSocket.getOutputStream();
		            
		            // Create thread lock.
		            Lock myLock = new ReentrantLock();
		            
		            // Read byte from input stream into buffer.
					int byteNum = bluetoothBAIS.read(bluetoothBuffer);
							
		            while(byteNum != -1)
		            {
		            	try
		            	{
		            		// Lock thread.
		            		myLock.lock();
		            		
		            		// Write/Send byte out.
		            		bluetoothOutputStream.write(bluetoothBuffer);
			            	
		            		// Read byte from input stream.
			            	byteNum = bluetoothBAIS.read(bluetoothBuffer);
		            	}
		            	finally
		            	{
		            		// Finally unlock thread.
		            		myLock.unlock();
		            	}
		            	
		            }
		            
					//bluetoothOutputStream.write(bluetoothBytes);
					
		            // Close Input stream, output stream, and socket.
					myBluetoothSocket.close();
					bluetoothOutputStream.close();
					bluetoothBAIS.close();
				
				    Log.i("CHARBEL", "2--After connection !!!");
					publishProgress("2--After connection !!!");
				    
				} catch (IOException e) {
					Log.e("Charbel", "2--!!!!!I/O Exception!!!!!!!!!  "+e.getMessage());
					publishProgress("2--!!!!!I/O Exception!!!!!!!!!  "+e.getMessage());
				}
				
			}
		});
		
    	// Start worker threads 
    	bluetoothTread.start();
    	wifiDirectThread.start();
    	
    	// Try and pause the background thread to wait for these 
    	// worker threads to finish.
    	try {
    		
    		Log.i("Charbel", "Bloacking a sync thread to wait for worker threads!");
			
    		wifiDirectThread.join();
    		bluetoothTread.join();
    		
		} catch (InterruptedException e) {
			Log.e("Charbel", "2--!!!!!I/O Exception!!!!!!!!!  "+e.getMessage());
			e.printStackTrace();
		}
    	
    	// Output progress.
		Log.i("Charbel", "Everything was SENT!");
    	publishProgress("1--WD Sent!!!");
    	publishProgress("2--BT Sent!!!");
		
		return null;
    }
    
    
    
    @Override
    protected void onPostExecute(String result) {
        	
    	// Get a handle on views.
        final TextView textView1 = (TextView) this.myActivity.findViewById(R.id.textView1);
        final TextView textView2 = (TextView) this.myActivity.findViewById(R.id.textView2);
    		
        textView1.setText("File Successfully transferred!");
        textView2.setText("File Successfully transferred!");
        Log.e("CHARBEL", "File Successfully transferred!");
        	
        // Turn off Bluetooth.
        this.myBluetoothAdapter.disable();
        
        // Remove Clients association with WiFi-Direct group.
        this.myWifiDirectManager.removeGroup(myWifiDirectChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// Do nothing.
			}
			
			@Override
			public void onFailure(int reason) {
				// Do nothing.
			}
		});
        
    }

    
    @Override
    protected void onProgressUpdate(String... values) {
 
    	super.onProgressUpdate(values);
    	
    	// Get a handle on views.
	    TextView tv1 = (TextView) this.myActivity.findViewById(R.id.textView1);
	    TextView tv2 = (TextView) this.myActivity.findViewById(R.id.textView2);
	    
	    // Determine which view is used.
	    if(values[0].matches("^1--.*"))
	    {
	    	tv1.setText(values[0]);
	    }
	    else
	    {
	    	tv2.setText(values[0]);
	    }
	    
    }
    
    /**
     * prepareStreams()
     * This function is used to split the original file byte stream into two streams. One for WiFi-direct
     * the other for Bluetooth. Each stream will be sent over its corresponding wireless interface. WiFi-Direct
     * stream contains the majority of the bytes.
     */
    private void prepareStreams()
    {
    	try
    	{
    		// Get a handle on the asset manager.
	    	AssetManager myAssets = this.myActivity.getAssets();
		    
	    	byte[] localBuffer = new byte[1024];
	    	
	    	// Create main file stream to later be split into two streams
	    	ByteArrayOutputStream overallBAIS = new ByteArrayOutputStream();
	    	
	    	// Get bytes of file from asset manager.
	    	InputStream assetIS = myAssets.open("perl_to_transfer.jpg.jpg");
	    	
		    Log.i("Charbel", "Opening Asset.");
			publishProgress("Opening Asset.");
		    
			// Read bytes into main output stream.
	    	while(assetIS.read(localBuffer) != -1)
	    	{
	    		overallBAIS.write(localBuffer);
	    	}
	    	
	    	// Get total data byte array.
	    	byte[] totalByteData = overallBAIS.toByteArray();
	    	
	    	Log.i("Charbel", "Saved total byte stream to byte array!");
			publishProgress("Saved total byte stream to byte array!");
			
	    	// Get the closest index to % of the data used for splitting the streams.
	    	int endIndex = (int) Math.floor(totalByteData.length*0.96);
	    	
	    	// Create two data byte arrays, one for each stream.
	    	this.wifiDirectBytes = Arrays.copyOfRange(totalByteData, 0, endIndex);
	    	this.bluetoothBytes = Arrays.copyOfRange(totalByteData, endIndex, totalByteData.length);
	    	
	    	Log.i("Charbel", "Extracted part arrays from full bytes array!!");
			publishProgress("Extracted part arrays from full bytes array!!");
			
			// Create two streams, one for each interface.
	    	this.wifiDirectBAIS = new ByteArrayInputStream(wifiDirectBytes);
	    	this.bluetoothBAIS = new ByteArrayInputStream(bluetoothBytes);
	    	
	    	Log.i("Charbel", "Input streams ready!!!!!!!!");
			publishProgress("Input streams ready!!!!!!!!");
    	}
    	catch(FileNotFoundException e){
    		Log.e("Charbel", "!!!!!FILE NOT FOUND Exception!!!!!!!!!  "+e.getMessage());
    		e.printStackTrace();
    	} catch (IOException e) {
    		Log.e("Charbel", "!!!!!I/O Exception!!!!!!!!!  "+e.getMessage());
			e.printStackTrace();
		}
    	
    }


    
    
    
}
