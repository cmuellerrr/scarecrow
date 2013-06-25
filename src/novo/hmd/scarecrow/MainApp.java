package novo.hmd.scarecrow;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class MainApp extends Application{
	
	private DataUpdateReceiver dataUpdateReceiver; // receives broadcast messages from ConnectionService
	private static String TAG = "MainApp"; // string used for Logs

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
	            }
	            else if (msg.equals("disconnected")) {
	               
	            }
            }
            
        }
    }
    
}
