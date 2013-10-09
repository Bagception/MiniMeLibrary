package de.philipphock.android.lib.services;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class ServiceUtil {
	public static final boolean isServiceRunning(Context c,Class<?> s){
		
	    ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (s.getCanonicalName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}
