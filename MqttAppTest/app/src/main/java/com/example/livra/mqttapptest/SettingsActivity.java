package com.example.livra.mqttapptest;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        RadioGroup radioGroup = findViewById(R.id.frequency_radioGroup);

        RadioButton radioButton;

        switch (String.valueOf(MainActivity.updateIntervalInMilliseconds)) {
            case "20000":
                radioButton = findViewById(R.id.twentySeconds);
                radioButton.setChecked(true);
                break;
            case "10000":
                radioButton = findViewById(R.id.tenSeconds);
                radioButton.setChecked(true);
                break;
            case "5000":
                radioButton = findViewById(R.id.fiveSeconds);
                radioButton.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.twentySeconds:
                        MainActivity.updateIntervalInMilliseconds = 20000;
                        MainActivity.fastestUpdateIntervalInMilliseconds = 10000;
                        break;
                    case R.id.tenSeconds:
                        MainActivity.updateIntervalInMilliseconds = 10000;
                        MainActivity.fastestUpdateIntervalInMilliseconds = 5000;
                        break;
                    case R.id.fiveSeconds:
                        MainActivity.updateIntervalInMilliseconds = 5000;
                        MainActivity.fastestUpdateIntervalInMilliseconds = 2500;
                        break;
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
