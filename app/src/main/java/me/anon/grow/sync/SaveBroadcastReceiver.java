package me.anon.grow.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import me.anon.grow.helper.EncryptionHelper;

import static android.util.Base64.NO_WRAP;

public class SaveBroadcastReceiver extends BroadcastReceiver
{
	private boolean isEncrypted = false;
	private boolean shouldBeEncrypted = false;
	private boolean wifiOnly = false;
	private String encryptionKey = "";
	private String serverIp = "";
	private String serverPort = "";

	@Override public void onReceive(Context context, Intent intent)
	{
		if (intent.getExtras() == null) return;

		populateSettings(context);

		if (intent.getExtras().containsKey("me.anon.grow.PLANT_LIST"))
		{
			String plantData = intent.getExtras().getString("me.anon.grow.PLANT_LIST", "");
			plantData = shouldBeEncrypted ? Base64.encodeToString(EncryptionHelper.encrypt(encryptionKey, plantData), NO_WRAP) : plantData;
			postPlantData(plantData);
		}
		else if (intent.getExtras().containsKey("me.anon.grow.IMAGE_ADDED"))
		{
			postImage(intent.getExtras().getString("me.anon.grow.IMAGE_ADDED"), false);
		}
		else if (intent.getExtras().containsKey("me.anon.grow.IMAGE_DELETED"))
		{
			postImage(intent.getExtras().getString("me.anon.grow.IMAGE_DELETED"), true);
		}
	}

	/**
	 * Posts image to the API
	 * @param path The path of the image to send
	 * @param delete To delete or not
	 */
	private void postImage(String path, boolean delete)
	{

	}

	/**
	 * Posts data to the API for `/plants` endpoint
	 * @param data
	 */
	private void postPlantData(String data)
	{

	}

	/**
	 * Populates local setting variables for convenience
	 * @param context
	 */
	private void populateSettings(Context context)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		shouldBeEncrypted = prefs.getBoolean("send_encrypted", false);
		wifiOnly = prefs.getBoolean("wifi_only", false);
		encryptionKey = prefs.getString("encryption_key", "");
		serverIp = prefs.getString("server_ip", "");
		serverPort = prefs.getString("server_port", "");
	}
}
