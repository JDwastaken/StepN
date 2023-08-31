package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.Locale;


public class Settings extends Fragment implements AdapterView.OnItemSelectedListener {
    public static final float DEFAULT_STEP_SIZE = Locale.getDefault() == Locale.US ? 2.5f : 75f;
    public static final String DEFAULT_STEP_UNIT =Locale.getDefault() == Locale.US ? "ft" : "cm" ;
    public static final float DEFAULT_GOAL = 1000;
    private TextView goal;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_settings,null);
        LinearLayout setGoal = view.findViewById(R.id.setGoalLayout);
        LinearLayout stepSize = view.findViewById(R.id.stepSizeLayout);
        LinearLayout showNotification = view.findViewById(R.id.notificationLayout);
        goal = view.findViewById(R.id.goalText);
        setGoal.setOnClickListener(v -> loadSetGoal());
        stepSize.setOnClickListener(v -> {
            final SharedPreferences pref = getActivity().getSharedPreferences("StepN", Context.MODE_PRIVATE);
            v= getActivity().getLayoutInflater().inflate(R.layout.step_size, null);
            final AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
            final RadioGroup unit =v.findViewById(R.id.unit);
            final EditText value=v.findViewById(R.id.value);
            unit.check(pref.getString("step_size_unit",DEFAULT_STEP_UNIT).equals("cm") ? R.id.cm : R.id.ft);
            value.setText(String.valueOf(pref.getFloat("step_size_value",DEFAULT_STEP_SIZE)));
            builder.setView(v);
            builder.setTitle(R.string.set_step_size);
            builder.setPositiveButton("OK", (dialog, which) -> {
                try{
                    pref.edit().putFloat("step_size_value", Float.parseFloat(value.getText().toString())).putString("step_size_unit",unit.getCheckedRadioButtonId()==R.id.cm ? "cm" : "ft").apply();
                    value.setText(getString(R.string.step_size_summary, Float.valueOf(value.getText().toString()), unit.getCheckedRadioButtonId()==R.id.cm ? "cm" : "ft"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            });
            builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
            Dialog dialog = builder.create();
            dialog.show();
        });
        showNotification.setOnClickListener(v -> loadNotification());
        return view;
    }
    private void loadNotification() {
        NotificationManager manager=(NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        if(check()){
            manager.notify(SensorListener.NOTIFICATION_ID,SensorListener.getNotification(getActivity()));
        }else {
            manager.cancel(SensorListener.NOTIFICATION_ID);
        }
    }
    private Boolean check() {
        Wrapper.launchNotificationSettings(getActivity());
        return true;
    }
    private void loadSetGoal() {
        final SharedPreferences pref = getActivity().getSharedPreferences("StepN", Context.MODE_PRIVATE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final NumberPicker np = new NumberPicker(getActivity());
        np.setMinValue(1);
        np.setMaxValue(100000);
        np.setValue(pref.getInt("goal",1000));
        builder.setView(np);
        builder.setTitle(R.string.set_goal);
        builder.setPositiveButton("OK", (dialog, which) -> {
            np.clearFocus();
            pref.edit().putInt("goal", np.getValue()).apply();
            goal.setText(getString(R.string.goal_summary, np.getValue()));
            dialog.dismiss();
            getActivity().startService(new Intent(getActivity(), SensorListener.class).putExtra("updateNotificationState", true));
        });
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}