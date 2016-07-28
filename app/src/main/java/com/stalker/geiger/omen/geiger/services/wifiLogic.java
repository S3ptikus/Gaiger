package com.stalker.geiger.omen.geiger.services;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by omen on 21.07.2016.
 */
public class wifiLogic {

    private WifiManager wifi;
    private Context cntx;
    private static final double homeRad = 3;
    private static final String TAG = wifiLogic.class.getSimpleName();

    public wifiLogic(Context pCntx) {
        wifi = (WifiManager) pCntx.getSystemService(Context.WIFI_SERVICE);
        checkWiFiState();
    }

    public ScanResult getWifiZone(){
        checkWiFiState();
        if (wifi.startScan()){
            for (ScanResult zone:wifi.getScanResults()) {
                if (checkZone(zone)){
                    return zone;
                }
            }
        }
        return null;
    }

    private void checkWiFiState(){
        if (wifi.isWifiEnabled() == false)
            wifi.setWifiEnabled(true);
    }

    private boolean checkZone(ScanResult pZone){
        // если зона содержит RAD и POW значит это зона радиации
        if (pZone.SSID.contains("RAD"))
            return true;
        else
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
        return Integer.decode(pSSID.split(":")[1]);
    }

    public static double getRadHour(ScanResult pZone){
        if (pZone != null) {
            int lvl = Math.abs(pZone.level);
            Log.d(TAG, "fiend SSID:" + pZone.SSID + " Level:" + pZone.level);
            if (lvl < 40) {
                return (getPowZone(pZone.SSID) * 1);
            } else if ((lvl >= 40) && (lvl <= 50)) {
                return (getPowZone(pZone.SSID) * 0.9);
            } else if ((lvl >= 50) && (lvl <= 60)) {
                return (getPowZone(pZone.SSID) * 0.5);
            } else if ((lvl >= 60) && (lvl <= 80)) {
                return (getPowZone(pZone.SSID) * 0.3);
            } else if ((lvl >= 90) && (lvl <= 100)) {
                return (getPowZone(pZone.SSID) * 0.1);
            }
        } else {
            Log.d(TAG, "Home rad");
            return homeRad;
        }
        return 0;
    }

}
