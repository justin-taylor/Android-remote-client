package org.example.touch;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

//ClientThread Class implementation
public class ClientThread implements Runnable {
	
	private InetAddress serverAddr;
	private int serverPort;
	private DatagramSocket socket;
	byte[] buf = new byte[1000];
	
	public boolean connected = false;
	
	public ClientThread(String ip, int port){
		try{
			serverAddr = InetAddress.getByName(ip);
		}
		catch (Exception e){
			Log.e("ClientActivity", "C: Error", e);
		}
		serverPort = port;
	}
		
	//Opens the socket and output buffer to the remote server
   public void run() {
       try {
           socket = new DatagramSocket();
           socket.setSoTimeout(1000);
           
           connected = true;
           
           this.getImage();
           connected = testConnection();
           if(connected)
           	surveyConnection();
       }
       catch (Exception e) {
           Log.e("ClientActivity", "Client Connection Error", e);
       }
   }
   
   public void sendMessage(String message){
		try {
           buf = message.getBytes();
           DatagramPacket out = new DatagramPacket(buf, buf.length, serverAddr, serverPort);
           socket.send(out);
       }
		catch (Exception e){ 
			closeSocketNoMessge();
		}
   }
   
   public void closeSocketNoMessge(){
   	socket.close();
   	connected = false;
   }
   
   public void closeSocket(){
   	sendMessage(new String("Close"));
   	socket.close();
   	connected = false;
   }
   
   /*
    *  Image parser used later
    */
   
   public void getImage(){
   	Log.d("IMAGING", "GETING");
   	while(connected){
   		DatagramPacket in = new DatagramPacket(buf, buf.length);
   		try{
   			socket.receive(in);
   			Bitmap bm = BitmapFactory.decodeByteArray(in.getData(), 0, 1000);
   			//controller.setImage(bm);
   			Log.d("Testing", "Received");
   		}catch(Exception e){Log.d("Error", "Could not receive image");}
   	}
   }
   
   /*
    * Used to test connection with the server.
    */
   
   private boolean testConnection(){
       	try {
	        	 Log.d("Testing", "Sending");
	        	 
	        	 if(!connected)buf = new String("Connectivity").getBytes();
	        	 else buf = new String("Connected").getBytes();
	        	 
	             DatagramPacket out = new DatagramPacket(buf, buf.length, serverAddr, serverPort);
	             socket.send(out);
	             Log.d("Testing", "Sent");
	        	}
       	catch(Exception e){return false;}
       	
       	try{
       		Log.d("Testing", "Receiving");
       		DatagramPacket in = new DatagramPacket(buf, buf.length);
       		socket.receive(in);
       		Log.d("Testing", "Received");
       		return true;
       	}
       	catch(Exception e){return false;}
   }
   
   private void surveyConnection(){
   	int count = 0;
   	while(connected){
   		try{Thread.sleep(1000);}
       	catch(Exception e){}
       	
   		if(!testConnection())
   			count++;
   		else
   			count = 0;
   		
   		if(count == 3){
   			closeSocket();
   			return;
   		}
   	}
   }
}
