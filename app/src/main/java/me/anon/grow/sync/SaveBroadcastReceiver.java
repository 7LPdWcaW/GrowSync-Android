package me.anon.grow.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	private String encryptionKey = "";
	private String serverIp = "";
	private String serverPort = "";
	private Context context;

	@Override public void onReceive(Context context, Intent intent)
	{
		if (intent.getExtras() == null) return;

		this.context = context.getApplicationContext();
		populateSettings();

		if (intent.getExtras().containsKey("me.anon.grow.PLANT_LIST"))
		{
			String plantData = intent.getExtras().getString("me.anon.grow.PLANT_LIST", "");
			byte[] sendData = shouldBeEncrypted ? EncryptionHelper.encrypt(encryptionKey, plantData) : plantData.getBytes();
			postPlantData(sendData);
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

	private void log(String event)
	{
		event = "[" + new Date().toLocaleString() + "] " + event;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString("log", prefs.getString("log", "") + "<br />" + event).apply();
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
				client.post(serverIp + ":" + serverPort + "/image", params, new JsonHttpResponseHandler()
				{
					@Override public void onSuccess(int statusCode, Header[] headers, JSONObject response)
					{
						log("Image <em>" + path + "<em> sent successfully");
					}

					@Override public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse)
					{
						log("Image <em>" + path + "<em> failed to send, server responded " + statusCode);
					}
				});
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			log("Sending delete request for image <em>" + path + "</em>");

			String newPath = filePath.getParentFile().getName() + "/" + filePath.getName();
			AsyncHttpClient client = new AsyncHttpClient();
			client.delete(serverIp + ":" + serverPort + "/image?image=" + newPath, new JsonHttpResponseHandler()
			{
				@Override public void onSuccess(int statusCode, Header[] headers, JSONObject response)
				{
					log("Image <em>" + path + "<em> delete request sent successfully");
				}

				@Override public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse)
				{
					log("Image <em>" + path + "<em> failed to delete, server responded " + statusCode);
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
		client.post(null, serverIp + ":" + serverPort + "/plants", stringEntity, "application/octet-stream", new JsonHttpResponseHandler()
		{
			@Override public void onSuccess(int statusCode, Header[] headers, JSONObject response)
			{
				log("Plant data successfully sent");
			}

			@Override public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse)
			{
				log("Plant data failed to send, server responded " + statusCode);
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

		if (!serverIp.startsWith("http"))
		{
			serverIp = "http://" + serverIp;
		}
	}
}
