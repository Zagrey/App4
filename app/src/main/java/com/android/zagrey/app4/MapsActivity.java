package com.android.zagrey.app4;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.common.util.Greeting;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "onMapClick: " + latLng.latitude + ", " + latLng.longitude);

                mMap.addMarker(new MarkerOptions().position(latLng).title("BOMB on: " + latLng.latitude + ",\n" + latLng.longitude));

                final String url = "http://192.168.1.197:8080/api/greetings";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                Greeting greeting = new Greeting();
                greeting.setText("BOMB on: " + latLng.latitude + ", " + latLng.longitude);

//                Greeting greetingResult = restTemplate.postForObject(url, greeting, Greeting.class);

                new HttpRequestTask().execute(greeting);
            }
        });

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
        mMap.setBuildingsEnabled(false);

        LatLng sydney = new LatLng(59.953125, 30.217260);
        CameraPosition position = new CameraPosition.Builder().target(sydney).zoom(17).bearing(0).tilt(75).build();


        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(position);
        mMap.animateCamera(cameraUpdate);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
    }

    private class HttpRequestTask extends AsyncTask<Greeting, Void, Greeting> {
        @Override
        protected Greeting doInBackground(Greeting... params) {
            try {
                final String url = "http://192.168.1.197:8080/api/greetings";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                Greeting greeting = params[0];
                Greeting greetingResult = restTemplate.postForObject(url, greeting, Greeting.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Greeting greeting) {

            if (greeting != null) {
                Log.i(TAG, "onPostExecute: " + greeting.getId() + ", " + greeting.getText());
            }
        }

    }
}