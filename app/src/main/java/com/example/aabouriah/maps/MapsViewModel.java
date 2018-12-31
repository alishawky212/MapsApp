package com.example.aabouriah.maps;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;

import com.example.aabouriah.maps.Entities.DirectionResults;
import com.example.aabouriah.maps.Entities.Route;
import com.example.aabouriah.maps.Entities.Steps;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsViewModel extends AndroidViewModel {

    private MutableLiveData<MarkerOptions> userLocation = new MutableLiveData<>();
    private MutableLiveData<MarkerOptions> placeLocation = new MutableLiveData<>();
    private MutableLiveData<PolylineOptions> directions = new MutableLiveData<>();
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    public MapsViewModel(@NonNull Application application) {
        super(application);
        createLocationRequest();
        setUpLocationCallback();
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates(){
        mFusedLocationProviderClient = new FusedLocationProviderClient(getApplication());
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
    }

    private void setUpLocationCallback(){
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude()));
                    userLocation.setValue(markerOptions);
                }
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(getApplication());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

    }

    public void getSelectedLocation(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        placeLocation.setValue(markerOptions);
    }

    public void getDirections(){

        Call<DirectionResults> call = ApiClient.getInstance().getUserService().getDirections(userLocation.getValue().getPosition().latitude+","+userLocation.getValue().getPosition().longitude,
                placeLocation.getValue().getPosition().latitude+","+placeLocation.getValue().getPosition().longitude);

        call.enqueue(new Callback<DirectionResults>() {
            @Override
            public void onResponse(@NonNull Call<DirectionResults> call, @NonNull Response<DirectionResults> response) {
                DirectionResults directionResults = response.body();
                ArrayList<LatLng> routesList = new ArrayList<LatLng>();
                if (directionResults.getRoutes().size() > 0) {
                    ArrayList<LatLng> decodelist;
                    Route routeA = directionResults.getRoutes().get(0);
                    if (routeA.getLegs().size() > 0) {
                        List<Steps> steps = routeA.getLegs().get(0).getSteps();
                        Steps step;
                        com.example.aabouriah.maps.Entities.Location location;
                        String polyline;
                        for (int i = 0; i < steps.size(); i++) {
                            step = steps.get(i);
                            location = step.getStart_location();
                            routesList.add(new LatLng(location.getLat(), location.getLng()));
                            polyline = step.getPolyline().getPoints();
                            decodelist = RouteDecode.decodePoly(polyline);
                            routesList.addAll(decodelist);
                            location = step.getEnd_location();
                            routesList.add(new LatLng(location.getLat(), location.getLng()));
                        }
                    }
                }
                if (routesList.size() > 0) {
                    PolylineOptions rectLine = new PolylineOptions().width(10).color(
                            Color.RED);

                    for (int i = 0; i < routesList.size(); i++) {
                        rectLine.add(routesList.get(i));
                    }

                    directions.setValue(rectLine);

                }
            }
            @Override
            public void onFailure(@NonNull Call<DirectionResults> call, @NonNull Throwable t) {

            }
        });

    }

    public MutableLiveData<MarkerOptions> getUserLocation() {
        return userLocation;
    }

    public MutableLiveData<MarkerOptions> getPlaceLocation() {
        return placeLocation;
    }

    public MutableLiveData<PolylineOptions> getDirectionsData(){
        return directions;
    }
}
