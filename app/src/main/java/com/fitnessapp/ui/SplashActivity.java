package com.fitnessapp.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.fitnessapp.Application;
import com.fitnessapp.R;
import com.fitnessapp.util.Constants;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //check user is logged in or not
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Application.getPreferenceBoolean(Constants.isLoggedIn)) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
            }
        }, 2000);
    }
}
