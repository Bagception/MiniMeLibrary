package de.philipphock.android.lib;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public abstract class BroadcastActor<R extends Reactor> extends BroadcastReceiver{
	protected final R reactor;
	public BroadcastActor(R reactor) {
		this.reactor = reactor;
	}
	
	public abstract void register(Activity a);
	public abstract void onReceive(Context context, Intent intent);

	public void unregister(Activity a){
		a.unregisterReceiver(this);
	}
}
