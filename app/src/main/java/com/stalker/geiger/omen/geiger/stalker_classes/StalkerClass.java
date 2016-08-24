package com.stalker.geiger.omen.geiger.stalker_classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.stalker.geiger.omen.geiger.R;
import com.stalker.geiger.omen.geiger.common.SharedUtils;
import com.stalker.geiger.omen.geiger.common.cmdCodeClass;
import com.stalker.geiger.omen.geiger.common.cmdCodeType;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by omen on 19.07.2016.
 */
public class StalkerClass implements Serializable {
    private String _name;
    private double _countRad = 0;
    private ArrayList<String> _listCmdCodes;

    public StatusLife get_status() {
        return _status;
    }
    private StatusLife _status = StatusLife.LIFE;
    private double _resistCoef = 1;
    private boolean setIll = true;

    public int get_resistCoef() {
        return (int) Math.ceil(_resistCoef * 100);
    }

    public void set_resistCoef(double pCoef){
        if (pCoef > 1)
            _resistCoef = 1;
        else if (pCoef < 0)
            _resistCoef = 0;
        else
            _resistCoef = pCoef;
    }

    public String get_name() {
        return _name;
    }

    public void set_countRad(double _countRad) {
        if (_countRad > 400)
            this._countRad = 400;
        else if (_countRad < 0)
            this._countRad = 0;
        else
            this._countRad = _countRad;
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
        _countRad += pCount * _resistCoef;
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
            if (!setIll){
                _resistCoef -=0.1;
                setIll = true;
            }
        }else if ((this._countRad > 20) && (this._countRad <= 150)) {
            this.set_status(StatusLife.ILL);
            if (!setIll){
                _resistCoef -=0.1;
                setIll = true;
            }
        }else if ((this._countRad > 150) && (this._countRad < 400)) {
            this.set_status(StatusLife.RADSIC);
            if (setIll){
                _resistCoef +=0.1;
                setIll = false;
            }
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
        if (_listCmdCodes == null)
            _listCmdCodes = new ArrayList<String>();
    }

    public String getJSON(){
        Gson g = new Gson();
        String res = g.toJson(this);
        return res;
    }

    public void applyCmdCode(cmdCodeClass pCodeObj){
        if (((pCodeObj._type == null) || (pCodeObj._value == -1)) && (pCodeObj._type != cmdCodeType.DEAD))
            return;

        if (_listCmdCodes == null) {
            _listCmdCodes = new ArrayList<String>();
        }

        if (_listCmdCodes.contains(pCodeObj._code))
            return;
        else
            _listCmdCodes.add(pCodeObj._code);

        switch (pCodeObj._type){
            case SETRESIST:{
                this.set_resistCoef(pCodeObj._value);
                break;
            }
            case SETRAD:{
                this.set_countRad(pCodeObj._value);
                break;
            }
            case DEAD:{
                this.set_countRad(400);
                break;
            }
            default:{
                return;
            }
        }
    }
}
