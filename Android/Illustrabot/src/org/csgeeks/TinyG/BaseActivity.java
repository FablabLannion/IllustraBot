package org.csgeeks.TinyG;

// Copyright 2012 Matthew Stock

import org.csgeeks.TinyG.Net.TinyGNetwork;
import org.csgeeks.TinyG.Support.TinyGService;
import org.csgeeks.TinyG.Support.TinyGService.TinyGBinder;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.gesture.GestureOverlayView;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class BaseActivity extends SherlockFragmentActivity implements /*FileFragment.FileFragmentListener,*/SensorListener , DrawingFragment.DrawingFragmentListener, JogFragment.JogFragmentListener/*, MotorFragment.MotorFragmentListener, AxisFragment.AxisFragmentListener, SystemFragment.SystemFragmentListener*/ {
	private static final String TAG = "TinyG";
	private TinyGService tinyg;
	private Menu menu;
	private int bindType = 0;
	private boolean connected = false;
	private boolean mBound;
	private ServiceConnection mConnection = new DriverServiceConnection();
	private PrefsListener mPreferencesListener;
//	private Download mDownload;
	private BroadcastReceiver mIntentReceiver;
	
	 // For shake motion detection.
    private SensorManager sensorMgr;
    private long lastUpdate = -1;
    private float x, y, z;
    private float last_x, last_y, last_z;
	public boolean drawingMode=false;
	private GestureOverlayView gestures;
    private static final int SHAKE_THRESHOLD = 1500;
    private Handler handler;
	private long timerPeriod=1000; //in ms
	private int AliveTrigger=0;
	
	@Override
	public void onResume() {
		IntentFilter updateFilter = new IntentFilter();
		updateFilter.addAction(TinyGService.STATUS);
		updateFilter.addAction(TinyGService.CONNECTION_STATUS);
		mIntentReceiver = new TinyGServiceReceiver();
		registerReceiver(mIntentReceiver, updateFilter);

		super.onResume();
	}

	@Override
	public void onPause() {
		unregisterReceiver(mIntentReceiver);
		
		if (sensorMgr != null) {
		    sensorMgr.unregisterListener(this,
	                SensorManager.SENSOR_ACCELEROMETER);
		    sensorMgr = null;
	        }
		
		
		super.onPause();
	}

	
	

    public void onAccuracyChanged(int arg0, int arg1) {
	// TODO Auto-generated method stub
    }
 
    public void onSensorChanged(int sensor, float[] values) {
	if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
	    long curTime = System.currentTimeMillis();
	    // only allow one update every 100ms.
	    if ((curTime - lastUpdate) >= 100) {
			long diffTime = (curTime - lastUpdate);
			lastUpdate = curTime;
	 
			x = values[SensorManager.DATA_X];
			y = values[SensorManager.DATA_Y];
			z = values[SensorManager.DATA_Z];
	 
			float speed = Math.abs(x+y+z - last_x - last_y - last_z)
	                              / diffTime * 10000;
			if (speed >= SHAKE_THRESHOLD) {
			    // yes, this is a shake action! Do something about it!
				if (drawingMode==true)
				{
					Log.d(TAG, "Shake your body!Boom!Boom!Boom!");
					//clearCanvas();
				}
			}
			last_x = x;
			last_y = y;
			last_z = z;
	    }
	}
    }	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final ActionBar actionBar = getSupportActionBar();

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setHomeButtonEnabled(true);
		Resources res = getResources();
		String[] tabs = res.getStringArray(R.array.tabArray);
		MyTabListener tabListener = new MyTabListener();
		for (int i=0; i < tabs.length; i++) {
			Tab tab = actionBar.newTab();
			tab.setText(tabs[i]);
			tab.setTag(tabs[i]);
			tab.setTabListener(tabListener);
			actionBar.addTab(tab);			
		}

		// Force portrait for now, since we don't really handle the loss of the
		// binding
		// (and subsequent destruction of the service) very well. Revisit later.
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.main);

		mConnection = new DriverServiceConnection();
		Context mContext = getApplicationContext();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		//bindType = Integer.parseInt(settings.getString("tgfx_driver", "0"));
		bindType=0;
		mPreferencesListener = new PrefsListener();
		settings.registerOnSharedPreferenceChangeListener(mPreferencesListener);

		if (savedInstanceState != null) {
			restoreState(savedInstanceState);
		}

		// Do the initial service binding
		mBound = bindDriver(mConnection);
		if (!mBound) {
			Toast.makeText(this, "Binding service failed", Toast.LENGTH_SHORT)
					.show();
		}
		
		// start motion detection
		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		boolean accelSupported = sensorMgr.registerListener(this,
			SensorManager.SENSOR_ACCELEROMETER,
			SensorManager.SENSOR_DELAY_GAME);
	 
		if (!accelSupported) {
		    // on accelerometer on this device
		    sensorMgr.unregisterListener(this,
	                SensorManager.SENSOR_ACCELEROMETER);
		}
		
        handler = new Handler();

	}

	private boolean bindDriver(ServiceConnection s) {
		switch (bindType) {
		case 0: // Network
			return bindService(new Intent(getApplicationContext(), TinyGNetwork.class), s,
					Context.BIND_AUTO_CREATE);
//		case 1: // USB host
//			// Check to see if the platform supports USB host
//			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
//				Toast.makeText(this, R.string.no_usb_host, Toast.LENGTH_SHORT)
//						.show();
//				return false;
//			}
//			return bindService(new Intent(getApplicationContext(), USBHostService.class), s,
//					Context.BIND_AUTO_CREATE);
//		case 2: // USB accessory
//			// Check to see if the platform support USB accessory
//			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
//				Toast.makeText(this, R.string.no_usb_accessory, Toast.LENGTH_SHORT)
//						.show();
//				return false;
//			}
//			return bindService(new Intent(getApplicationContext(), USBAccessoryService.class), s,
//					Context.BIND_AUTO_CREATE);
		default:
			return false;
		}
	}

	@Override
	public void onDestroy() {
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		super.onDestroy();
	}

	// This is how we get messages from the TinyG service. Two different message
	// types - a STATUS giving us
	// updates from an SR statement, and a CONNECTION_STATUS signal so that we
	// know if the service is connected
	// to the USB or network port.
	public class TinyGServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();
			String action = intent.getAction();
			if (action.equals(TinyGService.STATUS)) {
				//StatusFragment sf = (StatusFragment) getSupportFragmentManager().findFragmentById(R.id.statusF);
				//sf.updateState(b);
				Fragment f = getSupportFragmentManager().findFragmentById(R.id.tabview);
				if (f != null && f.getClass() == JogFragment.class)
					((JogFragment) f).updateState(b);
			}
			if (action.equals(TinyGService.CONNECTION_STATUS)) {
				Log.d(TAG, "Got CONNECTION_STATUS broadcast");
				connected = b.getBoolean("connection");
				if (connected==false)
				{
					Toast.makeText(getApplicationContext(),
							"Connection lost :-(",
							Toast.LENGTH_SHORT).show();
				}
				invalidateOptionsMenu();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.options, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		this.menu = menu;
		MenuItem menuConnect = menu.findItem(R.id.connect);
		if (connected)
			menuConnect.setTitle(R.string.disconnect);
		else
			menuConnect.setTitle(R.string.connect);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		Context mContext = getApplicationContext();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		//bindType = Integer.parseInt(settings.getString("tgfx_driver", "0"));
	    AliveTrigger = Integer.parseInt(settings.getString("AliveTrigger", "0"));
		
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:			
			if (drawingMode==true)
			{
				Log.d(TAG, "Clear Drawing...");
				clearCanvas();
			}			
			return true;
		case R.id.connect:
			if (tinyg == null)
				return true;
			if (connected) {
				tinyg.disconnect();
				if (AliveTrigger == 0) {
					((Handler) handler).removeCallbacks(runnable_alive);
				}
				
			} else {
				tinyg.connect();
				if (AliveTrigger == 1) {
				    handler.postDelayed(runnable_alive, timerPeriod);
				}
			}
			return true;
		case R.id.settings:
			startActivity(new Intent(this, EditPreferencesActivity.class));
			return true;
		case R.id.about:
	        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        AboutFragment af = new AboutFragment();
	        af.show(ft, "about");
			return true;
		case R.id.refresh:
//			if (mDownload != null)
//				return true;
			if (connected) {
				tinyg.refresh();
			} else {
				Toast.makeText(this, "Not connected!", Toast.LENGTH_SHORT)
						.show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("bindType", bindType);
		outState.putBoolean("connected", connected);
	}

	private void restoreState(Bundle inState) {
		bindType = inState.getInt("bindType");
		connected = inState.getBoolean("connected");
	}

	
	public void myClickHandler(View view) {
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.tabview);
		if (f == null)
			return;

		// Ugly!
//		if (f.getClass() == MotorFragment.class)
//			((MotorFragment) f).myClickHandler(view);
//		if (f.getClass() == AxisFragment.class)
//			((AxisFragment) f).myClickHandler(view);
//		if (f.getClass() == SystemFragment.class)
//			((SystemFragment) f).myClickHandler(view);
		if (f.getClass() == JogFragment.class)
			((JogFragment) f).myClickHandler(view);
//		if (f.getClass() == FileFragment.class)
//			((FileFragment) f).myClickHandler(view);
		if (f.getClass() == DrawingFragment.class)
			((DrawingFragment) f).myClickHandler(view);
	}
	
	

	// We get a driver binding, and so we create a helper class that interacts
	// with the Messenger.
	// We can probably redo this as a subclass.
	private class DriverServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder service) {
			TinyGBinder binder = (TinyGBinder) service;
			tinyg = binder.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			tinyg = null;
		}
	}

	// Make sure we rebind services if we change the preference.
	private class PrefsListener implements
			SharedPreferences.OnSharedPreferenceChangeListener {
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if (key.equals("tgfx_driver")) {
				Log.d(TAG, "Changing binding");
				/*
				bindType = Integer.parseInt(sharedPreferences.getString(
						"tgfx_driver", "0"));
						*/
				bindType=0;
				if (mBound) {
					unbindService(mConnection);
					mBound = false;
				}
				mBound = bindDriver(mConnection);
				if (!mBound) {
					Toast.makeText(BaseActivity.this,
							"Binding service failed", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
	}

//	public void onSystemSelected() {
//		if (tinyg == null)
//			return;
//		Log.d(TAG, "Sending GET_MACHINE message");
//		Bundle b = tinyg.getMachineStatus();
//		Fragment f = getSupportFragmentManager().findFragmentById(R.id.tabview);
//		if (f != null && f.getClass() == SystemFragment.class)
//			((SystemFragment) f).updateState(b);	
//	}
//	public void onMotorSelected(int m) {
//		if (tinyg == null)
//			return;
//		Log.d(TAG, String.format("Sending GET_MOTOR message %d", m));
//		Bundle b = tinyg.getMotor(m);
//		Fragment f = getSupportFragmentManager().findFragmentById(R.id.tabview);
//		if (f != null && f.getClass() == MotorFragment.class)
//			((MotorFragment) f).updateState(b);				
//	}
//
//	public void onAxisSelected(int a) {
//		if (tinyg == null)
//			return;
//		Bundle b = tinyg.getAxis(a);
//		Fragment f = getSupportFragmentManager().findFragmentById(R.id.tabview);
//		if (f != null && f.getClass() == AxisFragment.class)
//			((AxisFragment) f).updateState(b);			
//	}

	public boolean connectionState() {
		return connected;
	}
	
	private class MyTabListener implements ActionBar.TabListener {
		
		


		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			Fragment f;
			FragmentManager fm = getSupportFragmentManager();
			f = fm.findFragmentByTag((String) tab.getText());
			if (tab.getText().equals("Drawing"))
			{
				drawingMode=true;			
			}
			else
			{
				drawingMode=false;				
			}
			
			if (f == null) {
				if (tab.getText().equals("Drawing"))
				{
					f = new DrawingFragment();
				}
				else // Jog
				{			
					f = new JogFragment();				
				}
				ft.add(R.id.tabview, f, (String) tab.getText());
			} else {
				if (f.isDetached())
					ft.attach(f);
			}
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			FragmentManager fm = getSupportFragmentManager();
			Fragment f = fm.findFragmentByTag((String) tab.getText());
			if (f != null) {
				ft.detach(f);
			}
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}
	}

	public void jogChange(float rate) {
		Bundle b = new Bundle();
		b.putFloat("jogRate", rate);
		//StatusFragment sf = (StatusFragment) getSupportFragmentManager().findFragmentById(R.id.statusF);
		//sf.updateState(b);		
	}

	public void toggleDownload(String filename) {
		if (tinyg == null || !connected)
			return;
		
//		// stop downloading
//		if (mDownload != null) {
//			FileFragment ff = (FileFragment) getSupportFragmentManager().findFragmentById(R.id.tabview);
//			if (ff != null)
//				ff.updateState(false);					
//			mDownload.cancel();
//			// TODO Send interrupt
//			mDownload = null;
//		} else {
//			mDownload = new Download(this, tinyg);
//			mDownload.openFile(filename);
//			FileFragment ff = (FileFragment) getSupportFragmentManager().findFragmentById(R.id.tabview);
//			if (ff != null)
//				ff.updateState(true);					
//		}
	}

	public void sendGcode(String cmd) {
		if (tinyg == null || !connected)
			return;
		tinyg.send_gcode(cmd);
	}
	
	
	public void clearCanvas() {
		Log.d(TAG, "Clear Canvas");
		gestures.cancelClearAnimation();
		gestures.clear(true);
		//Request pen to go to default position : 0,0
		sendGcode("g0x" + 0 + "y" + 0 + "z5"  );
		Log.i("Gcode","g0x" + 0 + "y" + 0 + "z5");
	}
	public void recordCanvas(GestureOverlayView lgestures) {
		Log.d(TAG, "Record Canvas Id");
		gestures=lgestures;

	}	
	
	public Runnable runnable_alive = new Runnable() {
		   public void run() {
			   if (AliveTrigger == 1) {
			    Log.d("TimerDrawing", "Timer trig");
			    sendGcode("ALIVE");
			    /* and here comes the "trick" */
			   	handler.postDelayed(this, timerPeriod);
			  }
		   }
		};

}
