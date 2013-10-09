package de.philipphock.android.lib.activities;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class SensorActivity extends Activity implements SensorEventListener {

	private final SensorManager sensorManager;
    protected final Sensor sensor;

	public SensorActivity() {
		sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

	}
	
	@Override
	protected void onPause() {
        sensorManager.unregisterListener(this);
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
		super.onResume();
	}
	
	public void initSensor(Context context){
		
	}
	
	public static void detachSensor(Context context){
		
	}


}


