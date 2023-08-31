package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Home extends Fragment implements SensorEventListener {
    private PieModel pGoal, pCurrent;
    private PieChart pieChart;
    public static int totalStepsGoal = 0;
    private ProgressBar progressBar;
    private int todaysOffset, totalStart, goal, boot, totalDays, goalReach;
    private boolean showSteps = true;
    private TextView stepsView, totalView, averageView, calories, stepsLeft;
    private ImageView levels;
    public static NumberFormat format = NumberFormat.getInstance(Locale.getDefault());

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if(Build.VERSION.SDK_INT>=26){
            Wrapper.startForegroundService(getActivity(),new Intent(getActivity(), SensorListener.class));
        }
        else{
            getActivity().startService(new Intent(getActivity(), SensorListener.class));
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Database db = Database.getInstance(getActivity());
        todaysOffset = db.getSteps(Util.getToday());
        SharedPreferences pref = getActivity().getSharedPreferences("StepN", Context.MODE_PRIVATE);
        goal = pref.getInt("goal", (int) Settings.DEFAULT_GOAL);
        boot = db.getCurrentSteps();
        int pauseDifference = boot - pref.getInt("pauseCount", boot);
        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (sensor == null) { //comment this out for UI testing
            new AlertDialog.Builder(getActivity()).setTitle(R.string.sensor_not_found)
                    .setMessage(R.string.sensor_notice)
                    .setOnDismissListener(dialogInterface -> getActivity().finish()).setNeutralButton(android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss()).create().show();
        } else {
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
        }
        boot -= pauseDifference;
        totalStart = db.getTotalWithoutToday();
        totalDays = db.getDays();
        db.close();
        stepsDistanceChanges();
    }
    @Override
    public void onPause() {
        super.onPause();
        try{
            SensorManager sm=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Database db= Database.getInstance(getActivity());
        db.saveCurrentSteps(boot);
        db.close();
    }
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final  Bundle savedInstanceState) {
        final View view= inflater.inflate(R.layout.fragment_home,null);
        levels=view.findViewById(R.id.levels);
        stepsView=view.findViewById(R.id.pieSteps);
        totalView=view.findViewById(R.id.total);
        averageView=view.findViewById(R.id.average);
        progressBar=view.findViewById(R.id.progressBar);
        stepsLeft=view.findViewById(R.id.stepsLeft);
        calories=view.findViewById(R.id.caloriesBurned);
        pieChart=view.findViewById(R.id.chart);
        levels.setOnClickListener(v -> loadTotalStepsHomeFragment());
        setPieChart();
        pieChart.setOnClickListener(v -> {
            showSteps = !showSteps;
            stepsDistanceChanges();
        });
        return view;
    }
    private void stepsDistanceChanges() {
        if(showSteps){
            ((TextView) getView().findViewById(R.id.unit)).setText(getString(R.string.steps));

        }else{
            String unit=getActivity().getSharedPreferences("StepN",Context.MODE_PRIVATE)
                    .getString("step_size_unit", Settings.DEFAULT_STEP_UNIT);
            if(unit.equals("cm")){
                unit="km";
            }else{
                unit="mile";
            }
            ((TextView) getView().findViewById(R.id.unit)).setText(unit);
        }
        updatePie();
        updateBars();
    }
    private void updatePie() {
        int stepsToday = Math.max(todaysOffset + boot, 0);
        pCurrent.setValue(stepsToday);
        if(goal - stepsToday>0){
            if(pieChart.getData().size() == 1){
                pieChart.addPieSlice(pGoal);
            }
            pGoal.setValue(goal - stepsToday);}
        else{
            pieChart.clearChart();
            pieChart.addPieSlice(pCurrent);
        }
        pieChart.update();
        if(showSteps){
            stepsView.setText(format.format(stepsToday));
            double kcal = stepsToday * 0.04;
            calories.setText(format.format(kcal));
            totalView.setText(format.format(totalStart + stepsToday));
            averageView.setText(format.format((totalStart + stepsToday) / totalDays));
            totalStepsGoal = totalStart + stepsToday;
            if(totalStepsGoal < 3000){
                levels.setBackgroundColor(Color.GRAY);
                levels.setImageResource(R.drawable.threek);
                goalReach = 3000;
            }
            if(totalStepsGoal >= 3000 && totalStepsGoal < 7000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.threek);
                goalReach = 7000;
            }
            if(totalStepsGoal >= 7000 && totalStepsGoal < 10000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.sevenk);
                goalReach=10000;
            }
            if(totalStepsGoal >= 10000 && totalStepsGoal < 14000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.tenk);
                goalReach=14000;
            }
            if(totalStepsGoal >= 14000 && totalStepsGoal < 20000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.fourteenk);
                goalReach = 20000;
            }
            if(totalStepsGoal >= 20000 && totalStepsGoal < 30000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.twentyk);
                goalReach = 30000;
            }
            if(totalStepsGoal >= 30000 && totalStepsGoal < 40000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.thirtyk);
                goalReach = 40000;
            }
            if(totalStepsGoal >= 40000 && totalStepsGoal < 60000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.fourtyk);
                goalReach = 60000;
            }
            if(totalStepsGoal >= 60000 && totalStepsGoal < 70000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.sixtyk);
                goalReach = 700000;
            }
            float set=((totalStepsGoal * 100) / goalReach);
            int b = Math.round(set);
            stepsLeft.setText(format.format(goalReach - totalStepsGoal));
            progressBar.setProgress(b);
        }
        else{
            SharedPreferences pref = getActivity().getSharedPreferences("StepN",Context.MODE_PRIVATE);
            float stepSize = pref.getFloat("step_size_value", Settings.DEFAULT_STEP_SIZE);
            float distanceToday = stepsToday * stepSize;
            float distanceTotal = (stepsToday + totalStart) * stepSize;
            if(pref.getString("step_size_unit", Settings.DEFAULT_STEP_UNIT).equals("cm"))
            {
                distanceToday /= 100000;
                distanceTotal /= 100000;

            }else{
                distanceToday /= 5280;
                distanceTotal /= 5280;

            }
            stepsView.setText(format.format(distanceToday));
            totalView.setText(format.format(distanceTotal));
            averageView.setText(format.format(distanceTotal / totalDays));
            totalStepsGoal = totalStart + stepsToday;
            if(totalStepsGoal<3000){
                levels.setBackgroundColor(Color.GRAY);
                levels.setImageResource(R.drawable.threek);
                goalReach=3000;
            }
            if(totalStepsGoal>=3000 && totalStepsGoal<7000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.threek);
                goalReach=7000;
            }
            if(totalStepsGoal>=7000 && totalStepsGoal<10000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.sevenk);
                goalReach=10000;
            }
            if(totalStepsGoal>=10000 && totalStepsGoal<14000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.tenk);
                goalReach=14000;
            }
            if(totalStepsGoal>=14000 && totalStepsGoal<20000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.fourteenk);
                goalReach=20000;
            }
            if(totalStepsGoal>=20000 && totalStepsGoal<30000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.twentyk);
                goalReach=30000;
            }
            if(totalStepsGoal>=30000 && totalStepsGoal<40000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.thirtyk);
                goalReach=40000;
            }
            if(totalStepsGoal>=40000 && totalStepsGoal<60000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.fourtyk);
                goalReach=60000;
            }
            if(totalStepsGoal>=60000 && totalStepsGoal<70000){
                levels.setBackgroundColor(Color.BLUE);
                levels.setImageResource(R.drawable.fourtyk);
                goalReach=700000;
            }
            float set=((totalStepsGoal * 100) / goalReach);
            int b = Math.round(set);
            stepsLeft.setText(format.format(goalReach - totalStepsGoal));
            progressBar.setProgress(b);
        }
    }
    private void updateBars() {

        SimpleDateFormat df= new SimpleDateFormat("E", Locale.getDefault());
        BarChart barChart = getView().findViewById(R.id.barGraph);
        if(barChart.getData().size()>0) barChart.clearChart();
        int steps;
        float distance, stepSize = Settings.DEFAULT_STEP_SIZE;
        boolean stepSizeCm=true;
        if(!showSteps){
            SharedPreferences pref = getActivity().getSharedPreferences("StepN",Context.MODE_PRIVATE);
            stepSize = pref.getFloat("step_size_value", Settings.DEFAULT_STEP_SIZE);
            stepSizeCm = pref.getString("step_size_unit", Settings.DEFAULT_STEP_UNIT).equals("cm");}
        barChart.setShowDecimal(!showSteps);
        BarModel bm;
        Database db=Database.getInstance(getActivity());
        List<Pair<Long,Integer>> last = db.getLastEntries(8);
        db.close();
        for(int i=last.size()-1;i>0;i--) {
            Pair<Long, Integer> current = last.get(i);
            steps = current.second;
            if (steps > 0) {
                bm = new BarModel(df.format(new Date(current.first)), 0, steps > goal ? Color.parseColor("#47ED92") : Color.parseColor("#BEBEBE")); //Goal reached = blue else gray
                if (showSteps) {
                    bm.setValue(steps);
                } else {
                    distance = steps * stepSize;
                    if (stepSizeCm) {
                        distance /= 100000;
                    } else {
                        distance /= 5280;
                    }
                    distance = Math.round(distance * 1000) / 1000f;
                    bm.setValue(distance);
                }
                barChart.addBar(bm);
            }
        }
        if(barChart.getData().size()>0){
            barChart.setOnClickListener(v -> Statistics.getDialog(getActivity(), boot).show());
        }
    }
    private void setPieChart() {
        pCurrent = new PieModel(0, Color.parseColor("#47ED92"));
        pieChart.addPieSlice(pCurrent);
        pGoal = new PieModel(Settings.DEFAULT_GOAL, Color.parseColor("#BEBEBE"));
        pieChart.addPieSlice(pGoal);
        pieChart.setDrawValueInPie(false);
        pieChart.setUsePieRotation(true);
        pieChart.startAnimation();
    }
    private void loadTotalStepsHomeFragment() {
        Fragment newFragment=new TotalStepsHome();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment,newFragment)
                .commit();
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.values[0] > Integer.MAX_VALUE || event.values[0] == 0){
            todaysOffset = -(int) event.values[0];
            Database db= Database.getInstance(getActivity());
            db.insertNewDay(Util.getToday(),(int)event.values[0]);
            db.removeNegativeEntries();
            db.close();
        }
        boot = (int)event.values[0];
        updatePie();
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
