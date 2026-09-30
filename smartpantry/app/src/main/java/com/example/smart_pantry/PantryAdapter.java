package com.example.smart_pantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private ArrayList<PantryItem> pantryData;
    private View.OnClickListener mOnItemClickListener;
    private boolean isDeleting;
    private Context parentContext;

    public class PantryViewHolder extends RecyclerView.ViewHolder {
        public TextView textItemName;
        public TextView textItemQuantityUnit;
        public Button buttonDelete;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textItemName = itemView.findViewById(R.id.textItemName);
            textItemQuantityUnit = itemView.findViewById(R.id.textItemQuantityUnit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);

            itemView.setTag(this);
            itemView.setOnClickListener(mOnItemClickListener);
        }

        public TextView getTextItemName() {
            return textItemName;
        }

        public TextView getTextItemQuantityUnit() {
            return textItemQuantityUnit;
        }

        public Button getButtonDelete() {
            return buttonDelete;
        }
    }

    public PantryAdapter(ArrayList<PantryItem> arrayList, Context context) {
        pantryData = arrayList;
        parentContext = context;
    }

    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        mOnItemClickListener = itemClickListener;
    }

    public void setDelete(boolean b) {
        isDeleting = b;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pantry_item_row, parent, false);
        return new PantryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, final int position) {
        PantryItem item = pantryData.get(position);
        holder.getTextItemName().setText(item.getItemName());

        String qtyUnitStr = item.getQuantity() + " " + item.getUnit();

        // Check if expiring-soon alerts setting is enabled
        SharedPreferences prefs = parentContext.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        boolean alertsEnabled = prefs.getBoolean(SettingsActivity.KEY_EXPIRING_ALERTS, true);

        if (alertsEnabled && isExpiringSoon(item.getExpiryDate())) {
            // Highlight row with soft red background if expiring within 3 days
            holder.itemView.setBackgroundColor(0xFFFFEBEE);
            if (item.getExpiryDate() != null && !item.getExpiryDate().trim().isEmpty()) {
                qtyUnitStr += " (Expires: " + item.getExpiryDate() + ")";
            }
        } else {
            holder.itemView.setBackgroundColor(0x00000000);
        }

        holder.getTextItemQuantityUnit().setText(qtyUnitStr);

        if (isDeleting) {
            holder.getButtonDelete().setVisibility(View.VISIBLE);
            holder.getButtonDelete().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteItem(position);
                }
            });
        } else {
            holder.getButtonDelete().setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return pantryData.size();
    }

    private boolean isExpiringSoon(String expiryDateStr) {
        if (expiryDateStr == null || expiryDateStr.trim().isEmpty()) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date expiryDate = sdf.parse(expiryDateStr.trim());
            if (expiryDate == null) return false;

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar expiry = Calendar.getInstance();
            expiry.setTime(expiryDate);
            expiry.set(Calendar.HOUR_OF_DAY, 0);
            expiry.set(Calendar.MINUTE, 0);
            expiry.set(Calendar.SECOND, 0);
            expiry.set(Calendar.MILLISECOND, 0);

            long diffInMillis = expiry.getTimeInMillis() - today.getTimeInMillis();
            long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

            return diffInDays <= 3;
        } catch (Exception e) {
            return false;
        }
    }

    private void deleteItem(int position) {
        PantryItem pantryItem = pantryData.get(position);
        PantryDataSource ds = new PantryDataSource(parentContext);
        try {
            ds.open();
            boolean didDelete = ds.deletePantryItem(pantryItem.getPantryId());
            ds.close();

            if (didDelete) {
                pantryData.remove(position);
                notifyDataSetChanged();
                Toast.makeText(parentContext, pantryItem.getItemName() + " deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(parentContext, "Delete Failed!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(parentContext, "Delete Failed!", Toast.LENGTH_LONG).show();
        }
    }
}


