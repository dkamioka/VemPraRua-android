package org.k3x.vemprarua.util;

import org.json.JSONArray;
import org.json.JSONObject;

public interface JsonHandler {

	public void onSuccess(JSONObject response, int code);
	
	public void onSuccess(JSONArray response, int code);

	public void onFailure(Throwable e, JSONObject errorResponse, int codeReq);
}
