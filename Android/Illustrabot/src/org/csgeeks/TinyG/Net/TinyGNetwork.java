package org.csgeeks.TinyG.Net;

// Copyright 2012 Matthew Stock

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.csgeeks.TinyG.R;
import org.csgeeks.TinyG.Support.TinyGService;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class TinyGNetwork extends TinyGService {
	private static final int NETWORK_BUFFER_SIZE = 16 * 1024;
	private String tgfx_hostname;
	private String tgfx_port;
	private Socket socket;
	private Menu menu;	
	protected InputStream is;
	protected OutputStream os;
	protected ListenerTask mListener;
    private static final String TAG = "illustrabot";
	private final WebSocketConnection mConnection = new WebSocketConnection();
	private String wsuri = "ws://10.40.213.140:7681";
	
	
	
	private void send_commands() {
		/*
		send_message("ee", CMD_DISABLE_LOCAL_ECHO);
		send_message("si", CMD_SET_STATUS_UPDATE_INTERVAL);
		send_message("sr", CMD_GET_STATUS_REPORT);
		send_message("qr", CMD_GET_QUEUE_REPORT);
		send_message("x", CMD_GET_X_AXIS);
		send_message("y", CMD_GET_Y_AXIS);
		send_message("z", CMD_GET_Z_AXIS);
		send_message("a", CMD_GET_A_AXIS);
		send_message("b", CMD_GET_B_AXIS);
		send_message("c", CMD_GET_C_AXIS);
		send_message("1", CMD_GET_MOTOR_1_SETTINGS);
		send_message("2", CMD_GET_MOTOR_2_SETTINGS);
		send_message("3", CMD_GET_MOTOR_3_SETTINGS);
		send_message("4", CMD_GET_MOTOR_4_SETTINGS);
		send_message("sys", CMD_GET_MACHINE_SETTINGS);	
		*/
	}
	
	@Override
	public void send_message(String cmd_type, String cmd) {
		String msg = "{"+cmd_type+", "+cmd+"}";
		//queue.put(msg);
		Log.d(TAG, "Sending CMD: " + msg);
		mConnection.sendTextMessage(cmd);
	}
	
	private void start() {
		  
	       
	       // final String wsuri = "ws://192.168.1.92:8787" + "/jWebSocket/jWebSocket";
	       // final String wsuri = "ws://192.168.1.92:8000" + "/test";
		Context mContext = getApplicationContext();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		String wsuri1 = settings.getString("tgfx_hostname", "echo.websocket.org");
		String wsuri2 = settings.getString("tgfx_port", "");
		String wsuri3 = settings.getString("tgfx_dir", "");	
		if (wsuri2=="")
		{
			wsuri ="ws://"+wsuri1 +  wsuri3;	
		
		}
		else
		{
			 wsuri ="ws://"+wsuri1+":"+ wsuri2 +  wsuri3;
		}
		//wsuri = "ws://echo.websocket.org" + "";
			

		
		
	       try {
	          mConnection.connect(wsuri, new WebSocketHandler() {
	  
	             @Override
	             public void onOpen() {
	                Log.d(TAG, "Status: Connected to " + wsuri);
	               // mConnection.sendTextMessage("Starting illustrabot");
	                send_commands();
	             }
	  
	             
	             
	             @Override
	             public void onTextMessage(String payload) {
	                Log.d(TAG, "Got echo: " + payload);
	                
	             }
	  
	             
	             
	             @Override
	             public void onClose(int code, String reason) {
	         		//MenuItem menuConnect = menu.findItem(R.id.connect);	  
	        		//menuConnect.setTitle(R.string.disconnect);            	 
	                Log.d(TAG, "Connection lost.");
					// Let everyone know we are disconnected
					Bundle b = new Bundle();
					b.putBoolean("connection", false);
					Intent i = new Intent(CONNECTION_STATUS);
					i.putExtras(b);
					sendBroadcast(i, null);	                
	             }
	             
	             
	          });
	       } catch (WebSocketException e) {
	  
	          Log.d(TAG, e.toString());
	       }
	    }
	
	
	public void disconnect() {
		super.disconnect();
		try {
			if (is != null)
				is.close();
			if (os != null)
				os.close();
		} catch (IOException e) {
			Log.e(TAG, "Close: " + e.getMessage());
		}
		is = null;
		os = null;

		mConnection.disconnect();
		
		if (mListener != null) {
			mListener.cancel(true);
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
		socket = null;
	}

	public void write(String message) {
		try {
			//os.write(message.getBytes());
			mConnection.sendTextMessage(message);
		} catch (NullPointerException e) {
			Log.e(TAG, "write to network attempted without socket established.");
		}
	}

	// We call the initialize function to configure any local variables, pulling
	// preferences in for example.
	public void connect() {
		initialize();

		new ConnectTask().execute(0);
		Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
	}

	// This AsyncTask runs the client-specific connection code.
	private class ConnectTask extends AsyncTask<Integer, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Integer... params) {
			Log.d(TAG, "Starting connect in background");
			
			start();
			
			//try {
			//	socket = new Socket(tgfx_hostname, tgfx_port);
			//	os = socket.getOutputStream();
			//	is = socket.getInputStream();
			//} catch (Exception e) {
			//	socket = null;
			//	Log.e(TAG, "Socket: " + e.getMessage());
			//	return false;
			//}
			
			// A changer ?
			
			return true;
		}

		protected void onPostExecute(Boolean res) {
			if (res) {
				//mListener = new ListenerTask();
				//mListener.execute(new InputStream[] { is });
				// Let everyone know we are connected
				Bundle b = new Bundle();
				b.putBoolean("connection", true);
				Intent i = new Intent(CONNECTION_STATUS);
				i.putExtras(b);
				sendBroadcast(i, null);
				//refresh();
				////Log.i(TAG, "Listener started, connection_status intent sent");
			} else {
				Toast.makeText(TinyGNetwork.this, "Connection failed",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	protected void initialize() {
		Context mContext = getApplicationContext();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		//tgfx_hostname = settings.getString("tgfx_hostname", "127.0.0.1");
		//tgfx_port = Integer.parseInt(settings.getString("tgfx_port", "4444"));
		 tgfx_hostname = settings.getString("tgfx_hostname", "echo.websocket.org");
		 tgfx_port = settings.getString("tgfx_port", "");
		
	}

	protected class ListenerTask extends AsyncTask<InputStream, String, Void> {
		@Override
		protected Void doInBackground(InputStream... params) {
			byte[] inbuffer = new byte[NETWORK_BUFFER_SIZE];
			byte[] linebuffer = new byte[1024];
			InputStream lis = params[0];
			int cnt, idx = 0;
			try {
				while (!isCancelled()) {
					if ((cnt = lis.read(inbuffer, 0, NETWORK_BUFFER_SIZE)) < 0) {
						Log.e(TAG, "network read failure");
						break;
					}
					for (int i = 0; i < cnt; i++)
						if (inbuffer[i] == '\n') {
							String foo = new String(linebuffer, 0, idx);
							Log.d(TAG, "string inside listenertask: " + foo);
							publishProgress(foo);
							idx = 0;
						} else
							linebuffer[idx++] = inbuffer[i];
				}
			} catch (IOException e) {
				Log.e(TAG, "listener read exception: " + e.getMessage());
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
//			Bundle b;
//			if (values.length <= 0)
//				return;
//			Log.d(TAG, "read = " + values[0]);
//			if ((b = machine.processJSON(values[0])) == null)
//				return;
//			updateInfo(values[0], b);
			
			return;
		}

		@Override
		protected void onCancelled() {
			Log.i(TAG, "ListenerTask cancelled");
		}
	}

}
