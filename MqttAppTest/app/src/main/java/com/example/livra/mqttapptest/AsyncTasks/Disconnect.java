package com.example.livra.mqttapptest.AsyncTasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.livra.mqttapptest.MainActivity;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Disconnect extends AsyncTask<Void, Void, Void> {

    @SuppressLint("StaticFieldLeak")
    private Context context;

    public Disconnect(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        if (!MainActivity.wifiManager.isWifiEnabled()) {
            MainActivity.wifiManager.setWifiEnabled(true);
        }

        try {
            IMqttToken token = MainActivity.client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(context, "disconnected!!", Toast.LENGTH_LONG).show();
                    MainActivity.mRequestingLocationUpdates = false;
                    MainActivity.stopLocationUpdates();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(context, "Error, can't disconnect!!", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return null;
    }
}
