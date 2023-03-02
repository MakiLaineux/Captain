package com.technoprimates.captain;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.technoprimates.captain.databinding.ActivityMainBinding;

import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // fingerprint support is required for this app, otherwise quit the app
        if (!checkBiometricSupport()) {
            this.finishAffinity();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    /**
     * Checks if fingerprint authentication is possible with the current device and configuration.
     * This require lockscreen security to be enabled, USE_BIOMETRIC permission to be granted,
     * and fingerprint feature to be available on the device.
     */
    private boolean checkBiometricSupport() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        PackageManager packageManager = this.getPackageManager();

        if (!keyguardManager.isKeyguardSecure()) {
            Toast.makeText(this, R.string.toast_lockscreen_not_enabled, Toast.LENGTH_LONG).show();
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) !=
                PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.toast_fingerprint_permission_not_granted, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            Toast.makeText(this, R.string.toast_fingerprint_not_supported, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}