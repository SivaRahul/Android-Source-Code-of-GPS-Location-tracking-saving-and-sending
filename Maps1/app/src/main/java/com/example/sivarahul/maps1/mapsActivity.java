package com.example.sivarahul.maps1;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.LocationListener;
import android.location.*;
import com.google.android.gms.location.*;
import android.content.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


import static android.content.Intent.ACTION_SEND;




public class mapsActivity extends Activity implements  GoogleMap.OnMyLocationChangeListener,GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener, GooglePlayServicesClient.OnConnectionFailedListener, GooglePlayServicesClient.ConnectionCallbacks, View.OnClickListener {

    ImageButton save;
    ImageButton shareit;
    ImageButton maptypess;
    TextView ordin;
    ImageButton direc;
    //ImageButton pos;
    double dragLat;
    double dragLong;
    Marker second;
    Address address;
    List<Address> addressList;
    String savedpath;
    String ln;
    String ordinates;
    String filepath;
    String provider;
    String result;
    StringBuffer sb=new StringBuffer("");
    LatLng dragPosition;
    // Location mCurrentLocation;
    Location net_loc = null, gps_loc = null, finalLoc = null;
    String currentTimeStamp;
    SimpleDateFormat dateFormat;
    Geocoder rev;
    GoogleMap googleMap;
    LocationManager locationManager;
    AlertDialog.Builder dialog;
    CameraPosition camPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        savedpath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/savedlocation.txt";
        filepath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/tracking.txt";
        createMapView();
        ids();
        getbes();
        //location();

