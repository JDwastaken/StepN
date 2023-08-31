package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.util.Pair;
import android.view.Window;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

abstract class Statistics {
    public static Dialog getDialog(final Context c, int since_boot){
        final Dialog d = new Dialog(c);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.statistics);
        d.findViewById(R.id.close).setOnClickListener(v -> d.dismiss());
        Database db= Database.getInstance(c);
        Pair<Date,Integer> record= db.getRecordData();
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(Util.getToday());
        int daysThisMonth = date.get(Calendar.DAY_OF_MONTH);
        date.add(Calendar.DATE,-6);
        int thisWeek = db.getSteps(date.getTimeInMillis(),System.currentTimeMillis())+since_boot;
        date.setTimeInMillis(Util.getToday());
        date.set(Calendar.DAY_OF_MONTH, 1);
        int thisMonth = db.getSteps(date.getTimeInMillis(),System.currentTimeMillis())+since_boot;
        ((TextView) d.findViewById(R.id.record)).setText(Home.format.format(record.second) + " " + java.text.DateFormat.getDateInstance().format(record.first));
        ((TextView) d.findViewById(R.id.totalThisWeek)).setText(Home.format.format(thisWeek));
        ((TextView) d.findViewById(R.id.totalThisMonth)).setText(Home.format.format(thisMonth));
        ((TextView) d.findViewById(R.id.averageThisWeek)).setText(Home.format.format(thisWeek/7));
        ((TextView) d.findViewById(R.id.averageThisMonth)).setText(Home.format.format(thisMonth/daysThisMonth));
        db.close();
        return d;
    }
}