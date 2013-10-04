package de.uniulm.bagception.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.uniulm.bagception.rfidapi.UsbCommunication;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import de.uniulm.bagception.R;

import de.uniulm.bagception.rfidapi.RFIDMiniMe;

public class MainActivity extends Activity {

	private TextView tv_usbstate;
	private static final String ACTION_USB_PERMISSION = "com.mti.rfid.minime.USB_PERMISSION";
	private UsbCommunication mUsbCommunication = UsbCommunication.newInstance();
	private UsbManager mManager;
	private PendingIntent mPermissionIntent;
	private static final boolean DEBUG = true;
	private boolean bSavedInst = false;
	private SharedPreferences mSharedpref;
	private Fragment objFragment;
	private static final int PID = 49193;
	private static final int VID = 4901;
	private ArrayAdapter<String> adapter;
	private final ArrayList<String> ids = new ArrayList<String>();

	public void inventoryClicked(View v) {
		RFIDMiniMe.triggerInventory(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv_usbstate = (TextView) findViewById(R.id.tv_usbstate);
		mManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);

		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED); // will
																	// intercept
																	// by system
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		filter.addAction(ACTION_USB_PERMISSION);
		registerReceiver(usbReceiver, filter);

		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ids);
		ListView view = (ListView) findViewById(R.id.list);
		view.setAdapter(adapter);
		IntentFilter f = new IntentFilter();
		f.addAction(RFIDMiniMe.BROADCAST_RFID_TAG_FOUND);
		registerReceiver(rfidTagReceiver, f);

		setUsbState(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void setUsbState(boolean state) {
		if (state) {
			tv_usbstate.setText("Connected");
			tv_usbstate.setTextColor(android.graphics.Color.GREEN);
		} else {
			tv_usbstate.setText("Disconnected");
			tv_usbstate.setTextColor(android.graphics.Color.RED);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (tv_usbstate.getText().equals("Connected"))
			mUsbCommunication.setUsbInterface(null, null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(rfidTagReceiver);
		unregisterReceiver(usbReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		while (deviceIterator.hasNext()) {
			UsbDevice device = deviceIterator.next();
			if (device.getProductId() == PID && device.getVendorId() == VID) {
				Log.d("USB", "MiniMe reader found");
				if (!mManager.hasPermission(device))
					mManager.requestPermission(device, mPermissionIntent);
				else
					Log.d("USB", "PERMISSION");
				break;
			}
		}
	}

	BroadcastReceiver usbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("USB", "1");
			String action = intent.getAction();
			if (DEBUG)
				Toast.makeText(context, "Broadcast Receiver",
						Toast.LENGTH_SHORT).show();

			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) { // will
				Log.d("USB", "2"); // intercept
				// by
				// system
				if (DEBUG)
					Toast.makeText(context, "USB Attached", Toast.LENGTH_SHORT)
							.show();
				UsbDevice device = intent
						.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				mUsbCommunication.setUsbInterface(mManager, device);
				setUsbState(true);

			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				Log.d("USB", "3");
				if (DEBUG)
					Toast.makeText(context, "USB Detached", Toast.LENGTH_SHORT)
							.show();
				mUsbCommunication.setUsbInterface(null, null);
				setUsbState(false);
				// getReaderSn(false);

			} else if (ACTION_USB_PERMISSION.equals(action)) {
				Log.d("USB", "4");
				if (DEBUG)
					Toast.makeText(context, "USB Permission",
							Toast.LENGTH_SHORT).show();
				Log.d(UsbCommunication.TAG, "permission");
				synchronized (this) {
					UsbDevice device = intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						mUsbCommunication.setUsbInterface(mManager, device);
						setUsbState(true);

						RFIDMiniMe.setPowerLevelTo18();
						RFIDMiniMe.sleepMode();
					} else {
						finish();
					}
				}
			}
		}
	};

	BroadcastReceiver rfidTagReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String id = intent
					.getStringExtra(RFIDMiniMe.BROADCAST_RFID_TAG_FOUND);
			Log.d("RFID", "RECV: " + id);
			adapter.add(id);
			adapter.notifyDataSetChanged();
		}

	};

}
