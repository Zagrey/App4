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
import com.common.util.Point;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.*;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "MapsActivity";
    private final String url = "http://192.168.1.197:8080/api/points";
    private Location lastLocation;
    private Timer mTimer;
    private static final long TIMER_INTERVAL = 1000;

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

        LatLng sydney = new LatLng(59.953125, 30.217260);
        CameraPosition position = new CameraPosition.Builder().target(sydney).zoom(17).bearing(0).tilt(75).build();


        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(position);
        mMap.animateCamera(cameraUpdate);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick: " + latLng.latitude + ", " + latLng.longitude);

        mMap.addMarker(new MarkerOptions().position(latLng).title("BOMB on: " + latLng.latitude + ",\n" + latLng.longitude));


        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());


        Point p = Point.builder()
                .createdOn(new Timestamp(System.currentTimeMillis()))
                .lifeTime(15)
                .latitude(latLng.latitude)
                .longitude(latLng.longitude)
                .description("BOMB on: " + latLng.latitude + ", " + latLng.longitude)
                .build();


        new HttpRequestTask().execute(p);
    }

    @Override
    public void onCameraMoveStarted(int reason) {

        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            Log.d(TAG, "onCameraMoveStarted: The user gestured on the map (REASON_GESTURE)");
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
            Log.d(TAG, "onCameraMoveStarted: The user tapped something on the map (REASON_API_ANIMATION)");
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            Log.d(TAG, "onCameraMoveStarted: The app moved the camera (REASON_DEVELOPER_ANIMATION)");
        }
    }

    @Override
    public void onCameraMove() {
        Log.d(TAG, "onCameraMove: The camera is moving.");
    }

    @Override
    public void onCameraMoveCanceled() {
        Log.d(TAG, "onCameraMoveCanceled: Camera movement canceled.");
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onCameraIdle() {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        String location = lastLocation == null ?
                "unknown" :
                lastLocation.getLatitude() + ", " + lastLocation.getLongitude();
        Log.d(TAG, "onCameraIdle: The camera has stopped moving. Last location: " + location);

        try {

            new PointsLoaderTask().execute();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class HttpRequestTask extends AsyncTask<Point, Void, Void> {
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

        Log.d(TAG, "timerMethod: PointsLoaderTask().execute()");
        new PointsLoaderTask().execute();
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
