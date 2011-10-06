// 	Copyright 2010 Justin Taylor
// 	This software can be distributed under the terms of the
// 	GNU General Public License. 

package org.example.touch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import android.util.Log;

public class Touch extends Activity{
	private EditText ipField;
	private EditText portField;
	
	private SeekBar sensitivity;
	
	private boolean firstRun = true;
	
/***********************************************************************************

	Activity Lifecycle
	
***********************************************************************************/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		ipField = (EditText) findViewById(R.id.EditText01);
		portField = (EditText) findViewById(R.id.EditText02);
		sensitivity = (SeekBar) findViewById(R.id.SeekBar01);
		
		ipField.setText("192.168.1.2");	
		portField.setText("5444");
		
	    // Set button listeners
	    Button connectbutton = (Button) findViewById(R.id.Button01);
	    connectbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				connectToServer();
			}
		});
	    
	    Button disconnectbutton = (Button) findViewById(R.id.Button02);
	    disconnectbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				closeConnectionToServer();
			}
		});
	    
	}
	
	@Override
	protected void onResume(){
		super.onResume();

		AppDelegate appDel = ((AppDelegate)getApplicationContext());
		
		if(!appDel.connected() && !firstRun){
			serverUnreachablealert();
		}
		
		appDel.stopServer();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		firstRun = false;
	}
	
	
	
/***********************************************************************************

	Network and Server Status Alerts
	
***********************************************************************************/
	
	private void networkUnreachableAlert(){
	    AlertDialog network_alert = new AlertDialog.Builder(this).create();
	    network_alert.setTitle("Network Unreachable");
	    network_alert.setMessage("Your device is not connected to a network.");
	    network_alert.setButton("Ok", new DialogInterface.OnClickListener() {
			//@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
	    network_alert.show();
	}
		
	private void serverUnreachablealert(){
		AlertDialog alert = new AlertDialog.Builder(this).create();
	    alert.setTitle("Server Connection Unavailable");
	    alert.setMessage("Please make sure the server is running on your computer");
	    alert.setButton("Ok", new DialogInterface.OnClickListener() {
			//@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
	    
	    alert.show();
	}

	
	
/***********************************************************************************

	Button Handlers used to connect to the server through the AppDelegate
	
***********************************************************************************/
	
	private void connectToServer() {
		AppDelegate appDel = ((AppDelegate)getApplicationContext());
		if(!appDel.canAccessNetwork()){
			networkUnreachableAlert();
			return;
		}
				
		if(!appDel.connected()){
			String serverIp;
			int serverPort;
			
			serverIp = ipField.getText().toString();
			serverPort = Integer.parseInt(portField.getText().toString());
			
			appDel.createClientThread(serverIp, serverPort);
		}
		
		//TODO find better way to check for connection to the server
		int x;
		for(x=0;x<4;x++){// every quarter second for one second check if the server is reachable
			if(appDel.connected()){
				Intent controller = new Intent(Touch.this, Controller.class);
				controller.putExtra("sensitivity" , Math.round( sensitivity.getProgress() /20) + 1);
				startActivity( controller );
				x = 6;
			}
			try{Thread.sleep(250);}
			catch(Exception e){}
		}
		/////////////////////////////////////////////////////////////////////
		
		
		
		if(!appDel.connected())
			serverUnreachablealert();
	}
	
	private void closeConnectionToServer(){
		AppDelegate appDel = ((AppDelegate)getApplicationContext());
		appDel.stopServer();
	}
}
