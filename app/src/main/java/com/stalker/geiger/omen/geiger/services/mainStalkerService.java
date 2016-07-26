package com.stalker.geiger.omen.geiger.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.stalker.geiger.omen.geiger.R;
import com.stalker.geiger.omen.geiger.StalkerActivity;
import com.stalker.geiger.omen.geiger.stalker_classes.StalkerClass;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Random;

public class mainStalkerService extends IntentService {

    StalkerClass stalker;
    boolean isRunning;
    static final String TAG = mainStalkerService.class.getSimpleName();
    static final int loopTime = 1000;
    static final String formatRad = "000.000";
    private boolean isStalkerChange = false;
    private wifiLogic wifiLogic;
    private NotificationManager nManager;
    private final int NOTIFICATION_ID = 12345;

    public static final String RESPONSE_STRING_RAD_COUNT = "myResponseRadCount";
    public static final String RESPONSE_STRING_STALKER_RAD_COUNT = "myResponseStalkerRadCount";

    public mainStalkerService() {
        super("mainStalkerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPrefFileName), MODE_PRIVATE);
        wifiLogic = new wifiLogic(this);
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String json = sharedPref.getString(getString(R.string.stalkerClass),"");
        if (json != ""){
            Gson g = new Gson();
            stalker = g.fromJson(json, StalkerClass.class);
            isRunning = true;
            // запускаем главный таск
            mainTask();
        } else {
            isRunning = false;
            return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void mainTask(){
        Log.d(TAG, "mainTask");
        ScanResult zone;
        double getRadHour = 0;
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(StalkerActivity.MyStalkerReciever.PROCESS_RESPONSE_COUNT_RAD);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);

        while (isRunning){
            try {
                //TODO Wifi block and RAD count
                zone = wifiLogic.getWifiZone();

                getRadHour = wifiLogic.getRadHour(zone);

//                if (zone != null){
//                    // Уведомление
//                    abstractDist = wifiLogic.getLevel(zone);
//
//
//                    isStalkerChange = true;
//                }else {
//                    getRadHour = 0;
//                    isStalkerChange = false;
//                    clearNotification();
//                }
                getRadHour = getRndRadCount(getRadHour);
                // Добавляем кол-во радов сталкеру. За одну секунду
                stalker.add_rad(getRadHour / 120);
                stalker.saveState(this);
                String formatRadString = new DecimalFormat(formatRad).format(getRadHour);
                setNotification("RadCount - " + formatRadString);
                // Отправляем в активити

                broadcastIntent.putExtra(RESPONSE_STRING_RAD_COUNT, formatRadString + ":" + stalker.get_countRadForProgressbar());
                sendBroadcast(broadcastIntent);

                Thread.sleep(loopTime);
            }catch (InterruptedException e){
                Log.d(TAG,e.getMessage());
            }
        }
    }

    private double getRndRadCount(double pCount){
        Random rand = new Random();
        double randomNum = rand.nextInt((50 - 0) + 1)/100f;
        if (System.nanoTime()%2 == 0)
            return Math.abs(pCount + randomNum);
            //return new DecimalFormat(formatRad).format (Math.abs(pCount + randomNum));
         else
            return Math.abs(pCount - randomNum);
            //return new DecimalFormat(formatRad).format (Math.abs(pCount - randomNum));
    }

    private void setNotification(String pMsg){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Уровень радиации")
                        //.setVibrate(new long[] {100,100})
                        .setContentText(pMsg);
        Intent targetIntent = new Intent(this, StalkerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        nManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void clearNotification(){
        nManager.cancel(NOTIFICATION_ID);
    }

}
