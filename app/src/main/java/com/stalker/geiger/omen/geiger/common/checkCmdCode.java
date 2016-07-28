package com.stalker.geiger.omen.geiger.common;

/**
 * Created by omen on 28.07.2016.
 */
public class checkCmdCode {
    public static cmdCodeType getCmdType(String pCmd){
        if (pCmd.contains("heal"))
                return cmdCodeType.HEAL;
        else if (pCmd.contains("res"))
                return cmdCodeType.SETRESIST;
        else
            return cmdCodeType.DEAD;
    }

}
