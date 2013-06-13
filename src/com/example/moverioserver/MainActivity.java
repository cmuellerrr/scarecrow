package com.example.moverioserver;


import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private DataUpdateReceiver dataUpdateReceiver;
	private static final String TAG = "MainActivity_Server";	// used for logging purposes
	
	private MainApp MainApp;

	//IP address data storage
	public static final String PREFS_NAME = "moverioPrefs";
	SharedPreferences prefs;
	public String ip_address;
	
	
	/**To prevent "spamming" of control panel commands: **/
	public long lastActionTime = 0; // the time of the last control panel command sent by the android device.
	private static int MIN_ACTION_TIME = 250; // the minimum amount of time (ms) that must pass before the android device can send another command

	
	/** ConnectionService **/
	private ConnectionService mBoundService;
	private boolean mIsBound;
	
	/**Checks external storage**/
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	
	//image view for prototyping
	ImageView imageView;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.control_panel);
        Log.v(TAG, "onCreate");
        
        // to view variables stored in MainApp
        MainApp = (MainApp) MainActivity.this.getApplication();   

        // to begin ConnectionService (connection to ultrasound system)
       	if (!isMyServiceRunning())
       		startService(new Intent(MainActivity.this,ConnectionService.class));
       	doBindService();

		
       	// stores data on android tablet (in this case, we are storing the IP address of ultrasound system for future use)
       	prefs = getSharedPreferences(PREFS_NAME, 0);
        ip_address = prefs.getString("machine_ip","none");

        imageView = (ImageView) findViewById(R.id.imageView);
        checkExternalStorage();
        
        
	}
	
    // The activity is about to become visible.
    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");   
    }
	
    // The activity has become visible (it is now "resumed").     
    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        // sets up data update receiver for receiving broadcast messages from ConnectionService
        if (dataUpdateReceiver == null) 
        	dataUpdateReceiver = new DataUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter("connection");
        registerReceiver(dataUpdateReceiver, intentFilter);
        
        // Changes color of top right connection icon depending on whether a connection has been established. Can't do it onCreate because service
        // is not necessarily bound yet
        if (MainApp.connected){
        	//getActionBar().removeItem(CONNECTION_POPUP);
   	       // addActionBarItem(getActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(new ActionBarDrawable(this, R.drawable.ab_connection, Color.parseColor("#d8f7cf"), Color.parseColor("#000000"))), CONNECTION_POPUP);
        }else {
        	//getActionBar().removeItem(CONNECTION_POPUP);
        	//addActionBarItem(getActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(new ActionBarDrawable(this, R.drawable.ab_connection, Color.parseColor("#ffc5c5"), Color.parseColor("#000000"))), CONNECTION_POPUP);
        }
        
       // init();
       // touchBlocker.setVisibility(View.GONE); // touch blocker stops blocking touches when focus is on this activity
       // mView.initVariables(this); // initializes tool bars and pointers to the tool bars

    }
    
    @Override
    protected void onPause(){
    	super.onPause();
    	Log.v(TAG, "onPause");
    	if (dataUpdateReceiver != null) 
    		unregisterReceiver(dataUpdateReceiver);
    	//touchBlocker.setVisibility(View.VISIBLE); // touch blocker blocks touches when not within this activity (used for ConnectionPopUp activity)
    	//mView.unloadLeftToolBar(); // because mView always loads left tool bar at start
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop");
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        doUnbindService();
        // The activity is about to be destroyed.
    }
    
    // Pressing the back button on the android device will perform this function
    /*@Override
    public void onBackPressed() {
    	return;
    }*/
    
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    }
     
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    }


    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_main, menu);
	   	 Log.v("TAG", "menu create");

		menu.add("Enable Server");		
		return true;
	}
	
	public boolean onOptionsItemSelected (MenuItem item){
		if (item.getTitle().equals("Enable Server")){
			mBoundService.startServer();
		}
		
		return false;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
	   	 Log.v("TAG", "menu prepare");

     super.onPrepareOptionsMenu(menu);
	
   	 return true;
	}
	
    // declares service and connects
    private ServiceConnection mConnection = new ServiceConnection() {
    	public void onServiceConnected(ComponentName className, IBinder service) {
        	 Log.v("TAG", "set mBoundService");
            mBoundService = ((ConnectionService.LocalBinder)service).getService();
           	if (!mBoundService.isConnected()){
           		//mBoundService.connect(ip_address);
           		//mBoundService.startServer();

           		
           	}
        }
        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };

    // Binds the service to the activity 
    // Allows access service functions/variables available to binded activities.
    	// See LocalBinder class in ConnectionService
    private void doBindService() {
    	Log.v(TAG, "bind service");
        bindService(new Intent(MainActivity.this, ConnectionService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }


    // unbinds the service and activity
    private void doUnbindService() {
        if (mIsBound) {
        	Log.v(TAG, "unbind service");
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
    
    // returns whether or not the service is running
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ConnectionService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
	
	// Listens to broadcast messages
    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	Log.v(TAG, "on receive");
            if (intent.getAction().equals("connection")) {
            	Bundle b = intent.getExtras();
            	String msg = b.getString("msg");
            	
            	//Log.v(TAG, msg);

            	//Resources r = getResources();
            	Drawable[] layers = new Drawable[2];
            	
            	try {
					JSONObject json = new JSONObject(msg);
					String background = json.getString("background");
					String foreground = json.getString("foreground");	
					String background_imagePath = Environment.getExternalStorageDirectory().toString() + "/DCIM/" + background;
					String foreground_imagePath = Environment.getExternalStorageDirectory().toString() + "/DCIM/" + foreground;
					
					if (foreground.equals("") && background.equals("")){
						layers[0] = Drawable.createFromPath(background_imagePath);
						imageView.setImageDrawable(null);
					}else if (foreground.equals(""))
						layers[1] = Drawable.createFromPath(background_imagePath);
					else
						layers[1] = Drawable.createFromPath(foreground_imagePath);
            		
					
					if (layers[1] != null && layers[0] != null){
						LayerDrawable layerDrawable = new LayerDrawable(layers);
	        	   		imageView.setImageDrawable(layerDrawable);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
            	if (msg.equals("1")){
            		//layers[0] = r.getDrawable(R.drawable.t);
            		//layers[1] = r.getDrawable(R.drawable.tt);
            		//layers[0] = r.getDrawable(R.drawable.ic_launcher);
            		//LayerDrawable layerDrawable = new LayerDrawable(layers);
            		//imageView.setImageDrawable(layerDrawable);
            	   	if (mExternalStorageAvailable){
            	   		String imagePath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Test_Screen_1 - Start.png";
            	   		
            	   		imageView.setImageDrawable(Drawable.createFromPath(imagePath));
            	   	}
            		
            		//imageView.setBackgroundColor(Color.RED);
            	}else if (msg.equals("2")){
            	   	if (mExternalStorageAvailable){
            	   		String imagePath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Test_Screen_2 - Start.png";
            	   		
            	   		imageView.setImageDrawable(Drawable.createFromPath(imagePath));
            	   	}
            		//imageView.setBackgroundColor(Color.BLUE);

            	
            	}else if (msg.equals("")){
            		
            	}else if (msg.equals("")){
            		
            	}
            	
        /*    	if (msg.equals("connected")){
                // if ConnectionService confirms that the connection to the ultrasound system, the connection icon on the top right turns green
	            	runOnUiThread(new Runnable() {
	            	      public void run() { 
	            	    	  Log.v(TAG, "on receive - connected");
	            	    	  //getActionBar().removeItem(CONNECTION_POPUP);
	            	    	  //addActionBarItem(getActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(new ActionBarDrawable(ControlPanelActivity.this, R.drawable.ab_connection, Color.parseColor("#d8f7cf"), Color.parseColor("#000000"))), CONNECTION_POPUP);
	            	      }
	            	    });
	            }else if (msg.equals("disconnected")) {
		        // if ConnectionService says the ultrasound system has been disconnected, the connection icon on the top right turns red
	                 runOnUiThread(new Runnable(){
	                	  public void run() { 
	                		   Log.v(TAG, "on receive - disconnected");
	                		  // getActionBar().removeItem(CONNECTION_POPUP);
	                	      // addActionBarItem(getActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(new ActionBarDrawable(ControlPanelActivity.this, R.drawable.ab_connection, Color.parseColor("#ffc5c5"), Color.parseColor("#000000"))), CONNECTION_POPUP);
	                	   }
	                	 });
	            }else if (msg.equals("B_Mode")) {
		        // if ConnectionService says the B-Mode button has been pressed, call B() function:
	                runOnUiThread(new Runnable() {
	                	  public void run() { 
	                		   Log.v(TAG, "on receive - B_Mode");
	                		   //B();
	                	   }
	                	 });
	            }else if (msg.equals("stream_comp")){
		        // if ConnectionService tells ControlPanelActivity that the stream for the current image frame is complete,
		        // call the drawFrame() function
	            	//drawFrame();
	            }
            	*/
            	
          }
          
        }
    }
    
    
    
    public void Connect_Function(View view){
        //startActivity(new Intent(this, ConnectionPopUp.class));	
    	Log.v("TAG", "ConnectButton");
    	mBoundService.startServer();
    }

    

    private void checkExternalStorage(){
    	String state = Environment.getExternalStorageState();
    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    	    // We can read and write the media
    	    mExternalStorageAvailable = mExternalStorageWriteable = true;
    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
    	    // We can only read the media
    	    mExternalStorageAvailable = true;
    	    mExternalStorageWriteable = false;
    	} else {
    	    // Something else is wrong. It may be one of many other states, but all we need
    	    //  to know is we can neither read nor write
    	    mExternalStorageAvailable = mExternalStorageWriteable = false;
    	}
    }
    

}
