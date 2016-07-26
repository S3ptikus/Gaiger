package com.stalker.geiger.omen.geiger;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.google.gson.Gson;
import com.stalker.geiger.omen.geiger.services.mainStalkerService;
import com.stalker.geiger.omen.geiger.stalker_classes.StalkerClass;

import java.text.DecimalFormat;

public class StalkerActivity extends AppCompatActivity {

    private StalkerClass stalker;
    private String TAG = StalkerActivity.class.getSimpleName();
    private SharedPreferences sharedPref;

    TextView textViewStalkerName;
    TextView textViewStatusLife;
    TextView textViewRadCount;
    TextView textViewStalkerRad;
    ProgressBar progressBarLife;

    private MyStalkerReciever receiver;
    private IntentFilter filter;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        this.unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        registerReceiver(receiver, filter);
        super.onPostResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // test
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stalker);
        sharedPref = getSharedPreferences(getString(R.string.sharedPrefFileName), MODE_PRIVATE);

        // Получаем компоненты
        textViewStalkerName = (TextView) findViewById(R.id.textViewStalkerName);
        textViewStatusLife = (TextView) findViewById(R.id.textViewStatus);
        textViewRadCount = (TextView) findViewById(R.id.textViewRadCount);
        progressBarLife = (ProgressBar) findViewById(R.id.lifeBar);
        textViewStalkerRad = (TextView) findViewById(R.id.countStalkerRad);

        // Регистрируем ресивер
        filter = new IntentFilter(MyStalkerReciever.PROCESS_RESPONSE_COUNT_RAD);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyStalkerReciever(this);
        registerReceiver(receiver, filter);

        String json = sharedPref.getString(getString(R.string.stalkerClass),"");
        // если первый раз, то класса не будет
        if (json == ""){
            String name = getIntent().getStringExtra(getString(R.string.stalkerName));
            stalker = new StalkerClass(name);
        } else {
            Gson g = new Gson();
            stalker = g.fromJson(json, StalkerClass.class);
        }

        // запускаю таск на сохранение данных
        if (isMyServiceRunning(mainStalkerService.class)){
            Log.d(TAG,"Service is running");
        }else {
            Intent intent = new Intent(StalkerActivity.this, mainStalkerService.class);
            startService(intent);
        }

        updateStalkerInfo();
        // Сохраняем состояние объекта
        stalker.saveState(this);
    }

    public void updateStalkerInfo(){
        textViewStalkerName.setText(stalker.get_name());
        progressBarLife.setProgress(stalker.get_countRadForProgressbar());
        textViewStatusLife.setText(stalker.get_status(this));
        textViewStalkerRad.setText(new DecimalFormat("000.00").format(stalker.get_countRad()) + " рад");
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item3 = menu.add("Остановить сервис и сбросить настройки");
        item3.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPrefFileName), MODE_PRIVATE);
                SharedPreferences.Editor ed = sharedPref.edit();
                ed.clear();
                ed.commit();

                Intent intent = new Intent(StalkerActivity.this, mainStalkerService.class);
                stopService(intent);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public class MyStalkerReciever extends BroadcastReceiver{

        private Context cntx;

        public static final String PROCESS_RESPONSE_COUNT_RAD = "com.stalker.geiger.omen.geiger.intent.action.PROCESS_RESPONSE_COUNT_RAD";

        public MyStalkerReciever(Context pCtnx) {
            super();
            cntx = pCtnx;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra(mainStalkerService.RESPONSE_STRING_RAD_COUNT);
            TextView myTextView = (TextView) findViewById(R.id.textViewRadCount);
            myTextView.setText(responseString);

            // прочитать состояние из файла и выдать его на экране
            stalker = StalkerClass.getStalkerState(cntx);

            updateStalkerInfo();

            //progressBarLife.setProgress(Integer.decode(responseString.split(":")[1]));
            //textViewStalkerRad.setText((Integer.decode(responseString.split(":")[1]) * 4) + " рад");

        }


    }
}
