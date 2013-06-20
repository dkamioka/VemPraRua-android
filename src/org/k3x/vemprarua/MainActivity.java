package org.k3x.vemprarua;

import java.util.List;

import org.k3x.vemprarua.api.LocationAPI;
import org.k3x.vemprarua.api.LocationAPIHandler;
import org.k3x.vemprarua.model.FieldError;
import org.k3x.vemprarua.model.User;
import org.k3x.vemprarua.services.VemPraRuaService;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends android.support.v4.app.FragmentActivity implements LocationAPIHandler {

	private User mUser;
	private List<User> mUsers;
	private GoogleMap mMap;
	private TextView mTotalTextView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
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
        if(mUser.latitude == 0) {
        	latLng = new LatLng(-23.564224, -46.653156);
        } else {
        	latLng = new LatLng(mUser.latitude, mUser.longitude);
        }
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mMap.moveCamera(update);
        mMap.setMyLocationEnabled(true);
        

    	LocationAPI api = new LocationAPI();
    	api.list(this);
    }
    
    private void startTrack() {
    	Intent intent = new Intent(this, VemPraRuaService.class);
		startService(intent);
    }
    
    private void stopTrack() {
    	Intent intent = new Intent(this, VemPraRuaService.class);
		stopService(intent);
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
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onCreated(boolean success, User user, List<FieldError> errors) {
		mUser = user;
		mUser.save(this);
		startTrack();
	}

	@Override
	public void onUpdated(boolean success, User user, List<FieldError> errors) {
		if(user != null) {
			mUser = user;
			mUser.save(this);
		}
	}

	@Override
	public void onListed(boolean success, int total, List<User> users, List<FieldError> errors) {
		mUsers = users;
		mMap.clear();
		
		mTotalTextView.setVisibility(View.VISIBLE);
		mTotalTextView.setText(total + " manifestantes!!!");
		
		LatLng latLng;
		for (User user: mUsers) {
	        if(mUser.latitude != 0) {
	        	latLng = new LatLng(mUser.latitude, mUser.longitude);
				mMap.addMarker(new MarkerOptions()
		        .position(latLng)
		        .title(user.name));
		        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
		        mMap.moveCamera(update);
	        }
		}
	}
    
}
