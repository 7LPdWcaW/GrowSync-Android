package me.anon.grow.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SaveBroadcastReceiver extends BroadcastReceiver
{
	@Override public void onReceive(Context context, Intent intent)
	{
		Log.e("test", "Received save request");
		Log.e("test", intent.getExtras().getString("PLANT_LIST", ""));
	}
}
