package org.csgeeks.TinyG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.GestureOverlayView.OnGesturingListener;
import android.gesture.GestureStroke;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;

public class DrawingFragment extends SherlockFragment implements OnTouchListener {
	private static final String TAG = "TinyG";
	private DrawingFragmentListener mListener;
	private OnGesturingListener GestureListener;
	private View mView;
	private VelocityTracker mVelocityTracker = null;
	private Integer nb_points=0;
	private float drawingVelocity;
	private SharedPreferences settings;
//	private GestureLibrary mLibrary;
	private GestureOverlayView gestures;
	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (DrawingFragmentListener) activity;

		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement DrawingFragmentListener");
		}
	}

	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	
    	// Inflate the layout for this fragment
    	mView = inflater.inflate(R.layout.drawing, container, false);
    	Context mContext = mView.getContext();
		settings = PreferenceManager
				.getDefaultSharedPreferences(mContext);  
		
		drawingVelocity = Float.parseFloat(settings.getString("drawingVelocity", "100"));
		Log.i("Velocity", "drawingVelocity = "+drawingVelocity);
        gestures = (GestureOverlayView) mView.findViewById(R.id.gestures);
        gestures.setOnTouchListener(this);
        //Send to BaseActivity gestures id for clearing processing
        mListener.recordCanvas(gestures);
        //Initiate a handler for timer processing
        
        return mView;
    }


	public void myClickHandler(View view) {
		//nothing
	}
	
	public interface DrawingFragmentListener {
		void sendGcode(String cmd);
		
		void recordCanvas(GestureOverlayView gestures);
		
	}
	
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		
		//TODO : deal with multiple strokes ?
		GestureStroke expectedStroke = gesture.getStrokes().get(0);
		int nbPoints=expectedStroke.points.length;
		float[] Xpoints = new float[nbPoints / 2];
		float[] Ypoints = new float[nbPoints / 2];    		
        for (int i=0; i < (nbPoints/2); i++) {
            int count = expectedStroke.points.length;
            float cx,cy;
            //Extract x and y coordinates
            cx=expectedStroke.points[i*2];
            cy=expectedStroke.points[i*2+1];            
            Log.i("Drawing","("+cx+","+cy+")");
			mListener.sendGcode("g0x" + cx + "y" + cy + "z0"  );

            Xpoints[i]=cx;
            Ypoints[i]=cy;
        }	
		nbPoints=nbPoints+1;
		

	}
	
	public void onGesturingStarted(GestureOverlayView overlay)
	{

	}
	public void onGesturingEnded(GestureOverlayView overlay)
	{
		
	}


	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);
		float velocityX;
		float velocityY;
		float yOffset;
		float cx;
		float cy;
		
		drawingVelocity = Float.parseFloat(settings.getString("drawingVelocity", "100"));
		//Log.i("Velocity", "drawingVelocity = "+drawingVelocity);
		
		switch(action) {
	 		case MotionEvent.ACTION_DOWN:
	 			if(mVelocityTracker == null) {
	 				// Retrieve a new VelocityTracker object to watch the velocity of a motion.
	 				mVelocityTracker = VelocityTracker.obtain();
	 			} else {
	 				// Reset the velocity tracker back to its initial state.
	 				mVelocityTracker.clear();
	 			}
	 			// Add a user's movement to the tracker.
	 			mVelocityTracker.addMovement(event);
	 			// 
	 			 cx = event.getRawX();
		         cy = event.getRawY();
			      //Remove offset for y
	            	yOffset = Float.parseFloat(settings.getString("yOffset", "200"));
	            	cy=cy-yOffset;
	            	if (cy<=0)
	            	{
	            				cy=0;
	            	}
            	
            	mListener.sendGcode("g0x" + cx + "y" + cy + "z5");
            	Log.i("Gcode, init mode","g0x" + cx + "y" + cy + "z5");
		        
	 			break;
	 		case MotionEvent.ACTION_MOVE:
	 			mVelocityTracker.addMovement(event);
	 			 cx = event.getRawX();
		         cy = event.getRawY();
		        nb_points++;
		        //  Code to display x and y go here
		        // When you want to determine the velocity, call 
                // computeCurrentVelocity(). Then call getXVelocity() 
                // and getYVelocity() to retrieve the velocity for each pointer ID. 
                mVelocityTracker.computeCurrentVelocity(1000);
                velocityX = VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId);
                velocityY = VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId);
                
                float abs_velocityX = (velocityX < 0) ? -velocityX : velocityX;
                float abs_velocityY = (velocityY < 0) ? -velocityY : velocityY;
                
                // Log velocity of pixels per second
                // Best practice to use VelocityTrackerCompat where possible.
                Log.d("Velocity", "X velocity: " + abs_velocityX);
                Log.d("Velocity", "Y velocity: " + abs_velocityY);
                //Log.d("Velocity", "onTouch : x="+cx+" y="+cy);
		        //
                if ((abs_velocityX > drawingVelocity) || (abs_velocityY > drawingVelocity)) {
                	Log.i("Velocity","onTouch : x="+cx+" y="+cy);
                	//Remove offset for y
                	yOffset = Float.parseFloat(settings.getString("yOffset", "200"));
                	cy=cy-yOffset;
                	if (cy<=0)
                	{
                				cy=0;
                	}
                	
                	mListener.sendGcode("g0x" + cx + "y" + cy + "z0");
                	Log.i("Gcode","g0x" + cx + "y" + cy + "z0");
                } else {
                	Log.d("Velocity", "Not send ");
                }

	 			break;
	 		case MotionEvent.ACTION_UP:
	        case MotionEvent.ACTION_CANCEL:
	        	 float cxl = event.getRawX();
		         float cyl = event.getRawY();
	        	 Log.d("Velocity", "Send Z up ");
             	//Remove offset for y
             	yOffset = Float.parseFloat(settings.getString("yOffset", "200"));
             	cyl=cyl-yOffset;
             	if (cyl<0)
             	{
             		cyl=0;
             	}	        	 
	        	 mListener.sendGcode("g0x" + cxl + "y" + cyl + "z5"  );
	        	 Log.i("Gcode","g0x" + cxl + "y" + cyl + "z5");
	        	 Log.d("Velocity", "nb_points = "+nb_points);
	        	 nb_points = 0;
	             // Return a VelocityTracker object back to be re-used by others.
	             mVelocityTracker.recycle();
	             break;
		}
		return false;
	}	
	


	
	
}