        maptypess.setOnClickListener(this);
        save.setOnClickListener(this);
        shareit.setOnClickListener(this);
        direc.setOnClickListener(this);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMarkerDragListener(this);
        googleMap.setOnMapLongClickListener(this);

    }
    private void createMapView(){

        try {
            if(googleMap == null ){
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();


            }googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }
    private void ids() {
        maptypess =(ImageButton)findViewById(R.id.type);
        ordin=(TextView)findViewById(R.id.tv1);
        direc=(ImageButton)findViewById(R.id.dir);
        save=(ImageButton)findViewById(R.id.sv);
        shareit=(ImageButton)findViewById(R.id.shr);
        //  pos=(ImageButton)findViewById(R.id.mypos);
    }
    public Boolean writeit(String contents,String path){
        try {
            File file = new File(path);

            // If file does not exists, then create it
         /*   if (!file.exists()) {
                file.createNewFile();
            }*/

            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(contents+"\n");
            bw.close();

            Log.d("Successfully","Successfully");
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.type) {
            maptype();
        }
        if(v.getId()==R.id.dir){                    ///////////////////////////
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?saddr=" + finalLoc.getLatitude() + "," + finalLoc.getLongitude() + "&daddr=" + dragLat + "," + dragLong));
            intent.setClassName(
                    "com.google.android.apps.maps",
                    "com.google.android.maps.MapsActivity");
            startActivity(intent);
        }
        if (v.getId() == R.id.sv) {
            writeit(getCurrentTimeStamp() + "::" +"http://maps.google.com/?q="+dragLat+","+dragLong , savedpath);
        }
        if(v.getId()==R.id.shr){
            Intent msg=new Intent(Intent.ACTION_SEND, Uri.parse("Send to:"));
            msg.setType("message/rfc822");
            // msg.putExtra(Intent.EXTRA_EMAIL, recipients);
            msg.putExtra(Intent.EXTRA_SUBJECT, "Location");
            msg.putExtra(Intent.EXTRA_TEXT, "http://maps.google.com/?q="+dragLat+","+dragLong);
            startActivity(msg);
        }

    }
    void maptype(){

        dialog=new AlertDialog.Builder(this);
        final String maptypes[]={"Satellite","Hybrid","Normal"};
        dialog.setTitle("Map Types");
        dialog.setSingleChoiceItems(maptypes, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (maptypes[i] == "satellite") {
                            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                            Toast.makeText(getApplicationContext(), maptypes[i] + "selected", Toast.LENGTH_SHORT).show();
                        } else if (i == 1) {
                            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                            Toast.makeText(getApplicationContext(), maptypes[i] + "selected", Toast.LENGTH_SHORT).show();
                        } else if (i == 2) {
                            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            Toast.makeText(getApplicationContext(), maptypes[i] + "selected", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
        );
        AlertDialog dialogmaptype=dialog.create();
        dialogmaptype.show();
    }
    public void getbes(){

        boolean gps_enabled = false;
        boolean network_enabled = false;

        locationManager = (LocationManager) this
                .getSystemService(LOCATION_SERVICE);

        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);



        if (gps_enabled) {
            gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (network_enabled) {
            net_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        }
        if(gps_loc==null && net_loc==null){
            showSettingsAlert("NETWORK / GPS");
        }

        else {
            if (gps_loc != null && net_loc != null) {

                if (gps_loc.getAccuracy() >= net_loc.getAccuracy()) {
                    finalLoc = gps_loc;
                    provider = LocationManager.GPS_PROVIDER;
                } else {
                    finalLoc = net_loc;
                    provider = LocationManager.NETWORK_PROVIDER;

                }
            }else{
                if (gps_loc != null) {
                    finalLoc = gps_loc;
                    provider = LocationManager.GPS_PROVIDER;
                } else if (net_loc != null) {
                    finalLoc = net_loc;
                    provider = LocationManager.NETWORK_PROVIDER;
                }
            }
            location();
        }
    }

    private void showSettingsAlert(String s) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                mapsActivity.this);

        alertDialog.setTitle(s + " SETTINGS");

        alertDialog
                .setMessage(s + " is not enabled! Want to go to settings menu?");

        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mapsActivity.this.startActivity(intent);
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();


    }
    private  void location(){

        // getbes();
//        finalLoc = locationManager.getLastKnownLocation(provider);
        if (finalLoc != null) {
            Toast.makeText(getApplicationContext(), "Found!",
                    Toast.LENGTH_SHORT).show();
            // currentlocation();
        }
        locationManager.requestLocationUpdates(provider, 10000, 0, new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                rev = new Geocoder(mapsActivity.this, Locale.ENGLISH);    //////////////////
                result = null;
                currentlocation(location);
                ordin.setText("CHANGING-" + "LAT:" + location.getLatitude() + "LONG:" + location.getLongitude());
                ordinates = String.valueOf(getCurrentTimeStamp() + ":: " + location.getLatitude() + " " + location.getLongitude());
                writeit(ordinates, filepath);
                try {

                    addressList = rev.getFromLocation(
                            location.getLatitude(), location.getLongitude(), 1);
                    if (addressList != null && addressList.size() > 0) {
                        address = addressList.get(0);
                        StringBuilder str = new StringBuilder();
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            str.append(address.getAddressLine(i)).append("\n");
                        }
                        str.append(address.getLocality()).append("\n");
                        str.append(address.getPostalCode()).append("\n");
                        str.append(address.getCountryName()).append("\n"); ////////////////
                        str.append(location.getLatitude()).append("\n");
                        str.append(location.getLongitude()).append("\n");
                        result = str.toString();
                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Could not get address..!", Toast.LENGTH_LONG).show();
                } finally {
                    writeit(getCurrentTimeStamp()+location.getLatitude()+" "+location.getLongitude(),filepath);
                }


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });

    }

    public  void currentlocation(Location locate){
        if(null != googleMap){
            LatLng myLaLn = new LatLng(locate.getLatitude(),
                    locate.getLongitude());
            camPos = new CameraPosition.Builder().target(myLaLn)
                    .zoom(15).bearing(0).tilt(0).build();

            CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
            googleMap.setMyLocationEnabled(true);
           // ordin.setText("POSITION-" + "LAT:" + locate.getLatitude() + "LONG:" + locate.getLongitude());

            CameraPosition camPos2 = new CameraPosition.Builder().target(myLaLn)
                    .zoom(15).bearing(0).tilt(0).build();
            googleMap.animateCamera(camUpd3);

        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        dragPosition = marker.getPosition();
        dragLat = dragPosition.latitude;
        dragLong = dragPosition.longitude;
        ordin.setText("Marker->" + "LAT:" + dragLat + "LONG:" + dragLong);
    }
    public void onMarkerDragStart(Marker marker) {
        ordin.setText("Marker->" + "LAT:" + dragLat + "LONG:" + dragLong);
    }
    @Override
    public void onMarkerDragEnd(Marker marker) {
        ordin.setText("Marker->" + "LAT:" + dragLat + "LONG:" + dragLong);
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(second!=null){
            second.remove();
        }
        ordin.setText("POSITION-" + "LAT:" + finalLoc.getLatitude() + "LONG:" + finalLoc.getLongitude());
        return true;
    }
    @Override
    public void onMapClick(LatLng latLng) {
        dragLat=latLng.latitude;
        dragLong=latLng.longitude;
        if(second!=null){
            second.remove();
        }
        MarkerOptions markerOpts = new MarkerOptions().position(latLng).title(
                "my Location").draggable(true).visible(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        second=googleMap.addMarker(markerOpts);
        ordin.setText("Marker-> " + "LAT:" + dragLat + "LONG:" + dragLong);

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected() {

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMyLocationChange(Location location) {

    }
    public  String getCurrentTimeStamp(){
        try {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            currentTimeStamp = dateFormat.format(new Date()); // Find todays date
            return currentTimeStamp;

        } catch (Exception e) {
            e.printStackTrace();


        }
        return null;
    }

}