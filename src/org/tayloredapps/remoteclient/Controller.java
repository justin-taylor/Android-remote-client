// 	Copyright 2010 Justin Taylor
// 	This software can be distributed under the terms of the
// 	GNU General Public License. 

package org.tayloredapps.remoteclient;

import org.example.touch.R;

import messages.Constants;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.View.OnTouchListener;
import android.view.View.OnKeyListener;
import android.view.*;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class Controller extends Activity implements OnTouchListener, OnKeyListener{
	
	float lastXpos = 0;
	float lastYpos = 0;
	
	private int mouse_sensitivity = 1;
	private float screenRatio = 1.0f;
	
	boolean keyboard = false;
	Thread checking;
		
	Button Left;
	Button Right;
	
	int count = 0;
	int FRAME_RATE = 10;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.control);
		
	    mouse_sensitivity = getIntent().getExtras().getInt("sensitivity");
	    screenRatio = getIntent().getExtras().getFloat("ratio");
	    
	    // Set the width of the buttons to half the screen size
	 	Display display = getWindowManager().getDefaultDisplay(); 
	 	int width = display.getWidth();
	 	
	 	Left = (Button) findViewById(R.id.LeftClickButton);
	 	Right =  (Button) findViewById(R.id.RightClickButton);
	 	
	 	Left.setWidth(width/2);
	 	Right.setWidth(width/2);
	 	
	 	Left.setOnTouchListener(this);
	 	Right.setOnTouchListener(this);
	 	
	 	ClientListener.deviceWidth = width;
	 	ClientListener.deviceHeight = display.getHeight() - Left.getHeight();
	 	
	    View touchView = (View) findViewById(R.id.TouchPad);
	    touchView.setOnTouchListener(this);

	    EditText editText = (EditText) findViewById(R.id.KeyBoard);
	    editText.setOnKeyListener(this);
	    editText.addTextChangedListener(new TextWatcher(){
		    public void  afterTextChanged (Editable s){
		    	try{
		    		sendToAppDel(Constants.KEYBOARD + s.toString());
        		} catch(IndexOutOfBoundsException e){}
		    	s.clear();
		    } 
		    
	        public void  beforeTextChanged  (CharSequence s, int start, int count, int after){} 
	        public void  onTextChanged  (CharSequence s, int start, int before, int count) {
	        }
	    });
	    
	    setImageRequestSizes();
	    AppDelegate appDel = ((AppDelegate)getApplicationContext());
	    appDel.setController( this );
	}
		
	private void setImageRequestSizes() {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		display.getMetrics(metrics);
		int width, height; 
		width = metrics.widthPixels;
		height = metrics.heightPixels;
		
		ClientListener.deviceWidth = (int)(screenRatio * width);
	    ClientListener.deviceHeight = (int)(screenRatio * height);
	    Log.e("REQUESTINGSIZE", screenRatio+" "+ ClientListener.deviceWidth+" "+ClientListener.deviceHeight);
	}
	
	public void finish()
	{
		AppDelegate appDel = ((AppDelegate)getApplicationContext());
		appDel.stopServer();
		
		super.finish();
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
	    setImageRequestSizes();
	 	super.onConfigurationChanged(newConfig);
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		if(v == Left){
			 switch ( event.getAction() ) {
			    case MotionEvent.ACTION_DOWN: sendToAppDel(Constants.LEFTMOUSEDOWN); break;
			    case MotionEvent.ACTION_UP: sendToAppDel(Constants.LEFTMOUSEUP); break;
			 }
		}else if( v == Right){
			switch ( event.getAction() ) {
		    	case MotionEvent.ACTION_DOWN: sendToAppDel(Constants.RIGHTMOUSEDOWN); break;
		    	case MotionEvent.ACTION_UP: sendToAppDel(Constants.RIGHTMOUSEUP); break;
			}
		}
		else
			mousePadHandler(event);
	 	
		return true;
	}
	
	// detect keyboard event
	// and send to delegate
	//@Override
	public boolean onKey(View v, int c, KeyEvent event){
		
		// c is the event keycode
		if(event.getAction() == 1)
		{
			sendToAppDel( "" + Constants.KEYCODE+c);
		}
		// this will prevent the focus from moving off the text field
	 	if(		c == KeyEvent.KEYCODE_DPAD_UP   ||
	 			c == KeyEvent.KEYCODE_DPAD_DOWN ||
	 			c == KeyEvent.KEYCODE_DPAD_LEFT ||
	 			c == KeyEvent.KEYCODE_DPAD_RIGHT
	 	)
	 		return true;
	 	
	 	return false;
	}
	
	// Show and hide Keyboard by setting the
	// focus on a hidden text field
    public void keyClickHandler(View v){
    	EditText editText = (EditText) findViewById(R.id.KeyBoard);
    	InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    	if(keyboard){
    		mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    		keyboard = false;
    	}
    	else{
    		mgr.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    		keyboard = true;
    	}
    }

	// send message to AppDelegate class
	// to be sent to server on client desktop
	private void sendToAppDel(String message){
		AppDelegate appDel = ((AppDelegate)getApplicationContext());
		if(appDel.connected()){
			appDel.sendMessage(message);
		}
		else{
			finish();
		}
	}
	
	private void sendToAppDel(char c){
		sendToAppDel(""+c);
	}
	
	
	public void setImage(final Bitmap bit){
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {

			public void run() {
				LinearLayout layout = (LinearLayout) findViewById(R.id.TouchPad);
				BitmapDrawable drawable = new BitmapDrawable( bit );
				layout.setBackgroundDrawable( drawable );				
			}
		});
		

	}
	
	// send a mouse message
    private void mousePadHandler(MotionEvent event) {
 	   int action = event.getAction();
 	   int touchCount = event.getPointerCount();
 	   
	   // if a single touch
 	   if(touchCount == 1){ 		   
			switch(action){
			
				case 0:	// touch down
				 		lastXpos = event.getX();
						lastYpos = event.getY();
						break;
				
				case 1:	// touch up
						long deltaTime = event.getEventTime() - event.getDownTime();
						if(deltaTime < 250)
							sendToAppDel(Constants.LEFTCLICK);
						break;
				
				case 2: // moved
					float deltaX = (lastXpos - event.getX()) * -1;
					float deltaY = (lastYpos - event.getY()) * -1;
					
					sendToAppDel(Constants.createMoveMouseMessage(deltaX * mouse_sensitivity
																, deltaY * mouse_sensitivity));
						
					lastXpos = event.getX();
					lastYpos = event.getY();
					break;
						
				default: break;
			}
 	   }
 
	   // if two touches send scroll message
	   // based off MAC osx multi touch scrolls up and down
 	   else if(touchCount == 2){
 		   if(action == 2){
 			   
 				float deltaY = event.getY() - lastYpos;
 				float tolerance = 10;
 			   
 			   if (deltaY > tolerance){
 				  sendToAppDel(Constants.SCROLLUP);
 				  lastYpos = event.getY();
 			   }
 			   else if(deltaY < -1 * tolerance){
 				  sendToAppDel(Constants.SCROLLDOWN);
 				 lastYpos = event.getY();
 			   }
 		   }else lastYpos = event.getY();
 	   }
 	}
}
