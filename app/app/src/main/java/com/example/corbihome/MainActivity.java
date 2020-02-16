package com.example.corbihome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    TabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // assign class variable to the tabs header
        tabs = findViewById(R.id.header_tabs);

        // load the first fragment
        loadFragment(new WelcomeFragment());

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    Log.i("MainActivity", "Tab selected, position: 0");
                    loadFragment(new WelcomeFragment());
                } else if (tab.getPosition() == 1) {
                    Log.i("MainActivity", "Tab selected, position: 1");
                    loadFragment(new CameraFragment());
                } else {
                    Log.i("MainActivity", "Tab selected, position: 2");
                    loadFragment(new MotionFragment());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // don't remove the fragment, just hide it from view
                Log.i("MainActivity", "Tab unselected, new position: " + tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.i("MainActivity", "Tab reselected, position: " + tab.getPosition());
            }
        });

        if (tabs.getSelectedTabPosition() == 1) {
            // camera tab
//            FragmentManager fm = getSupportFragmentManager();
//            MotionFragment fragment = (MotionFragment) fm.getFragment(savedInstanceState, "1");
//            fragment.updateMotionList();
            Log.i("MainActivity", "On tab 1");
        } else if (tabs.getSelectedTabPosition() == 2) {
            // motion tab
//            FragmentManager fm = getSupportFragmentManager();
//            CameraFragment fragment = (CameraFragment) fm.getFragment(savedInstanceState, "2");
            // update images
            Log.i("MainActivity", "On tab 2");
        }
    }


    private void loadFragment(Fragment fragment) {
        Log.i("MainActivity:loadFragment", "loading fragment");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_placeholder, fragment)
                .commit();
    }
}
