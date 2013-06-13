package com.example.moverioserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import org.apache.http.util.ByteArrayBuffer;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class ConnectionService extends Service {
	
	// socket
	Socket socket;
	public String ip;
	private static int port = 5555;
	private boolean run = false; 		// is connected or not
	
	private ServerSocket server = null;
	private Thread       receiveThread = null;
	private DataInputStream  streamIn  =  null;
	
	
	private Runnable receiveRunnable = null;

	
	// binder for activities to access functions
	private final IBinder myBinder = new LocalBinder();
	
	private static String TAG = "ConnectionService";

	@Override
	public IBinder onBind(Intent arg0) {
		return myBinder;
	}
	
	// these functions can be accessed by activities when the service is bound
    public class LocalBinder extends Binder {
        public ConnectionService getService() {
            return ConnectionService.this;
        }
       /* public void sendMsg(String msg){
        	ConnectionService.this.sendMsg(msg);
        }*/
        public void startServer(){
        	ConnectionService.this.startServer();
        
        }
        public void stop(){
        	ConnectionService.this.stop();
        }
 /*       public void connect(String ip_address){
        	ConnectionService.this.connect(ip_address);
        }*/
        public boolean isConnected(){
        	return ConnectionService.this.isConnected();
        }
    }
    
    
    public void startServer(){

        Log.v(TAG, "startServer");

    	
    	try {
    		run = false;
	    	if (socket!= null) socket.close();
	    	if (streamIn != null) streamIn.close();
	    	if (server != null) server.close();
	    	//if (receiveThread != null) receiveThread;

	    } catch (IOException e) {
			e.printStackTrace();
	    }
    	
        try {
        	//InetAddress addr = InetAddress.getByName("192.168.1.555");
			server = new ServerSocket(5555);
			//server.bind(new InetSocketAddress(addr, port));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        
    	//ip=ip_address;
    	socket = new Socket();
    	receiveRunnable = new receiveSocket();
    	receiveThread = new Thread(receiveRunnable);
    	receiveThread.start();

    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");
        
        try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  

        socket = new Socket();
    }

    @Override
    public void onStart(Intent intent, int startId){
        super.onStart(intent, startId);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        stop();
        socket = null;
    }

	public boolean isConnected() {
		return run;
	}
	
	class receiveSocket implements Runnable {

		@Override
		public void run() {
			try
	         {  System.out.println("Waiting for a client ..."); 
	         	sendBroadcastMsg("Incoming");
	            socket = server.accept();
	            System.out.println("Client accepted: " + socket);
	            open();
	            sendBroadcastMsg("Accepted");
	            run = true;
	            boolean done = false;
	            while (!done)
	            {  try
	               { 
	            	  String line = streamIn.readLine();
	            	  if (line!=null){ 
	            		  Log.v("SERVER",line);
	                  	  sendBroadcastMsg(line);
	                      done = line.equals(".bye");
	            	  }
	               }
	               catch(IOException ioe)
	               {  done = true;  }
	            }
	            close();
	         }
	         catch(IOException ie)
	         {  System.out.println("Acceptance Error: " + ie);  }
	      }
			
		/*
		public void start()
		{  if (thread == null)
		   {  thread = new Thread(this); 
		      thread.start();
		   }
		}
		public void stop()
		{  if (thread != null)
		   {  thread.stop(); 
		      thread = null;
		   }
		}*/
		
	}

	public void open() throws IOException
	{  streamIn = new DataInputStream(new 
	                     BufferedInputStream(socket.getInputStream()));
	}
	public void close() throws IOException
	{  run = false;
		if (socket != null)    socket.close();
	   if (streamIn != null)  streamIn.close();
	   
	}
	
/*
    // Opens a socket and attempts to connect to the input ip address
    	// called by activities (ConnectionPopUp & ControlPanelActivity) when this service is bound
    public void connect(String ip_address){
    	run = false;
    	sendBroadcastMsg("disconnected");
	    try {
	    	if (socket!= null) socket.close();
	    	if (streamOut != null) streamOut.close();
	    	if (streamIn != null) streamIn.close();
	    } catch (IOException e) {
			e.printStackTrace();
	    }
    	ip=ip_address;
    	socket = new Socket();
    	connectRunnable = new connectSocket();
    	sendThread = new Thread(connectRunnable);
    	sendThread.start();
    	
    }*/
    /*
    // thread sets up socket connection
    class connectSocket implements Runnable {
        @Override
        public void run() {
        	Log.v(TAG, "connectSocket_run");
            SocketAddress socketAddress = new InetSocketAddress(ip, port );
            try {               
                socket.connect(socketAddress, 100);
                streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                recRunnable = new receiveSocket();
                recThread = new Thread(recRunnable);
                recThread.start();
            } catch (IOException e) {
            	//stop();
                e.printStackTrace();
            }
        }    
    }
    
    // sends bytes to output stream
    public void sendMsg(String msg){
    	if (run){ // if system is connected
		  	try {
				streamOut.writeBytes(msg+"@");
				streamOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}else{
    		// if system is not connected, make connection pop up show up
    		 Intent dialogIntent = new Intent(getBaseContext(), ConnectionPopUp.class);
			 dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 getApplication().startActivity(dialogIntent);
    	}
    }    
    
    // thread receives messages
    class receiveSocket implements Runnable {
		private long beginTime;

		@Override
		public synchronized void run() {
			try
		      {  Log.v(TAG, "receive socket ");
				 streamIn  = new DataInputStream(socket.getInputStream()); // sets up input stream
				 run = true; // sets connection flag to true
			     sendBroadcastMsg("connected"); // lets the other activities know that a successful connection has been made
			     sendMsg("GETIMAGE"); // tells the ultrasound system to send the first image frame
			 
		      }
		      catch(IOException ioe)
		      {  Log.v(TAG, "Error getting input stream: " + ioe);
		         
		      }
			// when connected, this thread will stay in this while loop
			while (run )
		      {  try
		         {  beginTime = System.currentTimeMillis();
	            	long timeElapsed = System.currentTimeMillis() - beginTime;
	            	
	            	int bytesRead = streamIn.read(buffer); // reads bytes from the input stream

		         	Log.v("bytesRead",""+bytesRead);
		         	Log.v("size",""+size);
		         	
			        if (bytesRead > 0){ 
			         	if (streaming){ // if already in the middle of streaming a frame
			         		handleStreamIteration(bytesRead); // adds these new bytes to existing image buffer
			         	}else{ 
			         		handleStreamStart(bytesRead); // start a new frame, restarting image buffer
			         	}
			        }


			        if (bytesRead <0 && getImage==false)
			        	sendMsg("GETIMAGE");
		         	
		         }
		         catch(IOException ioe)
		         {  Log.v(TAG, "Listening error: " + ioe.getMessage());
		            
		         } 
		      }
		}
    }
    
    // This function is called at the beginning of every image frame.
    private synchronized void handleStreamStart(int b){
    	streaming = true; // indicates that we are now processing an image frame
    	img_buffer_offset=0; // we are now starting at the beginning of an image, so the image buffer is empty and the offset should be 0
    	
    	// Reads the 20-byte header to get width and height data.
    		// This if statement checks if the first 20-bytes fit a particular structure that indicates
    		// that it is the 20-byte header sent at the beginning of every image frame.
    	if((buffer[3]==64 || buffer[4]==64) && 
            (buffer[7] == -1 && buffer[8] == -1) || (buffer[8] == -1 && buffer[9] == -1) && 
    		(buffer[9] == 0 && buffer[10] == 0 && buffer[11] == 0 &&
    		buffer[12] == 0 && buffer[13] == 0 && buffer[14] == 0 && buffer[15] == 0 &&
   			buffer[16] == 0 && buffer[17] == 0 && buffer[18] == 0 && buffer[19] == 0)){
    			if (buffer[3]==64){ // byte 64 represents the '&' symbol that separates width and height
    				width = Character.getNumericValue(buffer[2]) + 10*Character.getNumericValue(buffer[1]) + 100*Character.getNumericValue(buffer[0]);
    				height = Character.getNumericValue(buffer[6]) + 10*Character.getNumericValue(buffer[5]) + 100*Character.getNumericValue(buffer[4]);
    			}else if (buffer[4]==64){
    				width = Character.getNumericValue(buffer[3]) + 10*Character.getNumericValue(buffer[2]) + 100*Character.getNumericValue(buffer[1]) + 1000*Character.getNumericValue(buffer[0]);
    				height = Character.getNumericValue(buffer[7]) + 10*Character.getNumericValue(buffer[6]) + 100*Character.getNumericValue(buffer[5]);
    			}
    		
    		//Log.v("buffer",""+i+","+Character.getNumericValue(buffer[i]));
    		//tag[i]=buffer[i];
    	}else{
    		// if the first 20 bytes do not fit the structure of the 20-byte header, then something is wrong and we should stop reading for now
    		streaming = false;
    		getImage = false;
    		return;
    	}
    	
    	
    	//Log.v("width,height",""+width+","+height);
    	size = width*height; // The total number of bytes we need to read before we are done with this image frame.
    						 // Size is decremented whenever bytes are read. When size reaches 0, we will draw the image
    						 // frame and request a new frame.
    	
    	if (size < 0){ // if size is negative, something is wrong and we stop reading bytes for now.
    		streaming = false;
    		getImage=false;
    		return;
    	}
    	
    	img_buffer = new int[size];
    	draw_buffer = new int[size];

    	// If there are more than 20 bytes to process, that means the rest of the bytes represent pixels on the image.
    	// We need to put these pixels into our image buffer.
    	if (b>20){
    		for (int i = 20; i < b;i++){
    			img_buffer[i-20] = buffer[i];
    			img_buffer_offset++;
    		}
    		size-=(b-20);
    	}
    }
    
    // This function is called when handling stream information when in the middle of an image frame.
    // This function is used after handleStreamStart has been called.
    private synchronized void handleStreamIteration(int b){   	
    	// checks the first 20 bytes of the stream if it fits the structure of the initial 20-byte header that signifies a new frame
    	if((buffer[3]==64 || buffer[4]==64) && 
        	(buffer[7] == -1 && buffer[8] == -1) || (buffer[8] == -1 && buffer[9] == -1) && 
        	(buffer[9] == 0 && buffer[10] == 0 && buffer[11] == 0 &&
        	buffer[12] == 0 && buffer[13] == 0 && buffer[14] == 0 && buffer[15] == 0 &&
       		buffer[16] == 0 && buffer[17] == 0 && buffer[18] == 0 && buffer[19] == 0)){
    		// if matches 20-byte header, then call handleStreamStart
    		handleStreamStart(b);
        	return;
        }
    	
    	// places bytes into image buffer
		for (int i = 0; i < b;i++){
			img_buffer[img_buffer_offset] =  Color.argb(buffer[i], 255, 255, 255);
			img_buffer_offset++;
			if (img_buffer_offset>img_buffer.length-1 && i != b-1){ 
				// if the image buffer offset is greater than the size of the image buffer, something is wrong so we stop reading for now
				 Log.v(TAG, "img buffer offset error");
				streaming = false;
				return;
			}

		}
				
		size-=b; // decrements the size counter by the number of bytes just read
		
 		if (size==0){ // if size is 0, that means we have read all the bytes for the current frame
 			handleStreamComplete();
 		}else if (size < 0){ // if size is negative, this means something is wrong so we stop reading for now
 			streaming = false;
 			getImage = false;
 			return;
 		}
     }
    
    // Called when the stream for a particular frame is complete
    private synchronized void handleStreamComplete(){
    	streaming = false;
     	Log.v("connection_service","frame: "+frames);

    	frames++;
    	
		System.arraycopy(img_buffer, 0, draw_buffer, 0, img_buffer.length); // places our image buffer in a draw buffer to be drawn 
		sendBroadcastMsg("stream_comp"); // tells ControlPanelActivity to update MeasureView's current frame
		sendMsg("GETIMAGE"); // tells the ultrasound system to send a new frame
    }
    
    // Not Used Or Tested
    // Adds all the unread bytes within the initBuffer array into to the beginning of the newBuffer array
    private synchronized byte[] moveBytes(byte[] initBuffer, byte[] newBuffer, int initBuffer_bytesUnread, int initBuffer_bytesTotal, int newBufferSize){
    	byte returnBuffer[] = new byte[newBufferSize + initBuffer_bytesUnread];
    	for (int i = initBuffer_bytesTotal-initBuffer_bytesUnread; i<initBuffer_bytesTotal;i++){
    		returnBuffer[i-(initBuffer_bytesTotal-initBuffer_bytesUnread)]=initBuffer[i];
    	}
    	for (int i = 0; i < newBufferSize; i++){
    		returnBuffer[i+initBuffer_bytesUnread]=newBuffer[i];
    	}
		return newBuffer;
    }
    */
    // Forces the socket to disconnect
    public void stop(){
    	run = false;
    	//sendBroadcastMsg("disconnected");

	    try {
	    	if (socket!= null) socket.close();
	    	//if (streamOut != null) streamOut.close();
	    	if (streamIn != null) streamIn.close();
	    } catch (IOException e) {
			e.printStackTrace();
	    }
	    Intent dialogIntent = new Intent(getBaseContext(), ConnectionPopUp.class);
	    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    getApplication().startActivity(dialogIntent);
	    //startActivity(new Intent(this, ConnectionPopUp.class));
    }    
    
	//sends a broadcast message to be read by other classes
	private void sendBroadcastMsg(String msg){
        Intent intent = new Intent("connection");
        intent.putExtra("msg", msg);
        sendBroadcast(intent);
	}
}