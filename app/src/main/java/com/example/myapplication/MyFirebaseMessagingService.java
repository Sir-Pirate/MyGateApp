package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
    }

        NotificationManager manager =
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    NotificationManager.IMPORTANCE_HIGH
            );

        }
    }
}