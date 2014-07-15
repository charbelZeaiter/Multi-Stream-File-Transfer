package com.example.assignmentserver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

/**
 * This Class is used to Create and run a background thread for which two worker threads are created to perform
 * network transmission simultaneously.
 */

public class FileServerAsyncTask extends AsyncTask<Void, String, String > {

 	private WifiP2pManager myWifiDirectManager;
    private Channel myWifiDirectChannel;
    private Activity myActivity;
    private WifiDirectBroadcastReceiver myWifiDirectBroadcastReceiver;
    private BluetoothAdapter mBluetoothAdapter;
    
    private byte[] wifiDirectBuffer;
    private byte[] bluetoothBuffer;
    
    private ByteArrayOutputStream wifiDirectBAOS;
    private ByteArrayOutputStream bluetoothBAOS;
   
    
    public FileServerAsyncTask(Activity aActivity, WifiP2pManager aWifiDirectManager, Channel aWifiDirectChannel, WifiDirectBroadcastReceiver aWifiBroadcastReceiver, BluetoothAdapter aBluetoothAdapter) {
        
    	this.wifiDirectBuffer = new byte[1024];
        this.bluetoothBuffer = new byte[1];
    
        this.myWifiDirectManager = aWifiDirectManager;
	    this.myWifiDirectChannel = aWifiDirectChannel;
	    this.myActivity = aActivity;
	    this.myWifiDirectBroadcastReceiver = aWifiBroadcastReceiver;
	    this.mBluetoothAdapter = aBluetoothAdapter;
	    
	    this.wifiDirectBAOS = new ByteArrayOutputStream();
	    this.bluetoothBAOS = new ByteArrayOutputStream();
	    
    }

