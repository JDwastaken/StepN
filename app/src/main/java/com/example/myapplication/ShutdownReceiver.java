package com.example.myapplication;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShutdownReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        context.startService(new Intent(context, SensorListener.class));
        context.getSharedPreferences("StepN", Context.MODE_PRIVATE).edit()
                .putBoolean("correctShutdown", true).apply();
        Database db = Database.getInstance(context);
        if (db.getSteps(Util.getToday()) == Integer.MIN_VALUE) {
            int steps = db.getCurrentSteps();
            db.insertNewDay(Util.getToday(), steps);
        } else {
            db.addToLastEntry(db.getCurrentSteps());
        }
        db.close();
    }

}