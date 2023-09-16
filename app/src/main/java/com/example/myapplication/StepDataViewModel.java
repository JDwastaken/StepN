package com.example.myapplication;

import androidx.lifecycle.ViewModel;

public class StepDataViewModel extends ViewModel {
    private int totalSteps;

    public int getTotalSteps(){
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps){
        this.totalSteps = totalSteps;
    }
}
