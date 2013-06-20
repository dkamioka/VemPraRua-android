package org.k3x.vemprarua.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.k3x.vemprarua.model.FieldError;
import org.k3x.vemprarua.model.User;
import org.k3x.vemprarua.util.Configs;
import org.k3x.vemprarua.util.JsonHandler;
import org.k3x.vemprarua.util.JsonREST;

import android.util.Log;


public class LocationAPI implements JsonHandler{

	private static final String API_RELATIVE_URL_CREATE_USER = "api/users/";
	private static final String API_RELATIVE_URL_UPDATE_USER = "api/users/%1$s/";

	private static final int CODE_CREATE_USER = 0;
	private static final int CODE_UPDATE_USER = 1;

	private LocationAPIHandler handler;
	private User user;

	public void create(User user, LocationAPIHandler handler) {
		try {
			this.handler = handler;
			this.user = user;
			if (Configs.DEBUG)		Log.i(LocationAPI.class.getName(), "New User!");
	
			JSONObject userJson = new JSONObject();
			if(user.latitude != 0) {userJson.put("latitude", user.latitude);}
			if(user.longitude != 0) {userJson.put("longitude", user.longitude);}
	
			JSONObject json = new JSONObject();
			json.put("user", userJson);
	
			String absoluteUrl = JsonREST.getAbsoluteUrl(API_RELATIVE_URL_CREATE_USER);
			JsonREST jsonREST = new JsonREST();
			jsonREST.post(
					absoluteUrl,
					json,
					this,
					new HashMap<String, String>(),
					CODE_CREATE_USER
					);
		} catch(Exception e) {
			throw new RuntimeException("it will never happen");
		}
	}

	public void onCreated(JSONObject response) {
		try {
			List<FieldError> errors = new ArrayList<FieldError>();

			JSONObject errorsJson = response.getJSONObject("user").getJSONObject("errors");
			if(errorsJson.length() != 0) {
				JSONArray namesJsonArray = errorsJson.names();
				for(int index = 0; index < namesJsonArray.length(); index++) {
					FieldError error = new FieldError();
					error.field = namesJsonArray.getString(index);

					JSONArray errorJson = errorsJson.getJSONArray(error.field);
					for(int midx = 0; midx < errorJson.length(); midx++) {
						error.messages.add(errorJson.getString(midx));
					}
					errors.add(error);
				}

				handler.onCreated(false, null, errors);
			} else {
				JSONObject userJson = response.getJSONObject("user");
				user.id = userJson.getString("id");
				user.name = userJson.getString("name");
				user.latitude = userJson.getDouble("latitude");
				user.longitude = userJson.getDouble("longitude");

				handler.onCreated(true, user, errors);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void update(User user, LocationAPIHandler handler) throws ClientProtocolException, IOException, JSONException {
		this.handler = handler;
		this.user = user;
		if (Configs.DEBUG)		Log.i(LocationAPI.class.getName(), "Update User!");

		JSONObject userJson = new JSONObject();
		if(user.name != null) {userJson.put("name", user.name);}
		if(user.latitude != 0) {userJson.put("latitude", user.latitude);}
		if(user.longitude != 0) {userJson.put("longitude", user.longitude);}

		JSONObject json = new JSONObject();
		json.put("user", userJson);


		String relativeUrl = String.format(API_RELATIVE_URL_UPDATE_USER, user.id);
		String absoluteUrl = JsonREST.getAbsoluteUrl(relativeUrl);
		JsonREST jsonREST = new JsonREST();
		jsonREST.put(
				absoluteUrl,
				json,
				this,
				new HashMap<String, String>(),
				CODE_UPDATE_USER
				);
	}

	public void onUpdated(JSONObject response) {
		try {
			List<FieldError> errors = new ArrayList<FieldError>();

			JSONObject errorsJson = response.getJSONObject("user").getJSONObject("errors");
			if(errorsJson.length() != 0) {
				JSONArray namesJsonArray = errorsJson.names();
				for(int index = 0; index < namesJsonArray.length(); index++) {
					FieldError error = new FieldError();
					error.field = namesJsonArray.getString(index);

					JSONArray errorJson = errorsJson.getJSONArray(error.field);
					for(int midx = 0; midx < errorJson.length(); midx++) {
						error.messages.add(errorJson.getString(midx));
					}
					errors.add(error);
				}

				handler.onCreated(false, null, errors);
			} else {
				JSONObject userJson = response.getJSONObject("user");
				user.id = userJson.getString("id");
				user.name = userJson.getString("name");
				user.latitude = userJson.getDouble("latitude");
				user.longitude = userJson.getDouble("longitude");

				handler.onCreated(true, user, errors);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	public void onSuccess(JSONObject response, int code) {
		switch (code) {
		case CODE_CREATE_USER:
			onCreated(response);
			break;
		case CODE_UPDATE_USER:
			onCreated(response);
			break;

		default:
			throw new RuntimeException("wrong code at UserAPI");
		}
	}


	public void onSuccess(JSONArray response, int code) {
		// not used
	}


	public void onFailure(Throwable e, JSONObject response, int code) {
		if(response != null) {
			Log.w(LocationAPI.class.getName(), response.toString());
		} else {
			Log.w(LocationAPI.class.getName(), "Response is null");
		}
		e.printStackTrace();

	}
}
