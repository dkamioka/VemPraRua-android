package org.k3x.vemprarua.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.k3x.vemprarua.util.CustomMultiPartEntity.ProgressListener;

import android.app.NotificationManager;
import android.os.AsyncTask;


public class JsonREST {
	private static final int HTTP_GET = 1;
	private static final int HTTP_POST = 2;
	private static final int HTTP_PUT = 3;
	private static final int HTTP_DELETE = 4;
	private static final int HTTP_POST_MP = 5;
	private static final int CODE_NOTIFICATION_UPLOAD = 45;
	
	public static int EXCEPTION_NONE = 0;
	public static int EXCEPTION_TIMEOUT = 1;
	public static int EXCEPTION_INTERNET = 2;
	public static int EXCEPTION_JSON = 3;
	public static int EXCEPTION_ILLEGAL = 4;

	private static JsonHandler lastHandler;
	private static Map <String, String> lastHeaders;
	private static StringEntity lastStringEntity;
	private static CustomMultiPartEntity lastMpEntity;
	private static int lastHttp;
	private static int lastCodeReq;
	private static String lastUrl;


	public static String getAbsoluteUrl(String relativeUrl) {
		return Configs.BASE_URL + relativeUrl;
	}


	public void get(String url, JsonHandler handler, Map<String, String> headers, int codeReq) {
		AsyncHttp asyncHttp = new AsyncHttp(handler, headers, codeReq, HTTP_GET);
		asyncHttp.execute(url);
	}

