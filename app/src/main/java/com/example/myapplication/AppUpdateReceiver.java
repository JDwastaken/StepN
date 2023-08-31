package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AppUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Build.VERSION.SDK_INT>=26){
            Wrapper.startForegroundService(context,new Intent(context,SensorListener.class));
        }
        else{
            context.startService(new Intent(context,SensorListener.class));
        }
    }
}