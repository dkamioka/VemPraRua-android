package org.k3x.vemprarua;

import java.util.List;

import org.k3x.vemprarua.api.AppVersionAPI;
import org.k3x.vemprarua.api.AppVersionAPIHandler;
import org.k3x.vemprarua.api.LocationAPI;
import org.k3x.vemprarua.api.LocationAPIHandler;
import org.k3x.vemprarua.model.FieldError;
import org.k3x.vemprarua.model.User;
import org.k3x.vemprarua.services.VemPraRuaService;
import org.k3x.vemprarua.util.Configs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends android.support.v4.app.FragmentActivity implements LocationAPIHandler, AppVersionAPIHandler {

	private User mUser;
	private List<User> mUsers;
	private GoogleMap mMap;
	private TextView mTotalTextView;
	private boolean mFitPins = true;
	
	private String mAppUrl;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int zoom = getIntent().getIntExtra("zoom", 8);
        double latitude = getIntent().getDoubleExtra("latitude", 0);
        double longitude = getIntent().getDoubleExtra("longitude", 0);
        String title = getIntent().getStringExtra("title");
        String message = getIntent().getStringExtra("message");
        
        int res = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(res != ConnectionResult.SUCCESS) {
        }
        
        GCMRegistrar.checkDevice(this);
		// TODO: "this method (checkManifest) is only necessary when you are developing the application;
		//		once the application is ready to be published, you can remove it"
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			Log.i(MainActivity.class.getName(), "Registrando..");
			GCMRegistrar.register(this, Configs.GCM_SENDER_ID);
			Log.v(MainActivity.class.getName(), "Already registered:" + GCMRegistrar.getRegistrationId(this));
		} else {
			Log.v(MainActivity.class.getName(), "Already registered:" + GCMRegistrar.getRegistrationId(this));
		}
		
        mTotalTextView = (TextView) findViewById(R.id.main_activity_total_users);
        
        mUser = User.getUser(this);
        if(mUser.id != null) {
        	LocationAPI api = new LocationAPI();
        	api.update(mUser, this);
        	Toast.makeText(this, "Olá, " + mUser.name, Toast.LENGTH_LONG).show();
        	startTrack();
        } else {
        	LocationAPI api = new LocationAPI();
        	api.create(mUser, this);
        }
        
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMap = mapFragment.getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng latLng;
        

        CameraUpdate update;
        if(latitude != 0 && longitude != 0) {
        	mFitPins = false;
        	latLng = new LatLng(latitude, longitude);
        	update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        	

    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage(message)
    		       .setTitle(title)
    		       .setPositiveButton("OK", new OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					dialog.dismiss();
    				}
    			}).create().show();
    		
        	
        } else if(mUser.latitude != 0 && mUser.longitude != 0) {
        	latLng = new LatLng(mUser.latitude, mUser.longitude);
        	update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        	LocationAPI api = new LocationAPI();
        	api.list(this);
        } else {
        	latLng = new LatLng(-23.564224, -46.653156);
        	update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        	LocationAPI api = new LocationAPI();
        	api.list(this);
        }
        mMap.moveCamera(update);
        mMap.setMyLocationEnabled(true);
    }
    
    private void startTrack() {
    	Intent intent = new Intent(this, VemPraRuaService.class);
		startService(intent);
    }
    
    private void stopTrack() {
    	Intent intent = new Intent(this, VemPraRuaService.class);
		stopService(intent);
		finish();
    }
 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
        case R.id.action_main_menu_refresh_map:
            LocationAPI api = new LocationAPI();
            api.list(this);
            return true;
        case R.id.action_main_menu_shutdown:
        	stopTrack();
            return true;
        case R.id.action_main_menu_report_conflitc:
        	reportConflict();
            return true;
        default:
        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void reportConflict() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// 2. Chain together various setter methods to set the dialog characteristics
		builder.setMessage("Está função ainda não está disponível.")
		       .setTitle("Em breve!")
		       .setPositiveButton("OK", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

		// 3. Get the AlertDialog from create()
		builder.create().show();
	}
	
	@Override
	public void onCreated(boolean success, User user, List<FieldError> errors) {
		mUser = user;
		mUser.save(this);
		startTrack();
		
        AppVersionAPI appVersionAPI = new AppVersionAPI();
        appVersionAPI.showLast(this);
	}

	@Override
	public void onUpdated(boolean success, User user, List<FieldError> errors) {
		if(user != null) {
			mUser = user;
			mUser.save(this);
		}

        AppVersionAPI appVersionAPI = new AppVersionAPI();
        appVersionAPI.showLast(this);
	}

	@Override
	public void onListed(boolean success, int total, List<User> users, List<FieldError> errors) {
		mUsers = users;
		mMap.clear();
		
		mTotalTextView.setVisibility(View.VISIBLE);
		mTotalTextView.setText(total + " manifestantes!!!");
		
		if(mFitPins) {
			LatLng latLng;
			Builder latLngBuilder = LatLngBounds.builder();
			for (User user: mUsers) {
		        if(user.latitude != 0) {
		        	
		        	latLng = new LatLng(user.latitude, user.longitude);
		        	latLngBuilder.include(latLng);
					mMap.addMarker(new MarkerOptions()
				        .position(latLng)
				        .title(user.name)
				        .snippet(user.status)
				        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
		        }
			}
			
	        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(latLngBuilder.build(), 60);
	        mMap.moveCamera(update);
		} else {
			for (User user: mUsers) {
		        if(user.latitude != 0) {
					LatLng latLng = new LatLng(user.latitude, user.longitude);
		        	mMap.addMarker(new MarkerOptions()
				        .position(latLng)
				        .title(user.name)
				        .snippet(user.status)
				        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
		        }
			}
		}
		
		mFitPins = true;
	}

	@Override
	public void onShowedLast(boolean success, int version, String appUrl) {
		if(version > Configs.APP_VERSION && appUrl != null) {
			this.mAppUrl = appUrl;
			// 1. Instantiate an AlertDialog.Builder with its constructor
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			// 2. Chain together various setter methods to set the dialog characteristics
			builder.setMessage("Temos uma nova versão deste app.\nAtualize agora.!")
			       .setTitle("Nova Versão!")
			       .setPositiveButton("OK", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.this.mAppUrl));
						startActivity(intent);
					}
				});

			// 3. Get the AlertDialog from create()
			builder.create().show();
		}
	}
    
}
