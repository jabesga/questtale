package com.besga.jonander.questtale;


import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.util.Date;


public class CustomMapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final LatLng SYDNEY = new LatLng(-33.88,151.21);

    protected  GoogleApiClient mGoogleApiClient;
    protected Location mCurrentLocation;
    protected LocationRequest mLocationRequest;
    GoogleMap googleMap;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    private Marker theMarker;
    Circle circle;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.map_layout,null,false);
        SupportMapFragment gmapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        gmapFragment.getMapAsync(this);
        return v;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        theMarker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(43.260441, -2.937270))
                .title("Hello world"));
        Log.d("CUSTOM", "1 - MAP LOADED");
        map.setMyLocationEnabled(true);
        buildGoogleApiClient();

    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i("CUSTOM", "2 - Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        createLocationRequest();

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onStart() { // why cant be protected?
        super.onStart();
    }
    @Override
    public void onStop() { // why cant be protected?

        super.onStop();
        mGoogleApiClient.disconnect();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    public void moveMapTo(Location mCurrentLocation) {
        if(googleMap != null){
            LatLng  location = new LatLng (mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 19));
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("CUSTOM", "3 - CONNECTED");
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            moveMapTo(mCurrentLocation);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            Log.d("CUSTOM", mCurrentLocation.toString());
            //Toast.makeText(getActivity(), mCurrentLocation.toString(), Toast.LENGTH_SHORT).show();
            startLocationUpdates();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("CUSTOM", "LOCATION CHANGED");
        mCurrentLocation = location;
        Log.d("CUSTOM", location.toString());
        //Toast.makeText(getActivity(), mCurrentLocation.toString(), Toast.LENGTH_SHORT).show();
        moveMapTo(mCurrentLocation);
        if(theMarker != null){
            isAroundMe(mCurrentLocation);
        }
        else{
            Log.d("CUSTOM", "IS NULL ME?");
        }

    }

    public void isAroundMe(Location mCurrentLocation){
        Log.d("CUSTOM", "IS AROUND ME?");
        float[] distance = new float[2];
        if(circle!=null){
            circle.remove();
        }
        circle = googleMap.addCircle(new CircleOptions()
                .center(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                .radius(10)
                .fillColor(Color.parseColor("#EAEAEA")));

        Location.distanceBetween( theMarker.getPosition().latitude, theMarker.getPosition().longitude,
                circle.getCenter().latitude, circle.getCenter().longitude, distance);

        if( distance[0] > circle.getRadius()  ){
            Toast.makeText(getActivity(), "Outside", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "Inside", Toast.LENGTH_LONG).show();
        }
    }
}
