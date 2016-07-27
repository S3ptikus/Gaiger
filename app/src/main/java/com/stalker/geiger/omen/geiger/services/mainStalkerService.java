package com.stalker.geiger.omen.geiger.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.stalker.geiger.omen.geiger.R;
import com.stalker.geiger.omen.geiger.StalkerActivity;
import com.stalker.geiger.omen.geiger.stalker_classes.StalkerClass;

import java.io.File;
import java.io.IOException;
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
    private wifiLogic wifiLogic;
    private NotificationManager nManager;
    private MediaPlayer player;
    private final int NOTIFICATION_ID = 12345;
    private boolean isVibrate = false;
    public static final String RESPONSE_STRING_RAD_COUNT = "myResponseRadCount";
    private SharedPreferences sharedPref;

    public mainStalkerService() {
        super("mainStalkerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPref = getSharedPreferences(getString(R.string.sharedPrefFileName), MODE_PRIVATE);
        wifiLogic = new wifiLogic(this);
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        player = new MediaPlayer();
        try {

            AssetFileDescriptor descriptor = getAssets().openFd("Sound/soundCount.mp3");
            player.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            player.prepare();
            player.setLooping(true);
            player.setVolume(1f, 1f);
        }catch (IOException e){
            Log.d(TAG, e.getMessage());
        }

        String json = sharedPref.getString(getString(R.string.stalkerClass),"");
        if (json != ""){
            Gson g = new Gson();
            stalker = g.fromJson(json, StalkerClass.class);
            isRunning = true;
            // запускаем главный таск
            serviceTask();
        } else {
            isRunning = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //new Thread(new serviceThread(this)).run();
        //return START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    private void serviceTask(){
        Log.d(TAG, "mainTask");
        ScanResult zone;
        double getRadHour;
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(StalkerActivity.MyStalkerReciever.PROCESS_RESPONSE_COUNT_RAD);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);

        while (isRunning){
            try {
                zone = wifiLogic.getWifiZone();
                // добавление звука\вибрации при входе в зону
                if (zone != null)
                    if (!player.isPlaying())
                        player.start();
                else
                    player.stop();
                isVibrate = (zone != null);

                getRadHour = wifiLogic.getRadHour(zone);
                getRadHour = getRndRadCount(getRadHour);
                // Добавляем кол-во радов сталкеру. За одну секунду
                stalker.add_rad(getRadHour / 120);
                stalker.saveState(this);
                String formatRadString = new DecimalFormat(getString(R.string.RadFormat)).format(getRadHour);
                setNotification(formatRadString);
                // Отправляем в активити
                broadcastIntent.putExtra(RESPONSE_STRING_RAD_COUNT, formatRadString);
                sendBroadcast(broadcastIntent);

                Thread.sleep(loopTime);
            }catch (InterruptedException e){
                Log.d(TAG,e.getMessage());
            }
        }
    }

    private double getRndRadCount(double pCount){
        Random rand = new Random();
        double randomNum = rand.nextInt((50) + 1)/100f;
        if (System.nanoTime()%2 == 0)
            return Math.abs(pCount + randomNum);
         else
            return Math.abs(pCount - randomNum);
    }

    private void setNotification(String pMsg){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_stalker_launcher)
                        .setContentTitle(getString(R.string.RadLevel))
                        .setAutoCancel(true)
                        .setContentText(pMsg + getString(R.string.RadHour));

        if (isVibrate){
            builder.setVibrate(new long[]{100, 100, 100});
        }

        Intent targetIntent = new Intent(this, StalkerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        nManager.notify(NOTIFICATION_ID, builder.build());
    }

    class serviceThread implements Runnable{
        Context cntx;

        public serviceThread(Context pCntx) {
            super();
            cntx = pCntx;
        }

        @Override
        public void run() {
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPrefFileName), MODE_PRIVATE);
            wifiLogic = new wifiLogic(cntx);
            nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String json = sharedPref.getString(getString(R.string.stalkerClass),"");
            if (json != ""){
                Gson g = new Gson();
                stalker = g.fromJson(json, StalkerClass.class);
                isRunning = true;
                // запускаем главный таск
                serviceTask();
            } else {
                isRunning = false;
            }
        }
    }

 }
