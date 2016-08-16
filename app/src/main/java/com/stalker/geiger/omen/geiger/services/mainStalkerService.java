package com.stalker.geiger.omen.geiger.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.stalker.geiger.omen.geiger.R;
import com.stalker.geiger.omen.geiger.StalkerActivity;
import com.stalker.geiger.omen.geiger.common.checkCmdCode;
import com.stalker.geiger.omen.geiger.common.cmdCodeClass;
import com.stalker.geiger.omen.geiger.stalker_classes.StalkerClass;
import com.stalker.geiger.omen.geiger.stalker_classes.StatusLife;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class mainStalkerService extends IntentService {

    StalkerClass stalker;
    boolean isRunning;
    static final String TAG = mainStalkerService.class.getSimpleName();
    static final int loopTime = 1000;
    private wifiLogic wifiLogic;
    private NotificationManager nManager;
    MediaPlayerExt playerLowCount, playerCount, playerDeath;

    private final int NOTIFICATION_ID = 12345;
    NotificationCompat.Builder builder;
    private boolean isVibrate = false;
    public static final String RESPONSE_STRING_RAD_COUNT = "myResponseRadCount";
    private SharedPreferences sharedPref;
    private String cmdCode = "";

    public mainStalkerService() {
        super("mainStalkerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPref = getSharedPreferences(getString(R.string.sharedPrefFileName), MODE_PRIVATE);
        wifiLogic = new wifiLogic(this);
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);

        //playerLowCount = new MediaPlayerExt(getString(R.string.AssetsLowSndCount), true);
        playerCount = new MediaPlayerExt(getString(R.string.AssetsSndCount), true);
        playerDeath = new MediaPlayerExt(getString(R.string.AssetsSndDeath), false);

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
        removeCmdCode();
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void serviceTask(){
        Log.d(TAG, "mainTask");
        ArrayList<ScanResult> zones;
        double getRadHour;
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(StalkerActivity.MyStalkerReciever.PROCESS_RESPONSE_COUNT_RAD);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);

        while (isRunning){
            try {
                zones = wifiLogic.getWifiZone();
                getRadHour = wifiLogic.getRadHour(zones);
                getRadHour = getRndRadCount(getRadHour);
                // Добавляем кол-во радов сталкеру. За одну секунду
                stalker.add_rad(getRadHour / 120);
                // Проверим, есть ли служебный код
                checkCode(sharedPref.getString(getString(R.string.cmdCodePrefKey),""));

                stalker.saveState(this);
                String formatRadString = new DecimalFormat(getString(R.string.RadFormat)).format(getRadHour);

                // Если сталкер уже мертв
                if (stalker.get_status() == StatusLife.DEAD) {
                    setNotification(getString(R.string.notificationDead), false);
                    // в любом случае останавливаем звук счетчика
                    if (playerCount.isPlaying())
                        playerCount.pause();
                    if (!playerDeath.isPlaying()) {
                        playerDeath.seekTo(0);
                        playerDeath.start();
                    }
                } else {
                    playerDeath.pause();
                    if (!zones.isEmpty()) {
                        if (!playerCount.isPlaying())
                            playerCount.start();
                    }
                    else {
                        if (playerCount.isPlaying())
                            playerCount.pause();
                    }
                    setNotification(formatRadString, (!zones.isEmpty()));
                }
                // Отправляем в активити
                broadcastIntent.putExtra(RESPONSE_STRING_RAD_COUNT, formatRadString);
                sendBroadcast(broadcastIntent);

                Thread.sleep(loopTime);
            }catch (InterruptedException e){
                Log.d(TAG,e.getMessage());
            }
        }
    }

    private void checkCode(String pCode){
        if (pCode.isEmpty())
            return;

        stalker.applyCmdCode(checkCmdCode.getCmdObj(pCode));
        removeCmdCode();
    }

    private void removeCmdCode(){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(getString(R.string.cmdCodePrefKey));
        editor.commit();
    }

    private double getRndRadCount(double pCount){
        Random rand = new Random();
        double randomNum = rand.nextInt((50) + 1)/100f;
        if (System.nanoTime()%2 == 0)
            return Math.abs(pCount + randomNum);
         else
            return Math.abs(pCount - randomNum);
    }

    private void setNotification(String pMsg, boolean pVibrate){
        builder.setSmallIcon(R.mipmap.ic_stalker_launcher)
                .setContentTitle(getString(R.string.RadLevel))
                .setAutoCancel(true)
                .setContentText(pMsg + getString(R.string.RadHour));

        if (pVibrate){
            builder.setVibrate(new long[]{100, 100, 100});
        }else {
            builder.setVibrate(null);
        }

        Intent targetIntent = new Intent(this, StalkerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        nManager.notify(NOTIFICATION_ID, builder.build());
    }

    private class MediaPlayerExt extends MediaPlayer{
        public MediaPlayerExt(String pAssetsUri, boolean pLoop) {
            super();
            try {
                AssetFileDescriptor descriptor = getAssets().openFd(pAssetsUri);
                this.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();
                this.prepareAsync();
                this.setLooping(pLoop);
                this.setVolume(1f, 1f);
            }catch (IOException e){
                Log.d(TAG, e.getMessage());
            }
        }

        @Override
        public void start() throws IllegalStateException {
            super.start();
        }

        @Override
        public void setOnCompletionListener(OnCompletionListener listener) {
            super.setOnCompletionListener(listener);
            Log.d(TAG, "Complite media loop");


        }
    }

 }
