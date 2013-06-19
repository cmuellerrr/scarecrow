package novo.hmd.scarecrow;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class MainApp extends Application{
	
	public boolean connected = false;
	
	/** Control Panel parameters **/
    public boolean sys_freeze = false;

	public boolean B_Mode = true;
	public boolean C_Mode = false;
	public boolean D_Mode = false;
	public boolean M_Mode = false;
	
	public String Frequency = "2.0";
	public String LineDensity = "Low";
	public double Contrast = 0.8;
	
	public int bp_select = 0;
	public int Gain = 20;
	public int Angle = 90;
		
	
	private DataUpdateReceiver dataUpdateReceiver; // receives broadcast messages from ConnectionService
	private static String TAG = "ControlPanelApp";
/*  
  	@Override
    public Class<?> getHomeActivityClass() {
        return ControlPanelActivity.class;
    }
 
    @Override
    public Intent getMainApplicationIntent() {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_url)));
    }
*/
	
	@Override
	public void onCreate(){
		super.onCreate();
		// sets up data update receiver on creation
		if (dataUpdateReceiver == null) 
        	dataUpdateReceiver = new DataUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter("connection");
        registerReceiver(dataUpdateReceiver, intentFilter);
	}
 
    // receives broadcast messages from ConnectionService
    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	Log.v(TAG, "onReceive");
            if (intent.getAction().equals("connection")) {
            	Bundle b = intent.getExtras();
            	String msg = b.getString("msg");
	            if (msg.equals("connected")) {
	            	 connected = true;
	            }
	            else if (msg.equals("disconnected")) {
	                 connected = false;
	            }else if (msg.equals("B_Mode")){
	            	B();
	            }
            }
            
        }
    }
    
    //called when B mode button is pressed on android device (or ultrasound system, but this is not yet implemented on ultrasound system)
    public void B(){
	   	 C_Mode = false;
	   	 D_Mode = false;
	   	 M_Mode = false;
    }
    
    //called when C mode button is pressed on android device (or ultrasound system, but this is not yet implemented on ultrasound system)
    public void C(){
    	C_Mode = !C_Mode;
    }
    //called when D mode button is pressed on android device (or ultrasound system, but this is not yet implemented on ultrasound system)
    public void D(){
    	D_Mode = !D_Mode;
    }
}
