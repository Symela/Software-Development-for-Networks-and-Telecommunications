package com.example.livra.mqttapptest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.livra.mqttapptest.AsyncTasks.Connect;
import com.example.livra.mqttapptest.AsyncTasks.Disconnect;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.opencsv.CSVReader;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // views
    @SuppressLint("StaticFieldLeak")
    private static TextView subText;
    @SuppressLint("StaticFieldLeak")
    private static Button con, discon;


    // mqtt connection
    //TODO: 1. change topics
	// each device must have different topics: topicStr and topicStr1.
    public static String topicStr = "mobile2";
    public static String topicStr1 = "edge2";
    @SuppressLint("StaticFieldLeak")
    public static MqttAndroidClient client;
    public static MqttConnectOptions options;


    // gps
    private String mLastUpdateTime; // location last updated time
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000; // location updates interval - 10sec - default
    public static long updateIntervalInMilliseconds = 10000; // location updates interval according to selected frequency
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000; // fastest updates interval - 5 sec (default)
    public static long fastestUpdateIntervalInMilliseconds = 5000; // fastest updates interval according to selected frequency
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    // bunch of location related apis
    @SuppressLint("StaticFieldLeak")
    public static FusedLocationProviderClient mFusedLocationClient;
    @SuppressLint("StaticFieldLeak")
    public static SettingsClient mSettingsClient;
    public static LocationRequest mLocationRequest;
    public static LocationSettingsRequest mLocationSettingsRequest;
    public static LocationCallback mLocationCallback;
    public static Location mCurrentLocation;


    // wifi
    public static WifiManager wifiManager;


    // accelerommeter
    private static ArrayList<Float> accelerommeterValues;


    // vibrator and ringtone
    private Vibrator vibrator;
    private Ringtone huehue;


    // flags
    public static Boolean mRequestingLocationUpdates;


    // request codes
    public static final int REQUEST_CHECK_SETTINGS = 100;
    private static final int REQUEST_READ_PERMISSION = 200;
    private static final int REQUEST_CAMERA_PERMISSION = 300;


    // general variables
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    @SuppressLint("StaticFieldLeak")
    private static Activity activity;
    public static final String TAG = MainActivity.class.getSimpleName();
    private static String csvToString;
    private static File[] files;
    private static File csvFile;
    //TODO: 2. choose the correct directory path for csv files
    private static String CSV_DIRECTORY_PATH = <Path where Test set is saved on your android device>;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        int cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        huehue = RingtoneManager.getRingtone(context, uri);

        final String clientId = getMacAddr();
        //TODO: 3. change MQTT Broker IP
        String serverUri = "tcp://" + <MQTT IP> + ":1883";
        Log.i("server URI", serverUri);
        client = new MqttAndroidClient(context, serverUri, clientId);

        con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Connect(context, MainActivity.this).execute();
            }
        });

        discon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Disconnect(context).execute();
            }
        });

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {

                switch (message.toString()) {
                    case "0":
                        subText.setText(getString(R.string.safe));
                        subText.setTextColor(getResources().getColor(R.color.no_danger));
                        vibrator.vibrate(500);
                        break;
                    case "1":
                        subText.setText(getString(R.string.wake_up));
                        subText.setTextColor(getResources().getColor(R.color.warning));
                        huehue.play();
                        huehue.play();
                        huehue.play();
                        vibrator.vibrate(2000);
                        break;
                    case "2":

                        subText.setText(getString(R.string.change_lanes));
                        subText.setTextColor(getResources().getColor(R.color.colorAccent));

                        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                            for (int i = 0; i < updateIntervalInMilliseconds / 1000; i++) {
                                Camera cam = Camera.open();
                                Camera.Parameters p = cam.getParameters();
                                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                cam.setParameters(p);
                                cam.startPreview();
                                vibrator.vibrate(250);
                                huehue.play();
                                cam.stopPreview();
                                cam.release();
                            }
                        } else {
                            for (int i = 0; i < updateIntervalInMilliseconds / 1000; i++) {
                                vibrator.vibrate(250);
                                huehue.play();
                            }
                        }
                        break;
                    default:

                        break;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        // restore the values from saved instance state
        restoreValuesFromBundle(savedInstanceState);

        new Connect(context, MainActivity.this).execute();
    }


    // --------------------------- INITIALIZE ----------------------------------------------------------------------------------------------

    @SuppressLint("InlinedApi")
    private void init() {

        // ----- variables & views ----------------
        context = this;

        activity = MainActivity.this;

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(context, "Wifi enabled!", Toast.LENGTH_SHORT).show();
        }

        subText = findViewById(R.id.subText);
        con = findViewById(R.id.connBtn);
        discon = findViewById(R.id.disBtn);

        Button publish = findViewById(R.id.publish_button);
        publish.setVisibility(View.GONE);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        options = new MqttConnectOptions();

        accelerommeterValues = new ArrayList<>();

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // --- API's ------------------
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                publish();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();

        setApis();

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void setApis() {

        Log.i("update", String.valueOf(updateIntervalInMilliseconds) + " " + String.valueOf(fastestUpdateIntervalInMilliseconds));
        if (updateIntervalInMilliseconds == UPDATE_INTERVAL_IN_MILLISECONDS) {
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        } else {
            mLocationRequest.setInterval(updateIntervalInMilliseconds);
        }

        if (fastestUpdateIntervalInMilliseconds == FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS) {
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        } else {
            mLocationRequest.setFastestInterval(fastestUpdateIntervalInMilliseconds);
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }


    // -------------------- MENU FUNCTIONS ---------------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                // Add the buttons
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mRequestingLocationUpdates) {
                            new Disconnect(context).execute();
                        }
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                // Set other dialog properties

                builder.setTitle("Έξοδος από την εφαρμογή.");
                builder.setMessage("Να φύγω;");

                // Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.frequency:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    // ----------------- SAVE / RESTORE VALUES WHEN DESTROYING / CREATING ACTIVITY -----------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
        outState.putParcelable("last_known_location", mCurrentLocation);
        outState.putString("last_updated_on", mLastUpdateTime);
        outState.putLong("update_interval", updateIntervalInMilliseconds);
        outState.putLong("fastest_update", fastestUpdateIntervalInMilliseconds);
    }

    /**
     * Restoring values from saved instance state
     */
    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
            }

            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }

            if (savedInstanceState.containsKey("last_updated_on")) {
                mLastUpdateTime = savedInstanceState.getString("last_updated_on");
            }

            if (savedInstanceState.containsKey("update_interval")) {
                updateIntervalInMilliseconds = savedInstanceState.getLong("update_interval");
            }

            if (savedInstanceState.containsKey("fastest_update")) {
                fastestUpdateIntervalInMilliseconds = savedInstanceState.getLong("fastest_update");
            }
        }

        publish();
    }


    // ---------------- CHANGE UI FUNCTIONS --------------------------------------------------------------------------------------------------------------------

    public static void toggleButtons() {
        if (mRequestingLocationUpdates) {
            con.setEnabled(false);
            con.setBackgroundColor(context.getResources().getColor(R.color.disabled));
            discon.setEnabled(true);
            discon.setBackgroundColor(context.getResources().getColor(R.color.disconnect));
            discon.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            con.setEnabled(true);
            con.setBackgroundColor(context.getResources().getColor(R.color.connect));
            discon.setEnabled(false);
            discon.setBackgroundColor(context.getResources().getColor(R.color.disabled));
            discon.setTextColor(context.getResources().getColor(R.color.disconnect_text_disabled));
        }
    }


    // ------------------- PUBLISH FUNCTIONS ----------------------------------------------------------------------------------------------------

    @SuppressLint("InlinedApi")
    private static void publish() {

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(context, "Wifi enabled!", Toast.LENGTH_SHORT).show();
        }

        if (mCurrentLocation != null) {

            int readPermission;
            readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_PERMISSION);
            } else {
                File dir = new File(CSV_DIRECTORY_PATH);
                files = dir.listFiles();
                csvToString = read_file();

                makeMessageToPublish(mCurrentLocation, csvToString);
            }
        }

        toggleButtons();
    }

    private static String read_file() {
        Random random = new Random();

        File dir = new File(CSV_DIRECTORY_PATH);
        files = dir.listFiles();
        csvFile = files[random.nextInt(files.length)];

        CSVReader csvReader;
        StringBuilder stringBuilder = new StringBuilder();
        try {

            csvReader = new CSVReader(new FileReader(csvFile.getAbsolutePath()));
            String[] nextLine;

            while ((nextLine = csvReader.readNext()) != null) {
                stringBuilder.append("\n").append(Arrays.toString(nextLine));
            }

            return stringBuilder.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void makeMessageToPublish(Location mCurrentLocation, String csv) {

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(context, "Wifi enabled!", Toast.LENGTH_SHORT).show();
        }

        if (mRequestingLocationUpdates && accelerommeterValues.size() > 0) {
            String topic = topicStr;
            String message = getMacAddr() // MAC Address
                    + "\n" + String.format(Locale.getDefault(), "%.3f", mCurrentLocation.getLatitude()) // Latitude
                    + "\n" + String.format(Locale.getDefault(), "%.3f", mCurrentLocation.getLongitude()) // Longitude
                    + "\n" + String.format(Locale.getDefault(), "%.3f", accelerommeterValues.get(0)) // Accelerometer X
                    + "\n" + String.format(Locale.getDefault(), "%.3f", accelerommeterValues.get(1)) // Accelerometer Y
                    + "\n" + String.format(Locale.getDefault(), "%.3f", accelerommeterValues.get(2)) // Accelerometer Z
                    + "\n" + csvFile.getName() // File Name
                    + "\n" + csv; // CSV
            try {
                client.publish(topic, message.getBytes(), 0, false);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "You have to be connected to publish!", Toast.LENGTH_LONG).show();
        }
    }

    private static String getMacAddr() {

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(context, "Wifi enabled!", Toast.LENGTH_SHORT).show();
        }

        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF)).append(":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }


    // ----------------- GET "CHANGE SETTINGS" RESULT ----------------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                break;
            case REQUEST_READ_PERMISSION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "Permission granted.");

                        File dir = new File(CSV_DIRECTORY_PATH);
                        files = dir.listFiles();
                        csvToString = read_file();

                        makeMessageToPublish(mCurrentLocation, csvToString);
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "Permission not granted.");
                        break;
                }
                break;
            case REQUEST_CAMERA_PERMISSION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "Camera permission granted.");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "Camera permission not granted.");
                        break;
                }
                break;
        }
    }


    // ---------- START & STOP GPS UPDATES ----------------------------------------------------------------------------------------------

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    public static void startLocationUpdates() {

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(context, "Wifi enabled!", Toast.LENGTH_SHORT).show();
        }

        MainActivity.mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(MainActivity.TAG, "All location settings are satisfied.");

                        Toast.makeText(context, "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        MainActivity.mFusedLocationClient.requestLocationUpdates(MainActivity.mLocationRequest,
                                MainActivity.mLocationCallback, Looper.myLooper());

                        publish();
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(MainActivity.TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(activity, MainActivity.REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(MainActivity.TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(MainActivity.TAG, errorMessage);

                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                        }

//                        publish();
                    }
                });
    }

    public static void stopLocationUpdates() {

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(context, "Wifi enabled!", Toast.LENGTH_SHORT).show();
        }

        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "Location updates stopped!", Toast.LENGTH_SHORT).show();
                        toggleButtons();
                    }
                });
    }


    // --------- ACTIVITY LIFECYCLE FUNCTIONS --------------------------------------------------------------------------------------------

    @Override
    public void onResume() {
        super.onResume();

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(context, "Wifi enabled!", Toast.LENGTH_SHORT).show();
        }

        setApis();

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(context, "Wifi enabled!", Toast.LENGTH_SHORT).show();
        }

        // Resuming location updates depending on button state and
        // allowed permissions
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
            publish();
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (mRequestingLocationUpdates) {
            // pausing location updates
            stopLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
//        Process.killProcess(Process.myPid());
        if (mRequestingLocationUpdates) {
            // pausing location updates
            stopLocationUpdates();
        }
        super.onDestroy();
    }

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            if (client.isConnected()) {
                new Disconnect(context).execute();
            }
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        accelerommeterValues.add(0, event.values[0]);
        accelerommeterValues.add(1, event.values[1]);
        accelerommeterValues.add(2, event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
