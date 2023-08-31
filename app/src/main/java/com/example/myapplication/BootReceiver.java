package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        SharedPreferences pref = context.getSharedPreferences("StepN", Context.MODE_PRIVATE);
        Database db = Database.getInstance(context);
        if (!pref.getBoolean("correctShutdown", false)) {
            int steps = Math.max(0, db.getCurrentSteps());
            db.addToLastEntry(steps);
        }
        db.removeNegativeEntries();
        db.saveCurrentSteps(0);
        db.close();
        pref.edit().remove("correctShutdown").apply();
        if (Build.VERSION.SDK_INT >= 26) {
            Wrapper.startForegroundService(context, new Intent(context, SensorListener.class));
        } else {
            context.startService(new Intent(context, SensorListener.class));
        }
    }
}