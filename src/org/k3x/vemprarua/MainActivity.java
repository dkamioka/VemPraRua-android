package org.k3x.vemprarua;

import java.util.List;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.k3x.vemprarua.api.LocationAPI;
import org.k3x.vemprarua.api.LocationAPIHandler;
import org.k3x.vemprarua.model.FieldError;
import org.k3x.vemprarua.model.User;
import org.k3x.vemprarua.services.VemPraRuaService;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends android.support.v4.app.FragmentActivity implements LocationAPIHandler {

	private User mUser;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mUser = User.getUser(this);
        if(mUser.id != null) {
        	Toast.makeText(this, "Olá, " + mUser.name, Toast.LENGTH_LONG).show();
        	startTrack();
        } else {
        	LocationAPI api = new LocationAPI();
        	api.create(mUser, this);
        }
        
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        GoogleMap map = mapFragment.getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng latLng;
        if(mUser.latitude == 0) {
        	latLng = new LatLng(-23.564224, -46.653156);
        } else {
        	latLng = new LatLng(mUser.latitude, mUser.longitude);
        }
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        map.moveCamera(update);
        map.setMyLocationEnabled(true);
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
	public void onCreated(boolean success, User user, List<FieldError> errors) {
		mUser = user;
		mUser.save(this);
		startTrack();
	}

	@Override
	public void onUpdated(boolean success, User user, List<FieldError> errors) {
		// not used
	}
    
}