    @Override
    protected String doInBackground(Void... params) {
        
    	// Create WiFi-Direct worker thread.
    	Thread wifiDirectThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
		    	try 
		    	{	
		    		// Create server socket.
		            ServerSocket serverSocket = new ServerSocket(60001);
		            
		            publishProgress("1--About to start blocking for Wifi Direct connection!");
		            Log.i("CHARBEL", "1--About to start blocking for Wifi Direct connection!");
		            
		            // Block until connection acceptance.
		            Socket client = serverSocket.accept();
		            
		            Log.i("Charbel", "1--WiFi-Direct Connected: "+client.isConnected());
		            publishProgress("1--WiFi-Direct Connected: "+client.isConnected());

		            // Get a handle on the stream of bytes coming in.
		            InputStream wifiDirectIS = client.getInputStream();
		            
		            publishProgress("1--before loop");
		            Log.i("CHARBEL", "1--before loop");
		            
		            // Create a thread lock.
		            Lock myLock = new ReentrantLock();
		            
		            // Read bytes in from stream.
		            int byteNum = wifiDirectIS.read(wifiDirectBuffer);
		            
		            // Read bytes from stream until there are none left.
		            while(byteNum != -1)
		            {
		            	try
		            	{
		            		// Lock thread.
		            		myLock.lock();
		            		
		            		publishProgress("1--"+new String (wifiDirectBuffer) );
			            	Log.i("CHARBEL", "++RECEIVED++++: "+new String (wifiDirectBuffer));
			            	
			            	// Write bytes to output stream.
			            	wifiDirectBAOS.write(wifiDirectBuffer);
			            	
			            	// Read next set of bytes.
			            	byteNum = wifiDirectIS.read(wifiDirectBuffer);
		            	}
		            	finally
		            	{
		            		// Finally unlock thread.
		            		myLock.unlock();
		            	}
		            	
		            	// Sleep to give other worker thread a chance to receive bytes.
		            	try {
							Thread.sleep(3);
						} catch (InterruptedException e) {
							Log.i("CHARBEL", "InterruptedException: "+e.getMessage());
							e.printStackTrace();
						}
		            	
		            }
		            
		            // Close input stream.
					wifiDirectIS.close();
					
					// Close socket.
			        serverSocket.close();
			        
			        publishProgress("1--Wifi direct transfer done!!!!!");    
			        Log.i("CHARBEL", "1--Wifi direct transfer done!!!!");
		            
		        } catch (IOException e) {
		            Log.e("CHARBEL", "1--SERVER !!!!!!I/O Exception: "+e.getMessage());
		            e.printStackTrace();
		        }
				
			}
		});
    	
    	// Create Bluetooth worker thread.
    	Thread bluetoothThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try
				{
			        // Use RFCOMM UUID.
			        UUID mUUID = UUID.fromString("00000003-0000-1000-8000-00805F9B34FB");
					
			        // Get Bluetooth server socket.
					BluetoothServerSocket mBluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Assignment 1 Bluetooth Sever", mUUID);
					
					Log.i("CHARBEL", "2--BluetoothServerSocket: about to block for client!");
					publishProgress("2--bluetooth about to block for client!"); 
					
					// Block to get Bluetooth socket.
					BluetoothSocket mBluetoothSocket = mBluetoothServerSocket.accept();
					
					Log.i("CHARBEL", "2--BlueTooth Socket accpeted!");
					publishProgress("2--BlueTooth Socket accpeted!"); 
					
					// Unless you want to accept additional connections, call close(). This releases the server socket and all its resources, but
					// does not close the connected BluetoothSocket that's been returned by accept().
					mBluetoothServerSocket.close();
					
					Log.i("CHARBEL", "2--bluetooth can receive data now!");
					publishProgress("2--bluetooth can receive data now!"); 
					
					// Get Bluetooth input stream.
					InputStream bluetoothIS = mBluetoothSocket.getInputStream();
		            
		            publishProgress("2--before Bluethooth loop");
		            
		            // Receive bytes until none left.
					while(true)
		            {
						try
						{
							// Read byte from input input stream.
							bluetoothIS.read(bluetoothBuffer);
							
							//publishProgress(new String (bluetoothBuffer) );
							//Log.i("CHARBEL", "2--"+new String (bluetoothBuffer) );
							
							// Write byte to output stream.
							bluetoothBAOS.write(bluetoothBuffer);
			            	
						}
						catch(IOException e)
						{
							// Used for exiting the loop when there are no more bytes to read.
							break;
						}	
		            }
					
					// Close input stream and socket.
		            bluetoothIS.close();
					mBluetoothSocket.close();
					
				}
				catch (IOException e) {
		            Log.e("CHARBEL", "2--BT SERVER !!!!!!I/O Exception: "+e.getMessage());
		        }
				
			}
			
			
		});
    	
    	bluetoothThread.start();
    	wifiDirectThread.start();
    	
    	// Try to block background ASync thread until worker threads have finished.
    	try {
    		
    		Log.i("CHARBEL", "Attempting to block ASync thread until worker threads have finished!");
    		
			wifiDirectThread.join();
			bluetoothThread.join();
			
		} catch (InterruptedException e) {
			Log.e("CHARBEL", "InterruptedException: "+e.getMessage());
			e.printStackTrace();
		}
    	
    	Log.i("CHARBEL", "ASync Exiting!");

    	return null;
    }
    
    
    
    /**
     * Start activity that can handle the JPEG image
     */
    @Override
    protected void onPostExecute(String result) {
        	
    		// Put file together using two received streams.
    		this.compileFile();
    		
    		// Get a handle on views.
    	    TextView textView1 = (TextView) this.myActivity.findViewById(R.id.textView1);
    	    TextView textView2 = (TextView) this.myActivity.findViewById(R.id.textView2);
    		
        	
        	Log.e("CHARBEL", "File Successfully Received!");
        	
        	// Send system broadcast to update media gallery.
        	this.myActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
        	
        	// Turn off Bluetooth.
        	this.mBluetoothAdapter.disable();
        	
        	// Remove this hosts associate with WiFi-Direct group.
        	this.myWifiDirectManager.removeGroup(myWifiDirectChannel, new ActionListener() {
				
				@Override
				public void onSuccess() {
					// Do Nothing.
				}
				
				@Override
				public void onFailure(int reason) {
					// Do Nothing.
				}
			});
        	
        	textView1.setText("File Successfully Received!");
        	textView2.setText("File Successfully Received!");
    }

    
    @Override
    protected void onProgressUpdate(String... values) {
    	super.onProgressUpdate(values);
    	
    	// Handle on views.
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
     * compileFile()
     * This is a function that is used to combine the two bye streams (from the wireless interfaces) into a single byte stream. This final stream is used 
     * to create the file that was transferred.
     */
    private void compileFile()
    {
    	// Get a handle on views.
	    TextView textView1 = (TextView) this.myActivity.findViewById(R.id.textView1);
	    TextView textView2 = (TextView) this.myActivity.findViewById(R.id.textView2);
	    
    	Log.i("Charbel", "About to join the two byte streams!");
    	
    	try
    	{
    		// Get handle on purposed file to make.
	    	File file = new File(Environment.getExternalStorageDirectory(), "/download/test_"+System.currentTimeMillis()+".jpg");
			
	    	// Create the file.
			file.createNewFile();
			
			// Prepare file for byte stream input.
			FileOutputStream fos = new FileOutputStream(file);
			
			Log.i("Charbel", ">> Writting streams to file!!!");
			textView1.setText(">> Writting streams to file!!!");
			
			// Feed both byte streams into the file.
			fos.write(this.wifiDirectBAOS.toByteArray());
			fos.write(this.bluetoothBAOS.toByteArray());
			
			textView2.setText("After File stream byte write!!!!!!");
			Log.i("Charbel", "After File stream byte write!!!!!");
			
			// Close file stream.
			fos.close();
			
		} catch (IOException e) {
			Log.e("CHARBEL", "SERVER !!!!!!I/O Exception: "+e.getMessage());
			e.printStackTrace();
		}
    	
    	
    	
    	
    	
    	
    	
    	
    }


    
    
    
}
