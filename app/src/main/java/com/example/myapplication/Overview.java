package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.NumberFormat;
import java.util.Locale;

public class Overview extends Fragment {
    public static NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
    private StepDataViewModel stepDataViewModel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stepDataViewModel = new ViewModelProvider(requireActivity()).get(StepDataViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_overview,null);
        LinearLayout achievement = view.findViewById(R.id.achievement);
        achievement.setOnClickListener(v -> loadAchievementFragment());
        TextView totalCalories = view.findViewById(R.id.caloriesTotal);
        TextView totalSteps = view.findViewById(R.id.totalStep);
        TextView totalDistance = view.findViewById(R.id.distanceTotal);
        Database db = Database.getInstance(getActivity());
        int totalStepCount = stepDataViewModel.getTotalSteps();
        int totalStart = db.getTotalWithoutToday();
        totalSteps.setText(format.format(totalStepCount + totalStart));
        double kcal = (totalStepCount + totalStart)*0.04;
        totalCalories.setText(format.format(kcal));
        SharedPreferences pref = getActivity().getSharedPreferences("StepN", Context.MODE_PRIVATE);
        float stepSize=pref.getFloat("step_size_value", Settings.DEFAULT_STEP_SIZE);
        float distanceTotal = (totalStepCount + totalStart)*stepSize;
        String unit;
        if(pref.getString("step_size_unit", Settings.DEFAULT_STEP_UNIT).equals("cm"))
        {
            distanceTotal/=100000;
            unit ="km";
        }else{
            distanceTotal /= 5280;
            unit ="mile";
        }
        totalDistance.setText(format.format(distanceTotal)+" "+ unit);
        return view;
    }
    private void loadAchievementFragment(){
        Fragment newFragment=new Achievement();
        getParentFragmentManager().beginTransaction().replace(R.id.fragment,newFragment).commit();
    }
}