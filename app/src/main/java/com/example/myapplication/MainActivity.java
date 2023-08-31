package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;

public class MainActivity extends FragmentActivity {
    AHBottomNavigation bottomNavigation;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitymain);
        initiateViews();
        setupBottomNavigation();
        defaultNav();
        loadHomeFragment();
        bottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
            if (position==0){
                loadOverviewFragment();
            }
            if (position==1){
                loadHomeFragment();
            }
            if (position==2){
                loadSettingsFragment();
            }
            return true;
        });
    }
    private void initiateViews(){
        bottomNavigation=findViewById(R.id.bottom_navigation);
    }
    private void setupBottomNavigation(){
        AHBottomNavigationItem itemOne = new AHBottomNavigationItem(R.string.tab_1,R.drawable.overview_icon,R.color.item_selected);
        AHBottomNavigationItem itemTwo = new AHBottomNavigationItem(R.string.tab_2,R.drawable.home_icon,R.color.item_selected);
        AHBottomNavigationItem itemThree = new AHBottomNavigationItem(R.string.tab_3,R.drawable.settings_icon,R.color.item_selected);
        bottomNavigation.addItem(itemOne);
        bottomNavigation.addItem(itemTwo);
        bottomNavigation.addItem(itemThree);
    }
    private void defaultNav(){
        bottomNavigation.setCurrentItem(1);
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.setAccentColor(Color.parseColor("#2979FF"));
        bottomNavigation.setInactiveColor(Color.parseColor("#747474"));
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
    }
    private void loadHomeFragment(){
        Fragment newFragment = new Home();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, newFragment)
                .commit();
    }
    private void loadOverviewFragment(){
        Fragment newFragment = new Overview();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, newFragment)
                .commit();
    }
    private void loadSettingsFragment(){
        Fragment newFragment = new Settings();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, newFragment)
                .commit();
    }
}
