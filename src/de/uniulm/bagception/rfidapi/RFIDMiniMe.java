package de.uniulm.bagception.rfidapi;

import java.util.ArrayList;
import java.util.Collections;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;
import de.uniulm.bagception.rfidapi.CMD_PwrMgt.PowerState;

public class RFIDMiniMe {
	// TODO usbstatelistener
	private static final UsbCommunication mUsbCommunication = UsbCommunication
			.getInstance();

	public enum UsbState {
		CONNECTED, DISCONNECTED
	}; // TODO

	private static UsbState usbstate = UsbState.CONNECTED;

	private static MtiCmd mMtiCmd;
	
	public static final String BROADCAST_RFID_TAG_FOUND = "de.uniulm.bagception.rfid.broadcast.tagfound";
	
	public static synchronized void triggerInventory(final Context c) {

		
		final ArrayList<String> tagList = new ArrayList<String>();
		final int scantimes = 25;

		if (usbstate == UsbState.CONNECTED) {
			//final ProgressDialog mProgDlg = ProgressDialog.show(c, "Inventory",
				//	"Searching ...", true);

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
							//TODO broadcast intent
						} else {
							// #### process error ####
						}
					}
					//mProgDlg.dismiss();
					sleepMode();
				}

				
			}.start();
		}
	}

	private static void sendBroadcastTagFound(Context c,String tagId){
		Intent intent = new Intent();
		intent.setAction(BROADCAST_RFID_TAG_FOUND);
		intent.putExtra(BROADCAST_RFID_TAG_FOUND, tagId);
		c.sendBroadcast(intent);
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
