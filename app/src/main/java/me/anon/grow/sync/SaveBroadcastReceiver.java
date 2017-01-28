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

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import me.anon.grow.helper.EncryptionHelper;
import me.anon.grow.stream.EncryptInputStream;

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

	/**
	 * Posts image to the API
	 * @param path The path of the image to send
	 * @param delete To delete or not
	 */
	private void postImage(String path, boolean delete)
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

				RequestParams params = new RequestParams();
				params.put("image", fileInputStream, filePath.getName());
				params.put("filename", filePath.getParentFile().getName() + "/" + filePath.getName());

				AsyncHttpClient client = new AsyncHttpClient();
				client.post(serverIp + ":" + serverPort + "/image", params, new JsonHttpResponseHandler()
				{
					@Override public void onSuccess(int statusCode, Header[] headers, JSONObject response)
					{

					}

					@Override public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse)
					{

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
			String newPath = filePath.getParentFile().getName() + "/" + filePath.getName();
			AsyncHttpClient client = new AsyncHttpClient();
			client.delete(serverIp + ":" + serverPort + "/image?image=" + newPath, new JsonHttpResponseHandler()
			{
				@Override public void onSuccess(int statusCode, Header[] headers, JSONObject response)
				{

				}

				@Override public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse)
				{

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
		ByteArrayEntity stringEntity = new ByteArrayEntity(data);

		AsyncHttpClient client = new AsyncHttpClient();
		client.post(null, serverIp + ":" + serverPort + "/plants", stringEntity, "application/octet-stream", new JsonHttpResponseHandler()
		{
			@Override public void onSuccess(int statusCode, Header[] headers, JSONObject response)
			{

			}

			@Override public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse)
			{

			}
		});
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

		if (!serverIp.startsWith("http"))
		{
			serverIp = "http://" + serverIp;
		}
	}
}
