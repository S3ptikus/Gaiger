package com.stalker.geiger.omen.geiger.services;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by omen on 21.07.2016.
 */
public class wifiLogic {

    private WifiManager wifi;
    wifiApControl apControl;
    private Context cntx;
    private static final double homeRad = 0.5;
    private static final String TAG = wifiLogic.class.getSimpleName();

    public wifiLogic(Context pCntx) {
        wifi = (WifiManager) pCntx.getSystemService(Context.WIFI_SERVICE);
        apControl = wifiApControl.getApControl(wifi);
        checkWiFiState();
    }

    public ArrayList<ScanResult> getWifiZone(){
        ArrayList<ScanResult> resList = new ArrayList<ScanResult>();
        try {
            checkWiFiState();
            if (wifi.startScan()){
                for (ScanResult zone : wifi.getScanResults()) {
                    if (checkZone(zone)) {
                        resList.add(zone);
                    }
                }
            }
        }catch(NullPointerException e){
            return null;
        }
        return resList;
    }

    private void checkWiFiState(){
        if (apControl != null && apControl.isWifiApEnabled()) {
                apControl.setWifiApEnabled(apControl.getWifiApConfiguration(), false);
        }

        if (wifi.isWifiEnabled() == false)
            wifi.setWifiEnabled(true);
    }

    private boolean checkZone(ScanResult pZone){
        // название зоны, это целое число, если нет, то это не она
        Integer nameZone;
        int resCount = 0;
        try {
            nameZone = Integer.parseInt(pZone.SSID);
        }catch (NumberFormatException e){
            nameZone = -1;
        }
        if ((nameZone != -1) && (nameZone.toString().length() > 6)){
            char[] listNumber = String.valueOf(nameZone).substring(0,6).toCharArray();
            for (char item: listNumber) {
                resCount += Integer.parseInt(String.valueOf(item));
            }
            // если первый 6 цифр в сумме дают 24, то это наша сеть
            if (resCount == 24)
                return true;
            else
                return false;
        }
            return false;
    }

    public static int getLevel(ScanResult pZone){
        // если частота 5ghz
        if (pZone.frequency > 3000){
            return Math.abs(50 - Math.abs(pZone.level));
        } else { // частота 2Ghz
            return Math.abs(40 - Math.abs(pZone.level));
        }
    }

    public static int getPowZone(String pSSID){
        try {
            return Integer.parseInt(pSSID.substring(6));
        }catch (NumberFormatException e) {
            return 0;
        }
    }

    public static double getRadHour(ArrayList<ScanResult> pZones){
        if (pZones.isEmpty()) {
            return homeRad;
        } else {
            int rad = 0;
            int lvl, pwr;
            for (ScanResult zone: pZones) {
                lvl = Math.abs(zone.level);
                pwr = getPowZone(zone.SSID);
                Log.d(TAG, "fiend SSID:" + zone.SSID + " Level:" + zone.level);
                if (lvl < 40) {
                    rad += (pwr * 1);
                } else if ((lvl >= 40) && (lvl <= 50)) {
                    rad += (pwr * 1);
                } else if ((lvl >= 50) && (lvl <= 60)) {
                    rad += (pwr * 0.9);
                } else if ((lvl >= 60) && (lvl <= 75)) {
                    rad += (pwr * 0.75);
                } else {
                    rad += (pwr * 0.1);
                }
            }
            return rad;
        }
    }

}
