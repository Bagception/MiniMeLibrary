package de.uniulm.bagception.rfidapi.connection;

public interface USBConnectionServiceCallback {
	public void onUSBConnected(boolean connected);
	public void onUSBConnectionError(Exception e);
}
