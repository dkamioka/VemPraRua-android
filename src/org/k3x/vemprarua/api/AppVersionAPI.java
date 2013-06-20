package org.k3x.vemprarua.api;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.k3x.vemprarua.util.Configs;
import org.k3x.vemprarua.util.JsonHandler;
import org.k3x.vemprarua.util.JsonREST;

import android.util.Log;


public class AppVersionAPI implements JsonHandler{

	private static final String API_RELATIVE_URL_SHOW_LAST = "api/versions/last";

	private static final int CODE_SHOW_LAST = 0;

	private AppVersionAPIHandler handler;

	public void showLast(AppVersionAPIHandler handler) {
		try {
			this.handler = handler;
			if (Configs.DEBUG)		Log.i(AppVersionAPI.class.getName(), "Verify last version!");
	
			String absoluteUrl = JsonREST.getAbsoluteUrl(API_RELATIVE_URL_SHOW_LAST);
			JsonREST jsonREST = new JsonREST();
			jsonREST.get(
					absoluteUrl,
					this,
					new HashMap<String, String>(),
					CODE_SHOW_LAST
					);
		} catch(Exception e) {
			throw new RuntimeException("it will never happen");
		}
	}

	public void onShowedLast(JSONObject response) {
		int version = response.optInt("version", 0);
		String url = response.optString("url", null);
		handler.onShowedLast(true, version, url);
	}
	
	
	public void onSuccess(JSONObject response, int code) {
		switch (code) {
		case CODE_SHOW_LAST:
			onShowedLast(response);
			break;

		default:
			throw new RuntimeException("wrong code at AppVersionAPI");
		}
	}


	public void onSuccess(JSONArray response, int code) {
		// not used
	}


	public void onFailure(Throwable e, JSONObject response, int code) {
		if(response != null) {
			Log.w(AppVersionAPI.class.getName(), response.toString());
		} else {
			Log.w(AppVersionAPI.class.getName(), "Response is null");
		}
		e.printStackTrace();

	}
}
