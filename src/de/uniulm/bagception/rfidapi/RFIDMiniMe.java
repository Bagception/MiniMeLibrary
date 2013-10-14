package de.uniulm.bagception.rfidapi;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.util.Log;
import de.uniulm.bagception.rfidapi.CMD_PwrMgt.PowerState;
import de.uniulm.bagception.rfidapi.connection.USBConnectionServiceCallback;
import de.uniulm.bagception.rfidapi.connection.USBConnectionServiceHelper;

public class RFIDMiniMe  {
	private static final UsbCommunication mUsbCommunication = UsbCommunication
			.getInstance();


	private static MtiCmd mMtiCmd;
	
	public static final String BROADCAST_RFID_TAG_FOUND = "de.uniulm.bagception.rfid.broadcast.tagfound";
	public static final String BROADCAST_RFID_FINISHED = "de.uniulm.bagception.rfid.broadcast.endinventory";
	private static final Handler broadCastHandler = new Handler();
	
	public static void triggerInventory(final Context c){
		USBConnectionServiceHelper connHelper = new USBConnectionServiceHelper(c,new USBConnectionServiceCallback() {
			
			@Override
			public void onUSBConnectionError(Exception e) {
				e.printStackTrace();
				
			}
			
			@Override
			public void onUSBConnected(boolean connected) {
				if (connected){
					initInventory(c);					
				}else{
					Log.d("USB","USB not connected");
				}
				
				
			}
		});
		connHelper.checkUSBConnection();
	}
	
	private static synchronized void initInventory(final Context c) {

		
		final ArrayList<String> tagList = new ArrayList<String>();
		final int scantimes = 5;


			new Thread() {
				int numTags;
				String tagId;

				ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC,
						100);

				public void run() {

					tagList.clear();
					for (int i = 0; i < scantimes; i++) {
						mMtiCmd = new CMD_Iso18k6cTagAccess.RFID_18K6CTagInventory(
								mUsbCommunication);
						CMD_Iso18k6cTagAccess.RFID_18K6CTagInventory finalCmd = (CMD_Iso18k6cTagAccess.RFID_18K6CTagInventory) mMtiCmd;
						if (finalCmd.setCmd(CMD_Iso18k6cTagAccess.Action.StartInventory)) {
							tagId = finalCmd.getTagId();
							if (finalCmd.getTagNumber() > 0) {
								tg.startTone(ToneGenerator.TONE_PROP_BEEP);
								if (!tagList.contains(tagId))
									tagList.add(tagId);
								// finalCmd.setCmd(CMD_Iso18k6cTagAccess.Action.GetAllTags);
							}

							for (numTags = finalCmd.getTagNumber(); numTags > 1; numTags--) {
								if (finalCmd
										.setCmd(CMD_Iso18k6cTagAccess.Action.NextTag)) {
									tagId = finalCmd.getTagId();
									if (!tagList.contains(tagId)) {
										tagList.add(tagId);
									}
								}
							}
							Collections.sort(tagList);
							sendBroadcastTagFound(c,tagId);
						} else {
							// #### process error ####
						}
					}
					//mProgDlg.dismiss();
					Intent intent = new Intent();
					intent.setAction(BROADCAST_RFID_FINISHED);
					c.sendBroadcast(intent);				
					
					sleepMode();
					
				}

				
			}.start();
		
	}

	private static void sendBroadcastTagFound(final Context c,final String tagId){
		broadCastHandler.post(new Runnable() {
			
			@Override
			public void run() {
				Intent intent = new Intent();
				intent.setAction(BROADCAST_RFID_TAG_FOUND);
				intent.putExtra(BROADCAST_RFID_TAG_FOUND, tagId);
				c.sendBroadcast(intent);				
			}
		});

	}
	
	
	
	public static void setPowerLevelTo18() {
		MtiCmd mMtiCmd = new CMD_AntPortOp.RFID_AntennaPortSetPowerLevel(mUsbCommunication);
		CMD_AntPortOp.RFID_AntennaPortSetPowerLevel finalCmd = (CMD_AntPortOp.RFID_AntennaPortSetPowerLevel) mMtiCmd;
		
		finalCmd.setCmd((byte)18);
	}
	
	public static void sleepMode() {
		MtiCmd mMtiCmd = new CMD_PwrMgt.RFID_PowerEnterPowerState(
				mUsbCommunication);
		CMD_PwrMgt.RFID_PowerEnterPowerState finalCmd = (CMD_PwrMgt.RFID_PowerEnterPowerState) mMtiCmd;
		finalCmd.setCmd(PowerState.Sleep);
	}
	
	
	
	
	
}
