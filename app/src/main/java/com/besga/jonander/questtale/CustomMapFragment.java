package com.besga.jonander.questtale;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class CustomMapFragment extends Fragment implements OnMapReadyCallback, LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {

    protected Location mCurrentLocation;
    protected LocationRequest mLocationRequest;
    GoogleMap googleMap;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    public static final long INTERACTION_RADIUS = 25;
    private Marker theMarker;
    Circle interactiveRadius;
    Button interactionButton;
    private ArrayList<MapEntity> mapEntitiesList;
    private ArrayList<MapEntity> reachableMapEntitiesList;


    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.map_layout, container, false);
        createLocationRequest();

        interactionButton = (Button) v.findViewById(R.id.interactionButton);
        interactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                interactWithClicked(view);
            }
        });
        return v;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        requestLocationSettings();

    }

    protected void requestLocationSettings(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =  LocationServices.SettingsApi.checkLocationSettings(MainActivity.mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d("CUSTOM", "Location settings enabled");
                        locationSettingsAreEnabled();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            Log.d("CUSTOM", "Asking the user to activate the location settings");
                            status.startResolutionForResult(CustomMapFragment.this.getActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    public void locationSettingsAreEnabled(){
        Log.d("CUSTOM", "Trying to show Google Map");
        SupportMapFragment gmapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        gmapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d("CUSTOM", "GoogleMap ready");
        googleMap = map;
        map.setMyLocationEnabled(true);
        //map.setOnMarkerClickListener(this);

        // Download markers
        String entities_data = "" +
                "{'entities_data':\n" +
                "    [\n" +
                "        {\n" +
                "            'entity_id': 1,\n" +
                "            'entity_name': 'Igor Quintela',\n" +
                "            'conversation': 'Pues aquí estoy. Estudiando en Ariz. Y tú?',\n" +
                "            'close_answer': 'Pasaba a saludarte',\n" +
                "            'marker_data': {\n" +
                "                'lat': 43.289988,\n" +
                "                'lng': -2.979743,\n" +
                "                'title': '?'\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            'entity_id': 2,\n" +
                "            'entity_name': 'Nagore Reina',\n" +
                "            'conversation': 'Soy la princesa de este castillo, arrodillate ante mi o te echaré a mis dragones',\n" +
                "            'close_answer': 'Como ordenes',\n" +
                "            'marker_data': {\n" +
                "                'lat': 43.239887,\n" +
                "                'lng': -2.878159,\n" +
                "                'title': '?'\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            'entity_id': 3,\n" +
                "            'entity_name': 'Jesus Angel',\n" +
                "            'conversation': 'SOY RICO MUAHAHAH',\n" +
                "            'close_answer': 'Bueno mejor me voy yendo',\n" +
                "            'marker_data': {\n" +
                "                'lat': 43.239640,\n" +
                "                'lng': -2.878077,\n" +
                "                'title': '?'\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            'entity_id': 4,\n" +
                "            'entity_name': 'David Vidal',\n" +
                "            'conversation': 'Vicio al Elite',\n" +
                "            'close_answer': 'SPACEEEEEEEEEEEE',\n" +
                "            'marker_data': {\n" +
                "                'lat': 43.239806,\n" +
                "                'lng': -2.878246,\n" +
                "                'title': 'Pirata espacial'\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        generateMarkers(entities_data);

        //Update own position
        startLocationUpdates();
    }
    public void generateMarkers(String data){
        mapEntitiesList = new ArrayList<MapEntity>();

        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray entities_list = jsonObject.getJSONArray("entities_data");
            for(int i = 0; i < entities_list.length(); i++){
                int entity_id = (int) entities_list.getJSONObject(i).get("entity_id");
                String entity_name = (String) entities_list.getJSONObject(i).get("entity_name");
                Log.d("CUSTOM", entity_name.toString());
                String conversation = (String) entities_list.getJSONObject(i).get("conversation");
                String close_answer = (String) entities_list.getJSONObject(i).get("close_answer");
                JSONObject marker_data = (JSONObject) entities_list.getJSONObject(i).get("marker_data");
                Double lat = marker_data.getDouble("lat");
                Double lng = marker_data.getDouble("lng");
                String title = marker_data.getString("title");

                MarkerOptions mOptions = new MarkerOptions()
                        .position(new LatLng(lat,lng))
                        .title(title);
                Marker new_marker = googleMap.addMarker(mOptions);

                MapEntity new_entity = new MapEntity(entity_id, entity_name, conversation, close_answer, mOptions);
                mapEntitiesList.add(new_entity);
            }
        }
        catch (JSONException e){
            Log.d("CUSTOM", e.toString());
        }

    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                MainActivity.mGoogleApiClient, mLocationRequest, this);
    }

    boolean mCameraMovedToPlayer = false;

    @Override
    public void onLocationChanged(Location location) {
        //Log.d("CUSTOM", "Location has changed. Updating data");
        Log.d("CUSTOM", "Location changed: " + location.toString());
        mCurrentLocation = location;

        if(interactiveRadius != null){
            interactiveRadius.remove();
        }
        //Toast.makeText(getActivity(), "Updating circle", Toast.LENGTH_LONG).show();

        interactiveRadius = googleMap.addCircle(new CircleOptions()
                .center(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                .radius(INTERACTION_RADIUS)
                .fillColor(Color.parseColor("#EAEAEA")));

        if(!mCameraMovedToPlayer){
            mCameraMovedToPlayer = true;
            if(googleMap != null){
                LatLng  locationToMoveCamera = new LatLng (mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationToMoveCamera, 19));
            }

        }

        if(mapEntitiesList.size() > 0){
            somethingAroundMe(mCurrentLocation);
        }

    }

    public void somethingAroundMe(Location mCurrentLocation){
        Log.d("CUSTOM", "Checking is something around");
        float[] distance = new float[2];

        reachableMapEntitiesList = new ArrayList<>();

        for(int i = 0; i < mapEntitiesList.size(); i++){
            Location.distanceBetween( mapEntitiesList.get(i).getMarkerOptions().getPosition().latitude, mapEntitiesList.get(i).getMarkerOptions().getPosition().longitude,
                    interactiveRadius.getCenter().latitude, interactiveRadius.getCenter().longitude, distance);

            if( distance[0] > interactiveRadius.getRadius()  ){
                //Toast.makeText(getActivity(), "Outside", Toast.LENGTH_LONG).show();
            } else {
                reachableMapEntitiesList.add(mapEntitiesList.get(i));
                //.Toast.makeText(getActivity(), "Inside", Toast.LENGTH_LONG).show();
            }
        }
        if(reachableMapEntitiesList.size() > 0){
            interactionButton.setVisibility(View.VISIBLE);
        }
        else{
            interactionButton.setVisibility(View.GONE);
        }
        Log.d("CUSTOM", Integer.toString(reachableMapEntitiesList.size()));

    }
    /*
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(reachableMarkers.contains(marker)){
            Log.d("CUSTOM", "PRESSED MARKER NUMBER: " + marker.getTitle());
        }
        return false;
    }*/

    public void interactWithClicked(View v){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        //builderSingle.setIcon(R.drawable.map_icon);
        builderSingle.setTitle("Select...");

        final ArrayAdapter<MapEntity> arrayAdapter = new ArrayAdapter<MapEntity>(getActivity(), R.layout.select_dialog_singlechoice, R.id.DialogItem);
        for(int i = 0; i < reachableMapEntitiesList.size(); i++ ){
            arrayAdapter.add(reachableMapEntitiesList.get(i));
        }

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String strName = arrayAdapter.getItem(i).getEntityName();
                Log.d("CUSTOM", "SELECTED: " + strName);

                Bundle b = new Bundle();
                b.putParcelable("ENTITY_SELECTED", arrayAdapter.getItem(i));

                Intent intent = new Intent(getActivity(), NPCConversation.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
        builderSingle.show();
    }
}