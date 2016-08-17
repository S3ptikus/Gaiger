package com.stalker.geiger.omen.geiger.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.stalker.geiger.omen.geiger.R;
import com.stalker.geiger.omen.geiger.stalker_classes.StalkerClass;

/**
 * Created by omen on 17.08.2016.
 */
public class SharedUtils {

    SharedPreferences sharedPref;
    SharedPreferences.Editor ed;
    Context context;

    public SharedUtils(Context pCntx) {
        super();
        context = pCntx;
        sharedPref = pCntx.getSharedPreferences(pCntx.getString(R.string.sharedPrefFileName), pCntx.MODE_PRIVATE);
        ed = sharedPref.edit();
    }

    public void saveState(StalkerClass pStalker){
        ed.putString(context.getString(R.string.stalkerClass), pStalker.getJSON());
        ed.commit();
    }

    public StalkerClass getStalkerState(){
        String json = sharedPref.getString(context.getString(R.string.stalkerClass),"");
        // если первый раз, то класса не будет
        if (json == ""){
            return null;
        } else {
            Gson g = new Gson();
            return g.fromJson(json, StalkerClass.class);
        }
    }

    public String getString(String pKey){
        return sharedPref.getString(pKey,"");
    }

    public void removeCmdCode(){
        ed.remove(context.getString(R.string.cmdCodePrefKey));
        ed.commit();
    }

    public void setCmdCode(String pCode){
        ed.putString(context.getString(R.string.cmdCodePrefKey), pCode);
        ed.commit();
    }

    public void clear(){
        ed.clear();
        ed.commit();
    }

}
