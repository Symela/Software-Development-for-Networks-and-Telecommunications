package com.example.livra.mqttapptest.AsyncTasks;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.widget.Toast;

import com.example.livra.mqttapptest.BuildConfig;
import com.example.livra.mqttapptest.MainActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Connect extends AsyncTask<Void, Void, Void> {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    @SuppressLint("StaticFieldLeak")
    private Activity activity;

    public Connect(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        connect();
        return null;
    }

    private void connect() {
        if (!MainActivity.wifiManager.isWifiEnabled()) {
            MainActivity.wifiManager.setWifiEnabled(true);
            Toast.makeText(context, "Wifi enabled!", Toast.LENGTH_SHORT).show();
        }
        try {
            IMqttToken token = MainActivity.client.connect(MainActivity.options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(context, "connected!!", Toast.LENGTH_LONG).show();
                    setSubscription();
                    requestPermission();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(context, "Error, can't connect!!", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void requestPermission() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(activity)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        MainActivity.mRequestingLocationUpdates = true;
                        MainActivity.startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.activity.startActivity(intent);
    }

    private void setSubscription() {
        try {
            if (!MainActivity.wifiManager.isWifiEnabled()) {
                MainActivity.wifiManager.setWifiEnabled(true);
            }
            MainActivity.client.subscribe(MainActivity.topicStr1, 0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
