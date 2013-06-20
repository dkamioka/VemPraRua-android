package org.k3x.vemprarua.services;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.k3x.vemprarua.R;
import org.k3x.vemprarua.api.LocationAPI;
import org.k3x.vemprarua.api.LocationAPIHandler;
import org.k3x.vemprarua.model.FieldError;
import org.k3x.vemprarua.model.User;
import org.k3x.vemprarua.util.Configs;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class VemPraRuaService extends Service implements LocationListener, LocationAPIHandler {


	private static final int NOTIFICATION_CODE_GPS_OFF = 0;
	private static final int NOTIFICATION_CODE_INTERNET_OFF = 1;
	private static final int NOTIFICATION_CODE_LOCATION_LOG = 2;
	private static final int NOTIFICATION_CODE_NOT_FOUND = 3;

	private NotificationManager mNotificationManager;
	private User user;
	private long interval = 5 * 60 * 1000;

	public IBinder onBind(Intent intent) {
		return null;
	}

	// not set?
	int mStartMode; // indicates how to behave if the service is killed

	public void onCreate() {
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		LocationListener locationListener = this;

		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0, locationListener);
	}


	public int onStartCommand(Intent intent, int flags, int startId) {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		user = User.getUser(this);
		// Acquire a reference to the system Location Manager
		List<String> providers = locationManager.getProviders(true);
		Location location = null;

		if (providers != null){
			for (int i=providers.size()-1; i>=0; i--){
				location = locationManager.getLastKnownLocation(providers.get(i));
				if (location != null) break;
			}
		}

		sendLocation(location);

		return START_STICKY; // Should keep the GPS working
		//				return mStartMode;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (Configs.DEBUG) Log.i("Serv", "onDestroy");
		
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		sendLocation(location);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (Configs.DEBUG) System.out.println("OSC_provider_" + provider);
		if (Configs.DEBUG) System.out.println("OSC_status_" + status);
		if (Configs.DEBUG) System.out.println("OSC_bundle_" + extras.isEmpty());
	}

	public void onProviderEnabled(String provider) {
		if(provider.equals("gps")) {
			mNotificationManager.cancel(NOTIFICATION_CODE_GPS_OFF);
		}
		
		LocationListener locationListener = this;
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0, locationListener);
	}

	public void onProviderDisabled(String provider) {
		if(provider.equals("gps")) {
			int icon = R.drawable.ic_launcher;
			CharSequence tickerText = "Cuidado! GPS desativado.";
			long when = System.currentTimeMillis();

			Notification notification = new Notification(icon, tickerText, when);

			Context context = getApplicationContext();
			CharSequence contentTitle = "Ative seu GPS!";
			CharSequence contentText = "Ativando seu GPS vc aparecerá em nosso mapa.!";
			
			Intent notificationIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

			mNotificationManager.notify(NOTIFICATION_CODE_GPS_OFF, notification);
		}
	}

	@SuppressLint("DefaultLocale")
	private void sendLocation(Location location) {
		LocationListener locationListener = this;
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0, locationListener);
		
		if(location == null) {
			return;
		}

		if (Configs.DEBUG) {
			mNotificationManager.cancel(NOTIFICATION_CODE_NOT_FOUND);

			CharSequence titleText = "Nova posição!";
			
			String latitude = String.format("%.5f", location.getLatitude());
			String longitude = String.format("%.5f", location.getLongitude());
			String accuracy = String.format("%.2f", location.getAccuracy());
			
			CharSequence contentText = "La:" + latitude + " | Lo:" + longitude + " | Ac:" + accuracy;

			NotificationCompat.Builder mBuilder =
			        new NotificationCompat.Builder(this)
			        .setSmallIcon(R.drawable.ic_launcher)
			        .setContentTitle(titleText)
			        .setContentText(contentText);
			
			NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(NOTIFICATION_CODE_LOCATION_LOG, mBuilder.build()); 
		}

		try {
			if (Configs.DEBUG) Log.i(Configs.LOG_TAG, "Nova Posição!");

			user = User.getUser(this);
			user.latitude = location.getLatitude();
			user.longitude = location.getLongitude();

			LocationAPI api = new LocationAPI();
			user.save(this);
			api.update(user, this);
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
 
	}

	public void onUpdated(boolean success, User user, List<FieldError> errors) {
		if(success) {
			mNotificationManager.cancel(NOTIFICATION_CODE_INTERNET_OFF);
		} else {
			CharSequence titleText = "Falha de conexão (internet)";
			CharSequence contentText = "Somente com internet vc será contabilizado";

			NotificationCompat.Builder mBuilder =
			        new NotificationCompat.Builder(this)
			        .setSmallIcon(R.drawable.ic_launcher)
			        .setContentTitle(titleText)
			        .setContentText(contentText);
			
			NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(NOTIFICATION_CODE_INTERNET_OFF, mBuilder.build()); 
		}
	}


	@Override
	public void onCreated(boolean success, User user, List<FieldError> errors) {
		// not used
		
	}


	@Override
	public void onListed(boolean success, int total, List<User> users, List<FieldError> errors) {
		// not used
	}
}
