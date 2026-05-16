package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StaffDashboardAdapter extends RecyclerView.Adapter<StaffDashboardAdapter.ViewHolder> {

    private List<StaffModel> staffList;

    public StaffDashboardAdapter(List<StaffModel> staffList) {
        this.staffList = staffList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_dashboard, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        StaffModel staff = staffList.get(position);

        // Name & Role

        holder.tvName.setText(staff.name);

        holder.tvRole.setText(staff.role);

        // Status

        if (staff.isLoggedIn) {

            holder.tvStatus.setText("Inside");

            holder.tvStatus.setBackgroundColor(
                    Color.parseColor("#2E7D32")
            );

        } else {

            holder.tvStatus.setText("Outside");

            holder.tvStatus.setBackgroundColor(
                    Color.parseColor("#C62828")
            );
        }

        // Attendance

        holder.tvLoginTime.setText(
                "Login: " + formatTime(staff.loginTime)
        );

        if (staff.logoutTime > 0) {

            holder.tvLogoutTime.setText(
                    "Logout: " + formatTime(staff.logoutTime)
            );

        } else {

            holder.tvLogoutTime.setText(
                    "Logout: Still Active"
            );
        }

        holder.tvWorkedHours.setText(
                "Worked: " + staff.durationMinutes + " mins"
        );

        // CLICK EVENT → HISTORY SCREEN

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    holder.itemView.getContext(),
                    StaffHistoryActivity.class
            );

            intent.putExtra("staffId", staff.id);

            holder.itemView.getContext()
                    .startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    // FORMAT TIME

    private String formatTime(long millis) {

        if (millis == 0) return "--";

        return new SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
        ).format(new Date(millis));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvRole;
        TextView tvStatus;

        TextView tvLoginTime;
        TextView tvLogoutTime;
        TextView tvWorkedHours;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName =
                    itemView.findViewById(R.id.tvName);

            tvRole =
                    itemView.findViewById(R.id.tvRole);

            tvStatus =
                    itemView.findViewById(R.id.tvStatus);

            tvLoginTime =
                    itemView.findViewById(R.id.tvLoginTime);

            tvLogoutTime =
                    itemView.findViewById(R.id.tvLogoutTime);

            tvWorkedHours =
                    itemView.findViewById(R.id.tvWorkedHours);
        }
    }
}