package com.example.aabouriah.maps;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private Boolean mLocationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Marker selectedMarker,userMarker;
    private MapsViewModel mapsViewModel;
    private Polyline directionsPolyline = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Initialize ViewModel
        mapsViewModel = ViewModelProviders.of(this).get(MapsViewModel.class);
        mapsViewModel.getUserLocation().observe(this, new Observer<MarkerOptions>() {
            @Override
            public void onChanged(@Nullable MarkerOptions markerOptions) {
                if (mMap != null){
                     userMarker = mMap.addMarker(markerOptions);
                     userMarker.setTitle("Current Location");
                     if (markerOptions != null)
                     mMap.moveCamera(CameraUpdateFactory.newLatLng(markerOptions.getPosition()));
                }
            }
        });

        mapsViewModel.getPlaceLocation().observe(this, new Observer<MarkerOptions>() {
            @Override
            public void onChanged(@Nullable MarkerOptions markerOptions) {
                if (mMap != null){
                    if (selectedMarker != null) {
                        selectedMarker.remove();
                    }
                    selectedMarker = mMap.addMarker(markerOptions);
                    selectedMarker.setTitle("Selected Location");
                }
            }
        });

        mapsViewModel.getDirectionsData().observe(this, new Observer<PolylineOptions>() {
            @Override
            public void onChanged(@Nullable PolylineOptions polylineOptions) {
                if (mMap != null) {
                    if (directionsPolyline != null) {
                        directionsPolyline.remove();
                    }
                    directionsPolyline = mMap.addPolyline(polylineOptions);
                }
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                 .findFragmentById(R.id.map);
        if (mapFragment != null)
        mapFragment.getMapAsync(this);

        getLocationPermission();

        if (mLocationPermissionGranted) {
            mapsViewModel.startLocationUpdates();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mapsViewModel.getSelectedLocation(latLng);
        mapsViewModel.getDirections();
    }

    public void getLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionGranted = true;
        }else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    mapsViewModel.startLocationUpdates();
                }
            }
        }
    }
}
