package com.stalker.geiger.omen.geiger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;

public class SplashActivity extends Activity {

    String TAG = "Splash_activity";
    private final int SPLASH_DISPLAY_TIMER = 3000;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sharedPref = getSharedPreferences(getString(R.string.sharedPrefFileName), MODE_PRIVATE);
                String stalkerName = sharedPref.getString(getString(R.string.stalkerName),"");
                Log.d(TAG, stalkerName);

                if (stalkerName != ""){
                    Intent mainIntent = new Intent(SplashActivity.this, StalkerActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
            }
        },SPLASH_DISPLAY_TIMER);
    }
}
