package com.stalker.geiger.omen.geiger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity implements View.OnClickListener {

    String TAG = "LOGIN_ACTIVITY";
    SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String name = ((EditText) findViewById(R.id.editName)).getText().toString();
        sharedPref = getSharedPreferences(getString(R.string.sharedPrefFileName), MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPref.edit();
        ed.putString(getString(R.string.stalkerName), name);
        ed.commit();

        // Open Stalker Activity
        Intent mainIntent = new Intent(LoginActivity.this, StalkerActivity.class);
        mainIntent.putExtra(getString(R.string.stalkerName),name);
        startActivity(mainIntent);
        finish();

        Log.d(TAG, sharedPref.getString(getString(R.string.stalkerName),""));
    }
}