	public void put(String url, JSONObject json, JsonHandler handler, Map<String, String> headers, int codeReq) throws UnsupportedEncodingException {
		StringEntity stringEntity = new StringEntity("");
		if(json != null)
			stringEntity = new StringEntity(json.toString(), "utf-8");
		stringEntity.setContentType("application/json");
		stringEntity.setContentEncoding( new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

		AsyncHttp asyncHttp = new AsyncHttp(handler, headers, codeReq, stringEntity, HTTP_PUT);
		asyncHttp.execute(url);
	}

	public void post(String url, JSONObject json, JsonHandler handler, Map<String, String> headers, int codeReq) throws UnsupportedEncodingException {
		StringEntity stringEntity = new StringEntity("");
		if(json != null)
			stringEntity = new StringEntity(json.toString(), "utf-8");
		stringEntity.setContentType("application/json");
		stringEntity.setContentEncoding( new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

		AsyncHttp asyncHttp = new AsyncHttp(handler, headers, codeReq, stringEntity, HTTP_POST);
		asyncHttp.execute(url);
	}

	public void post(String url, CustomMultiPartEntity entity, JsonHandler handler, int codeReq) throws UnsupportedEncodingException {
		AsyncHttp asyncHttp = new AsyncHttp(handler, codeReq, entity, HTTP_POST_MP);
		asyncHttp.execute(url);
	}

	public void delete(String url, JsonHandler handler, Map<String, String> headers, int codeReq) throws UnsupportedEncodingException {
		AsyncHttp asyncHttp = new AsyncHttp(handler, headers, codeReq, HTTP_DELETE);
		asyncHttp.execute(url);
	}


	public void tryAgain(){
		AsyncHttp asyncHttp;
		if (lastHttp == HTTP_POST_MP){
			asyncHttp = new AsyncHttp(lastHandler, lastCodeReq, lastMpEntity, lastHttp);
		} else {
			asyncHttp = new AsyncHttp(lastHandler, lastHeaders, lastCodeReq, lastStringEntity, lastHttp);
		}
		asyncHttp.execute(lastUrl);
	}




	public class AsyncHttp extends AsyncTask<String, Integer, String>{
		protected int HTTP_REQUEST;
		protected String url;
		protected int codeReq;
		JsonHandler handler;
		StringEntity stringEntity;
		CustomMultiPartEntity mpEntity;
		HttpResponse response;
		HttpClient httpclient;
		Map<String, String> headers;
		public long totalSize;
		NotificationManager notificationManager;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		public AsyncHttp(JsonHandler handler, Map<String, String> headers, int codeReq, int http) {
			this.handler = handler;
			this.headers = headers;
			this.codeReq = codeReq;
			this.HTTP_REQUEST = http;
		}

		public AsyncHttp(JsonHandler handler, Map<String, String> headers, int codeReq, StringEntity stringEntity, int http) {
			this.handler = handler;
			this.headers = headers;
			this.codeReq = codeReq;
			this.stringEntity = stringEntity;
			this.HTTP_REQUEST = http;
		}

		public AsyncHttp(JsonHandler handler, int codeReq, CustomMultiPartEntity mpEntity, int http) {
			this.handler = handler;
			this.codeReq = codeReq;
			this.mpEntity = mpEntity;
			this.HTTP_REQUEST = http;
		}

		@Override
		protected String doInBackground(String... uri){
			JsonREST.lastCodeReq = this.codeReq;
			JsonREST.lastHandler = this.handler;
			JsonREST.lastHttp = this.HTTP_REQUEST;
			JsonREST.lastStringEntity = this.stringEntity;
			JsonREST.lastHeaders = this.headers;
			JsonREST.lastUrl = uri[0];
			JsonREST.lastMpEntity = this.mpEntity;

			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
			HttpConnectionParams.setSoTimeout(httpParameters, 20000);
			httpclient = new DefaultHttpClient(httpParameters);
			String responseString = "";
			this.url = uri[0];
			try {
				executeRequest();
				StatusLine statusLine = response.getStatusLine();
				if(statusLine.getStatusCode() == HttpStatus.SC_OK){
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					responseString = out.toString();
				} else{
					//Closes the connection.
					response.getEntity().getContent().close();
					RequisitionException.setError(EXCEPTION_ILLEGAL);
				}
			} catch (ConnectTimeoutException e) {
				RequisitionException.setError(EXCEPTION_TIMEOUT);
			} catch (SocketTimeoutException e) {
				RequisitionException.setError(EXCEPTION_TIMEOUT);
			} catch (IOException e) {
				RequisitionException.setError(EXCEPTION_INTERNET);
			} finally{
				RequisitionException.dismiss();
			}
			return responseString;
		}

		protected void executeRequest() throws ClientProtocolException, IOException{
			switch (HTTP_REQUEST) {

			case HTTP_GET:
				HttpGet httpGet = new HttpGet(this.url);
				for(Map.Entry<String, String> entry : this.headers.entrySet()) {
					httpGet.setHeader(entry.getKey(), entry.getValue());
				}

				this.response = httpclient.execute(httpGet);
				break;

			case HTTP_POST:
				HttpPost httpPost = new HttpPost(this.url);
				for(Map.Entry<String, String> entry : this.headers.entrySet()) {
					httpPost.setHeader(entry.getKey(), entry.getValue());
				}

				httpPost.setHeader("Content-type", "application/json; charset=utf-8");
				httpPost.setEntity(this.stringEntity);
				this.response = httpclient.execute(httpPost);
				break;

			case HTTP_PUT:
				HttpPut httpPut = new HttpPut(this.url);
				for(Map.Entry<String, String> entry : this.headers.entrySet()) {
					httpPut.setHeader(entry.getKey(), entry.getValue());
				}

				httpPut.setHeader("Content-type", "application/json; charset=utf-8");
				httpPut.setEntity(this.stringEntity);
				this.response = httpclient.execute(httpPut);
				break;

			case HTTP_DELETE:
				HttpDelete httpDelete = new HttpDelete(this.url);
				for(Map.Entry<String, String> entry : this.headers.entrySet()) {
					httpDelete.setHeader(entry.getKey(), entry.getValue());
				}

				this.response = httpclient.execute(httpDelete);
				break;
				
			case HTTP_POST_MP:
				HttpPost httpPostMP = new HttpPost(this.url);
				httpPostMP.setEntity(this.mpEntity);
				mpEntity.setListener(new ProgressListener() {
					@Override
					public void transferred(long num)
					{
						publishProgress((int) ((num / (float) totalSize) * 100));
					}
				});
				
				totalSize = mpEntity.getContentLength();
				
				this.response = httpclient.execute(httpPostMP);
				break;

			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (notificationManager != null) notificationManager.cancel(CODE_NOTIFICATION_UPLOAD);
			try {
				if (RequisitionException.EXCEPTION != 0)
					handler.onFailure(new Throwable(), null, codeReq);

				if (result.startsWith("{")){
					RequisitionException.EXCEPTION = 0;
					handler.onSuccess(new JSONObject(result), codeReq);
				}	else if (result.startsWith("[")) {
					RequisitionException.EXCEPTION = 0;
					handler.onSuccess(new JSONArray(result), codeReq);
				}
			} catch (JSONException e) {
				RequisitionException.setError(EXCEPTION_JSON);
				handler.onFailure(e, null, codeReq);
			} 
		}
	}

}
