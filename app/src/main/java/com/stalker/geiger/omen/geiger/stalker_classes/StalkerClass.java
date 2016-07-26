package com.stalker.geiger.omen.geiger.stalker_classes;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.stalker.geiger.omen.geiger.R;

import java.io.Serializable;

/**
 * Created by omen on 19.07.2016.
 */
public class StalkerClass implements Serializable {
    private String _name;
    private double _countRad = 0;
    private StatusLife _status = StatusLife.LIFE;
//    private Context cntx;

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public double get_countRad() {
        return _countRad;
    }

    public int get_countRadForProgressbar() {
        // Максимум 100 и это 400 радов до смерти
        return (int)(_countRad / 4) ;
    }

    public void add_rad(double pCount){
        _countRad += pCount;
        if (_countRad > 400)
            _countRad = 400;
        CheckStatus();
    }

    private void CheckStatus() {
        // 10-20 чутка
        // 150 - лучевая болезнь
        // 400 - смерть
        if (this._countRad <= 20){
            this.set_status(StatusLife.LIFE);
        }else if ((this._countRad > 20) && (this._countRad <= 150)) {
            this.set_status(StatusLife.ILL);
        }else if ((this._countRad > 150) && (this._countRad <= 400)) {
            this.set_status(StatusLife.RADSIC);
        }else if (this._countRad >= 400){
            this.set_status(StatusLife.DEAD);
        }
    }

    public String get_status(Context pCntx) {
        switch (_status){
            case LIFE:
                return pCntx.getString(R.string.statusStart) + " - " + pCntx.getString(R.string.statusLife);
            case ILL:
                return pCntx.getString(R.string.statusStart) + " - " + pCntx.getString(R.string.statusIll);
            case RADSIC:
                return pCntx.getString(R.string.statusStart) + " - " + pCntx.getString(R.string.statusRadSick);
            case DEAD:
                return pCntx.getString(R.string.statusStart) + " - " + pCntx.getString(R.string.statusDead);
            default:
                return "";
        }
    }

    public void set_status(StatusLife _status) {
        this._status = _status;
    }

    public StalkerClass(String pName) {
        super();
        _name =  pName;
    }

    public String getJSON(){
        Gson g = new Gson();
        String res = g.toJson(this);
        return res;
    }

    public void saveState(Context pCntx){
        SharedPreferences sharedPref = pCntx.getSharedPreferences(pCntx.getString(R.string.sharedPrefFileName), pCntx.MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPref.edit();
        ed.putString(pCntx.getString(R.string.stalkerClass), this.getJSON());
        ed.commit();
    }

    public static StalkerClass getStalkerState(Context pCntx){
        SharedPreferences sharedPref = pCntx.getSharedPreferences(pCntx.getString(R.string.sharedPrefFileName), pCntx.MODE_PRIVATE);
        String json = sharedPref.getString(pCntx.getString(R.string.stalkerClass),"");
        // если первый раз, то класса не будет
        if (json == ""){
            return null;
        } else {
            Gson g = new Gson();
            return g.fromJson(json, StalkerClass.class);
        }
    }
}
