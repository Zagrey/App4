package com.android.zagrey.app4;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;
import com.common.util.Point;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.*;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "MapsActivity";
    private final String url = "http://192.168.1.197:8080/api/points";
    private Location lastLocation;
    private Timer mTimer;
    private static final long TIMER_INTERVAL = 1000;
    private static final boolean POINT_ADD_TO_DB = false;
    private static final boolean POINTS_LOAD_FROM_DB = false;
    private static Location mCurrentLocation;
    private static String mLastUpdateTime;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopLocationUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerMethod();
            }

        }, 0, TIMER_INTERVAL);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mGoogleApiClient.disconnect();
        mTimer.cancel();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle_aubergine));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraMoveCanceledListener(this);
        mMap.setOnMapClickListener(this);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setBuildingsEnabled(true);

//        LatLng home = new LatLng(59.951344705114785, 30.21912563592196);
//        CameraPosition position = new CameraPosition.Builder().target(home).zoom(17).bearing(0).tilt(75).build();
//
//
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(position);
//        mMap.animateCamera(cameraUpdate);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

//        mMap.addMarker(new MarkerOptions().position(home).title("Home marker"));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick: " + latLng.latitude + ", " + latLng.longitude);

        String description = "BOMB on: " + latLng.latitude + ",\n" + latLng.longitude;

        if (mCurrentLocation != null) {
            double dist = Util.distance(mCurrentLocation.getLatitude(), latLng.latitude,
                    mCurrentLocation.getLongitude(), latLng.longitude, 0, 0);

            double azimuth = Util.azimuth(mCurrentLocation.getLatitude(), latLng.latitude,
                    mCurrentLocation.getLongitude(), latLng.longitude);

            description = "Distance to point: " + Math.round(dist) + " meters, azimuth: " +
                    Math.round(azimuth) + " degrees";
            Toast.makeText(this, description, Toast.LENGTH_LONG).show();
        }

        mMap.addMarker(new MarkerOptions().position(latLng).title(description));

        if (POINT_ADD_TO_DB) {
            Point p = Point.builder()
                    .createdOn(new Timestamp(System.currentTimeMillis()))
                    .lifeTime(15)
                    .latitude(latLng.latitude)
                    .longitude(latLng.longitude)
                    .description(description)
                    .build();


            new PointAddTask().execute(p);
        }
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onCameraMoveStarted(int reason) {

        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            Log.v(TAG, "onCameraMoveStarted: The user gestured on the map (REASON_GESTURE)");
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
            Log.v(TAG, "onCameraMoveStarted: The user tapped something on the map (REASON_API_ANIMATION)");
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            Log.v(TAG, "onCameraMoveStarted: The app moved the camera (REASON_DEVELOPER_ANIMATION)");
        }

    }

    @Override
    public void onCameraMove() {
        Log.v(TAG, "onCameraMove: The camera is moving.");
    }

    @Override
    public void onCameraMoveCanceled() {
        Log.v(TAG, "onCameraMoveCanceled: Camera movement canceled.");
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onCameraIdle() {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        String location = lastLocation == null ?
                "unknown" :
                lastLocation.getLatitude() + ", " + lastLocation.getLongitude();
        Log.v(TAG, "onCameraIdle: The camera has stopped moving. Last location: " + location);

        try {
            if (POINTS_LOAD_FROM_DB) {
                new PointsLoaderTask().execute();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());



        startLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Log.d(TAG, "onLocationChanged: " + mCurrentLocation );
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        LatLng p = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition position = new CameraPosition.Builder().target(p).zoom(17).bearing(0).tilt(75).build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(position);
        mMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class PointAddTask extends AsyncTask<Point, Void, Void> {
        @Override
        protected Void doInBackground(Point... params) {
            try {

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                Point p = params[0];
                restTemplate.postForObject(url, p, Point.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage());
            }

            return null;
        }

//        @Override
//        protected void onPostExecute(Point p) {
//
//            if (p != null) {
//                Log.i(TAG, "onPostExecute: " + p.getId() + ", " + p.getDescription());
//            }
//        }

    }

    private class PointsLoaderTask extends AsyncTask<Void, Void, List<Point>> {
        @Override
        protected void onPostExecute(List<Point> points) {
            super.onPostExecute(points);

            Log.d(TAG, "onPostExecute: loaded points: " + points.size());

            mMap.clear();

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            for (Point p : points) {

                if (timestamp.getTime() - (p.getCreatedOn().getTime() + 1000 * p.getLifeTime()) < 0) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(p.getLatitude(), p.getLongitude()))
                            .title("BOMB on: " + p.getLatitude() + ",\n" + p.getLongitude()));
                }
            }
        }

        @Override
        protected List<Point> doInBackground(Void... params) {
            try {
                Map<String, Double> par = new HashMap<>();
                if (lastLocation != null) {
                    par.put("latitude", lastLocation.getLatitude());
                    par.put("longitude", lastLocation.getLongitude());
                }

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                @SuppressWarnings({"unchecked", "InstantiatingObjectToGetClassObject"})
                Point[] pointsArray = restTemplate.getForObject(url, new Point[0].getClass(), par);

                //                Log.d(TAG, "PointsLoaderTask:doInBackground: loaded points: " + points);
                return new ArrayList<>(Arrays.asList(pointsArray));
            } catch (Exception e) {
                Log.e(TAG, "PointsLoaderTask:doInBackground: ", e);
            }
            return null;
        }
    }

    private void timerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
//        this.runOnUiThread(Timer_Tick);

        if(POINTS_LOAD_FROM_DB) {
            Log.d(TAG, "timerMethod: PointsLoaderTask().execute()");
            new PointsLoaderTask().execute();
        }
    }


//    private Runnable Timer_Tick = new Runnable() {
//        public void run() {
//
//            //This method runs in the same thread as the UI.
//
//            //Do something to the UI thread here
//
//        }
//    };
}
