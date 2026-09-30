package com.example.smart_pantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddEditPantryActivity extends AppCompatActivity {

    private TextView textTitle;
    private EditText etItemName;
    private EditText etQuantity;
    private Spinner spinnerUnit;
    private EditText etExpiryDate;
    private Button btnSelectDate;
    private Button btnSave;
    private Button btnCancel;

    private int pantryId = -1;
    private PantryItem pantryItem;
    private String[] unitOptions = {"g", "kg", "ml", "l", "pcs", "tsp", "tbsp", "cup"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_pantry);

        initViews();
        setupSpinner();
        setupDatePicker();

        // Match Intent extra key "pantryID" from PantryActivity
        pantryId = getIntent().getIntExtra("pantryID", -1);

        if (pantryId != -1) {
            textTitle.setText("Edit Pantry Item");
            loadExistingPantryItem();
        } else {
            textTitle.setText("Add Pantry Item");
        }

        btnSave.setOnClickListener(v -> savePantryItem());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void initViews() {
        textTitle = findViewById(R.id.textTitle);
        etItemName = findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etQuantity);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                unitOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(adapter);
    }

    private void setupDatePicker() {
        btnSelectDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddEditPantryActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        etExpiryDate.setText(formattedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void loadExistingPantryItem() {
        PantryDataSource ds = new PantryDataSource(this);
        try {
            ds.open();
            pantryItem = ds.getSpecificPantryItem(pantryId);
            ds.close();

            if (pantryItem != null) {
                etItemName.setText(pantryItem.getItemName());
                etQuantity.setText(String.valueOf(pantryItem.getQuantity()));

                if (pantryItem.getExpiryDate() != null) {
                    etExpiryDate.setText(pantryItem.getExpiryDate());
                }

                // Select matching unit in spinner
                if (pantryItem.getUnit() != null) {
                    for (int i = 0; i < unitOptions.length; i++) {
                        if (unitOptions[i].equalsIgnoreCase(pantryItem.getUnit())) {
                            spinnerUnit.setSelection(i);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading item details", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePantryItem() {
        String name = etItemName.getText().toString().trim();
        String qtyStr = etQuantity.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();
        String expiry = etExpiryDate.getText().toString().trim();

        // Input Validation
        if (name.isEmpty()) {
            etItemName.setError("Item name is required");
            etItemName.requestFocus();
            return;
        }

        if (qtyStr.isEmpty()) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(qtyStr);
        } catch (NumberFormatException e) {
            etQuantity.setError("Please enter a valid number");
            etQuantity.requestFocus();
            return;
        }

        if (quantity <= 0) {
            etQuantity.setError("Quantity must be greater than 0");
            etQuantity.requestFocus();
            return;
        }

        if (pantryItem == null) {
            pantryItem = new PantryItem();
        }

        pantryItem.setItemName(name);
        pantryItem.setQuantity(quantity);
        pantryItem.setUnit(unit);
        pantryItem.setExpiryDate(expiry.isEmpty() ? null : expiry);

        PantryDataSource ds = new PantryDataSource(this);
        boolean success = false;
        try {
            ds.open();
            if (pantryId != -1) {
                pantryItem.setPantryId(pantryId);
                success = ds.updatePantryItem(pantryItem);
            } else {
                success = ds.insertPantryItem(pantryItem);
            }
            ds.close();
        } catch (Exception e) {
            Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show();
        }

        if (success) {
            Toast.makeText(this, "Item saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save item", Toast.LENGTH_LONG).show();
        }
    }
}
