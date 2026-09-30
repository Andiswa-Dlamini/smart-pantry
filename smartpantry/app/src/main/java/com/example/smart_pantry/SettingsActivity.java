package com.example.smart_pantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "smart_pantry_prefs";
    public static final String KEY_EXPIRING_ALERTS = "pref_expiring_alerts";
    public static final String KEY_UNITS_SYSTEM = "pref_units_system";

    private Switch switchExpiringAlerts;
    private Spinner spinnerUnitsSystem;
    private SharedPreferences prefs;

    private String[] unitSystemOptions = {"Metric (g, kg, ml, l)", "Imperial (oz, lbs, cups)"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Enable Back/Up button in Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        setupSpinner();

        // Load saved preference values
        boolean alertsEnabled = prefs.getBoolean(KEY_EXPIRING_ALERTS, true);
        String savedUnitSystem = prefs.getString(KEY_UNITS_SYSTEM, "metric");

        switchExpiringAlerts.setChecked(alertsEnabled);
        spinnerUnitsSystem.setSelection("imperial".equalsIgnoreCase(savedUnitSystem) ? 1 : 0);

        // Save switch preference when changed
        switchExpiringAlerts.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_EXPIRING_ALERTS, isChecked).apply());

        // Save unit system preference when changed
        spinnerUnitsSystem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSystem = (position == 1) ? "imperial" : "metric";
                prefs.edit().putString(KEY_UNITS_SYSTEM, selectedSystem).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initViews() {
        switchExpiringAlerts = findViewById(R.id.switchExpiringAlerts);
        spinnerUnitsSystem = findViewById(R.id.spinnerUnitsSystem);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                unitSystemOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnitsSystem.setAdapter(adapter);
    }

    // Handle Back/Up arrow in Action Bar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
