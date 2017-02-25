package me.anon.grow.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import me.anon.helper.EncryptionHelper;
import me.anon.stream.EncryptInputStream;

public class SaveBroadcastReceiver extends BroadcastReceiver
{
	private boolean isEncrypted = false;
	private boolean shouldBeEncrypted = false;
	private boolean wifiOnly = false;
	private boolean sendImages = true;
	private String encryptionKey = "";
	private String serverIp = "";
	private String serverPort = "";
	private Context context;

	@Override public void onReceive(Context context, Intent intent)
	{
		if (intent.getExtras() == null) return;

		this.context = context.getApplicationContext();
		populateSettings();

		if (wifiOnly && !isConnectedToWifi())
		{
			log("Not handing event, wifi is not connected.");
			return;
		}

		if (TextUtils.isEmpty(serverIp) || serverIp.equalsIgnoreCase("http://"))
		{
			log("Not handling event, no server IP configured.");
			return;
		}

		if (intent.getExtras().containsKey("me.anon.grow.PLANT_LIST"))
		{
			String plantData = intent.getExtras().getString("me.anon.grow.PLANT_LIST", "");
			byte[] sendData = shouldBeEncrypted ? EncryptionHelper.encrypt(encryptionKey, plantData) : plantData.getBytes();
			postPlantData(sendData);
		}

		if (sendImages)
		{
			if (intent.getExtras().containsKey("me.anon.grow.IMAGE_ADDED"))
			{
				postImage(intent.getExtras().getString("me.anon.grow.IMAGE_ADDED"), false);
			}
			else if (intent.getExtras().containsKey("me.anon.grow.IMAGE_DELETED"))
			{
				postImage(intent.getExtras().getString("me.anon.grow.IMAGE_DELETED"), true);
			}
		}
	}

	private void log(String event)
	{
		event = "<b>[" + new Date().toLocaleString() + "]</b>" + "<br />" + event;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String current = prefs.getString("log", "");
		prefs.edit().putString("log", current.substring(Math.max(current.length() - 10000, 0), current.length()) + "<br />" + event).apply();
	}

	/**
	 * Posts image to the API
	 * @param path The path of the image to send
	 * @param delete To delete or not
	 */
	private void postImage(final String path, boolean delete)
	{
		File filePath = new File(path);

		if (!delete)
		{
			try
			{
				InputStream fileInputStream = new FileInputStream(filePath);;

				if (shouldBeEncrypted)
				{
					fileInputStream = new EncryptInputStream(encryptionKey, fileInputStream);
				}

				log("Sending image <em>" + path + "</em>");

				RequestParams params = new RequestParams();
				params.put("image", fileInputStream, filePath.getName());
				params.put("filename", filePath.getParentFile().getName() + "/" + filePath.getName());

				AsyncHttpClient client = new AsyncHttpClient();
				client.post(serverIp + (!TextUtils.isEmpty(serverPort) ? (":" + serverPort) : "") + "/image", params, new JsonHttpResponseHandler()
				{
					@Override public void onSuccess(int statusCode, Header[] headers, JSONObject response)
					{
						log("Image <em>" + path + "<em> sent successfully");
					}

					@Override public void onFinish()
					{
						if (getResultCode() != 200)
						{
							log("Image <em>" + path + "<em> failed to send, server responded " + getResultCode());
						}
					}
				});
			}
			catch (Exception e)
			{
				e.printStackTrace();
				log("Could not handle new image event: " + e.getMessage());
			}
		}
		else
		{
			log("Sending delete request for image <em>" + path + "</em>");

			String newPath = filePath.getParentFile().getName() + "/" + filePath.getName();
			AsyncHttpClient client = new AsyncHttpClient();
			client.delete(serverIp + (!TextUtils.isEmpty(serverPort) ? (":" + serverPort) : "") + "/image?image=" + newPath, new JsonHttpResponseHandler()
			{
				@Override public void onSuccess(int statusCode, Header[] headers, JSONObject response)
				{
					log("Image <em>" + path + "<em> delete request sent successfully");
				}

				@Override public void onFinish()
				{
					if (getResultCode() != 200)
					{
						log("Image <em>" + path + "<em> failed to delete, server responded " + getResultCode());
					}
				}
			});
		}
	}

	/**
	 * Posts data to the API for `/plants` endpoint
	 * @param data
	 */
	private void postPlantData(byte[] data)
	{
		log("Sending plant data");

		ByteArrayEntity stringEntity = new ByteArrayEntity(data);

		AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(5);
		client.post(null, serverIp + (!TextUtils.isEmpty(serverPort) ? (":" + serverPort) : "") + "/plants", stringEntity, "application/octet-stream", new JsonHttpResponseHandler()
		{
			@Override public void onSuccess(int statusCode, Header[] headers, JSONObject response)
			{
				log("Plant data successfully sent");
			}

			@Override public void onFinish()
			{
				if (getResultCode() != 200)
				{
					log("Plant data failed to send, server responded " + getResultCode());
				}
			}
		});
	}

	/**
	 * Populates local setting variables for convenience
	 */
	private void populateSettings()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		shouldBeEncrypted = prefs.getBoolean("send_encrypted", false);
		wifiOnly = prefs.getBoolean("wifi_only", false);
		encryptionKey = prefs.getString("encryption_key", "");
		serverIp = prefs.getString("server_ip", "");
		serverPort = prefs.getString("server_port", "");
		sendImages = prefs.getBoolean("send_images", true);

		if (!serverIp.startsWith("http"))
		{
			serverIp = "http://" + serverIp;
		}
	}

	/**
	 * Checks if wifi is connected
	 * @return
	 */
	public boolean isConnectedToWifi()
	{
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		return netInfo != null && netInfo.isConnectedOrConnecting();
	}
}
