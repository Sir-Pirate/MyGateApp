package com.example.myapplication;

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

public class StaffHistoryAdapter extends RecyclerView.Adapter<StaffHistoryAdapter.ViewHolder> {

    private List<AttendanceLogModel> historyList;

    public StaffHistoryAdapter(List<AttendanceLogModel> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_history, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        AttendanceLogModel log = historyList.get(position);

        holder.tvDate.setText(
                formatDate(log.loginTime)
        );

        holder.tvLogin.setText(
                "Login: " + formatTime(log.loginTime)
        );

        if (log.logoutTime > 0) {

            holder.tvLogout.setText(
                    "Logout: " + formatTime(log.logoutTime)
            );

        } else {

            holder.tvLogout.setText(
                    "Logout: Still Active"
            );
        }

        holder.tvWorked.setText(
                "Worked: " + log.durationMinutes + " mins"
        );
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    // FORMAT DATE

    private String formatDate(long millis) {

        return new SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
        ).format(new Date(millis));
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

        TextView tvDate, tvLogin, tvLogout, tvWorked;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tvDate);
            tvLogin = itemView.findViewById(R.id.tvLogin);
            tvLogout = itemView.findViewById(R.id.tvLogout);
            tvWorked = itemView.findViewById(R.id.tvWorked);
        }
    }
}