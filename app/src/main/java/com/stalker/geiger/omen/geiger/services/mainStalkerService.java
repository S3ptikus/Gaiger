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
import java.util.Random;

public class mainStalkerService extends IntentService {

    StalkerClass stalker;
    boolean isRunning;
    static final String TAG = mainStalkerService.class.getSimpleName();
    static final int loopTime = 1000;
    private wifiLogic wifiLogic;
    private NotificationManager nManager;
    private MediaPlayer player;
    private MediaPlayer deadPlayer;
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

        deadPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor descriptor = getAssets().openFd("Sound/phone-off-hook.mp3");
            deadPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            deadPlayer.prepare();
            deadPlayer.setLooping(true);
            deadPlayer.setVolume(1f, 1f);
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
        removeCmdCode();
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
                if (zone != null) {
                    if (!player.isPlaying())
                        player.start();
                }
                else {
                    if (player.isPlaying()) {
                        player.pause();
                    }
                }

                getRadHour = wifiLogic.getRadHour(zone);
                getRadHour = getRndRadCount(getRadHour);
                // Добавляем кол-во радов сталкеру. За одну секунду
                stalker.add_rad(getRadHour / 120);
                // Проверим, есть ли служебный код
                checkCode(sharedPref.getString(getString(R.string.cmdCodePrefKey),""));

                stalker.saveState(this);
                String formatRadString = new DecimalFormat(getString(R.string.RadFormat)).format(getRadHour);

                // Если сталкер уже мертв
                if (stalker.get_status() == StatusLife.DEAD) {
                    setNotification(getString(R.string.notificationDead), true);
                    if (player.isPlaying()) {
                        player.pause();
                    }
                    if (!deadPlayer.isPlaying()) {
                        deadPlayer.start();
                    }
                } else {
                    setNotification(formatRadString, (zone != null));
                    if (deadPlayer.isPlaying()) {
                        deadPlayer.pause();
                    }
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

    //    class serviceThread implements Runnable{
//        Context cntx;
//
//        public serviceThread(Context pCntx) {
//            super();
//            cntx = pCntx;
//        }
//
//        @Override
//        public void run() {
//            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPrefFileName), MODE_PRIVATE);
//            wifiLogic = new wifiLogic(cntx);
//            nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            String json = sharedPref.getString(getString(R.string.stalkerClass),"");
//            if (json != ""){
//                Gson g = new Gson();
//                stalker = g.fromJson(json, StalkerClass.class);
//                isRunning = true;
//                // запускаем главный таск
//                serviceTask();
//            } else {
//                isRunning = false;
//            }
//        }
//    }

 }
