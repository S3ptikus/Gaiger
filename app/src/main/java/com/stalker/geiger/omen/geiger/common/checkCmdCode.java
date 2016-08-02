package com.stalker.geiger.omen.geiger.common;

/**
 * Created by omen on 28.07.2016.
 */
public class checkCmdCode {
    public static cmdCodeClass getCmdObj(String pCmd){
        return new cmdCodeClass(getCmdType(pCmd), getCmdValue(pCmd));
    }

    private static cmdCodeType getCmdType(String pCode){
        // Heal
        if (pCode.indexOf("#") < pCode.indexOf("@"))
            return cmdCodeType.SETRAD;

        // RESCOEF
        if (pCode.indexOf("&") < pCode.indexOf("$"))
            return cmdCodeType.SETRESIST;

        // DEAD
        if (pCode.indexOf("*") < pCode.indexOf("("))
            return cmdCodeType.DEAD;

        return null;
    }

    private static double getCmdValue(String pCode){
        return 0;
    }

}
