package com.example.welcome_screen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.welcome_screen.databinding.ActivityMainBinding;
import com.example.welcome_screen.databinding.BottomNavBarBinding;

public class FragmentMain extends AppCompatActivity {

    BottomNavBarBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = BottomNavBarBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());
        Intent intent = getIntent();

        binding.bottomNavigationView2.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            final int homeId = R.id.home;
            final int profileId = R.id.workout;
            final int settingsId = R.id.settings;
            final int notificationsId = R.id.notification;
            if (itemId == homeId) {
                replaceFragment(new HomeFragment());
            } else if (itemId == profileId) {
                replaceFragment(new WorkoutFragment());
            } else if (itemId == settingsId)
                replaceFragment(new SettingsFragment());
            else
                replaceFragment(new NotificationsFragment());

            return true;
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void exerciseSelected(View view) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (fragment instanceof WorkoutFragment) {
            ((WorkoutFragment) fragment).exerciseSelected(view);
        }
    }





}

