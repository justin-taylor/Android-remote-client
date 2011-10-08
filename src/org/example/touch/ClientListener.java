package org.example.touch;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import messages.Constants;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ClientListener implements Runnable{

	private InetAddress serverAddr;
	private int serverPort;
	private DatagramSocket socket;
	byte[] buf = new byte[65000];
	private DatagramPacket dgp;
	private AppDelegate delegate;
	private int framesPerSecond = -1;
	public boolean connected = false;
	
	public ClientListener(int port, int fps, AppDelegate del){
		delegate = del;
		framesPerSecond = fps;
		
		try{
			String ip = getLocalIpAddress();
			dgp = new DatagramPacket(buf, buf.length);
			serverAddr = InetAddress.getByName(ip);

		}catch (Exception e){
			Log.e("ClientActivity", "C: Error", e);
		}
		serverPort = port;
	}
	
	public void run() {
	       try {
	           socket = new DatagramSocket(serverPort, serverAddr);
	           connected = true;
	           
	           //Timer timer = new Timer();
	           //timer.scheduleAtFixedRate(getImageTask, 0, 100);
	           
	           listen();
	           
	           delegate.sendMessage(Constants.REQUESTIMAGE+"");
	       }
	       catch (Exception e) {
	           Log.e("ClientActivity", "Client Connection Error", e);
	       }
	   }
	
		TimerTask getImageTask = new TimerTask(){
	
			@Override
			public void run() {
				 delegate.sendMessage(Constants.REQUESTIMAGE+"");
			}
			
		};
	
	  public void listen()
	  {
		   	Log.e("LISTENING", "LISTEING at "+serverAddr.getHostAddress()+" on "+serverPort);

		   	while(connected){

		   		try{
		   			delegate.sendMessage(Constants.REQUESTIMAGE+"");
		   			socket.receive(dgp);
		   			Bitmap bm = BitmapFactory.decodeByteArray(dgp.getData(), 0, 65000);
		   			//Looper.prepare();   
		   			delegate.getController().setImage(bm);
		   			Log.d("Testing", "Received image");

		   		}catch(Exception e){
		   			Log.d("Error", "Could not receive image");
		   			e.printStackTrace();
		   		}
		   	}
	  }
	
	   public static String getLocalIpAddress() {
		    try {
		        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
		            NetworkInterface intf = en.nextElement();
		            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
		                InetAddress inetAddress = enumIpAddr.nextElement();
		                if (!inetAddress.isLoopbackAddress()) {
		                	Log.d("HERRO", inetAddress.getHostAddress().toString());
		                    return inetAddress.getHostAddress().toString();
		                }
		            }
		        }
		    } catch (SocketException ex) {
		        Log.e("", ex.toString());
		    }
		    return null;
		}
	   
	   public void closeSocket(){
		   	socket.close();
		   	connected = false;
	   }
	   
}
