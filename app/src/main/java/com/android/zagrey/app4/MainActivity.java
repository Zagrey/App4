package com.android.zagrey.app4;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import com.common.util.Greeting;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    int samples = 0;

    private SensorManager sm;
    private Sensor acc;
    private Sensor mag;

    private EditText accX, accY, accZ;
    private EditText magX, magY, magZ;
    private EditText locAzimuth, locPitch, locRoll;
    private EditText locAzimuthD, locPitchD, locRollD;

    float[] mAcc; // data for accelerometer
    float[] mMag; // data for magnetometer
    float[] mR = new float[16];
    float[] mI = new float[16];
    float[] mLoc = new float[3];

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, new PlaceholderFragment())
//                    .commit();
//        }

//        final Button button = (Button) findViewById(R.id.buttonMap);
//        button.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                Log.i("tag2", "onShowMap: 2");
//            }
//        });

        // gui components
        accX = (EditText) findViewById(R.id.accX);
        accY = (EditText) findViewById(R.id.accY);
        accZ = (EditText) findViewById(R.id.accZ);
        magX = (EditText) findViewById(R.id.magX);
        magY = (EditText) findViewById(R.id.magY);
        magZ = (EditText) findViewById(R.id.magZ);
        locAzimuth = (EditText) findViewById(R.id.locAzimuth);
        locPitch = (EditText) findViewById(R.id.locPitch);
        locRoll = (EditText) findViewById(R.id.locRoll);
        locAzimuthD = (EditText) findViewById(R.id.locAzimuthD);
        locPitchD = (EditText) findViewById(R.id.locPitchD);
        locRollD = (EditText) findViewById(R.id.locRollD);

        //sensors
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void onShowMap(View view){

        Log.i("tag1", "onShowMap: 1");
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        new HttpRequestTask().execute();
        sm.registerListener(this, acc, SensorManager.SENSOR_DELAY_UI);
        sm.registerListener(this, mag, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onStop() {
        super.onStop();
        sm.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Заполнение меню; добавляются пункты меню в action bar, если он присутствует.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Это обработчик нажатия на пукт меню action bar. Аction bar будет
        // автоматически обрабатывать нажатия Home/Up кнопки, до тех пор
        // пока вы вы определите их действия в родительском activity в AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            new HttpRequestTask().execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mAcc = event.values.clone();

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mMag = event.values.clone();

        if (mAcc != null && mMag != null) {
            boolean success = SensorManager.getRotationMatrix(mR, mI, mAcc, mMag);
            if (success) {
                samples++;
                if ( samples <= 10 ) {
                    return;
                }
                samples = 1;

                SensorManager.getOrientation(mR, mLoc);

                //set text in view
                DecimalFormat df = new DecimalFormat("###.####");
                accX.setText(df.format(mAcc[0]));
                accY.setText(df.format(mAcc[1]));
                accZ.setText(df.format(mAcc[2]));
                magX.setText(df.format(mMag[0]));
                magY.setText(df.format(mMag[1]));
                magZ.setText(df.format(mMag[2]));
                locAzimuth.setText(df.format(mLoc[0]));
                locPitch.setText(df.format(mLoc[1]));
                locRoll.setText(df.format(mLoc[2]));
                locAzimuthD.setText(df.format(Math.toDegrees(mLoc[0])));
                locPitchD.setText(df.format(Math.toDegrees(mLoc[1])));
                locRollD.setText(df.format(Math.toDegrees(mLoc[2])));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }



    private class HttpRequestTask extends AsyncTask<Void, Void, Greeting> {
        @Override
        protected Greeting doInBackground(Void... params) {
            try {
                final String url = "http://192.168.0.105:8080/api/greetings/10227";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Greeting greeting = restTemplate.getForObject(url, Greeting.class);
//                Greeting g = new Greeting();
//                g.setContent("asdasd");
//                g.setId("10");
                return greeting;
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Greeting greeting) {
            TextView greetingIdText = (TextView) findViewById(R.id.id_value);
            TextView greetingContentText = (TextView) findViewById(R.id.content_value);

            if (greeting != null) {
                greetingIdText.setText(greeting.getId().toString());
                greetingContentText.setText(greeting.getText());
            } else {
                greetingIdText.setText("ID");
                greetingContentText.setText("NONE");
            }
        }

    }

}
