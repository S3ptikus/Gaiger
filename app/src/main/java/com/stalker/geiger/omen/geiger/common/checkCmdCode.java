package com.stalker.geiger.omen.geiger.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by omen on 28.07.2016.
 */
public class checkCmdCode {
    public static cmdCodeClass getCmdObj(String pCmd){
        return new cmdCodeClass(pCmd, getCmdType(pCmd), getCmdValue(pCmd));
    }

    private static cmdCodeType getCmdType(String pCode){
        // Heal
        if ((pCode.contains("#") && pCode.contains("@")) && (pCode.indexOf("#") < pCode.indexOf("@")))
            return cmdCodeType.SETRAD;

        // RESCOEF
        if ((pCode.contains("&") && pCode.contains("^")) && (pCode.indexOf("&") < pCode.indexOf("^")))
            return cmdCodeType.SETRESIST;

        // DEAD
        if ((pCode.contains("*") && pCode.contains("(")) && (pCode.indexOf("*") < pCode.indexOf("(")))
            return cmdCodeType.DEAD;
        return null;
    }

    private static double getCmdValue(String pCode){
        String compareValue = "";
        Double res;
        Pattern p = Pattern.compile("-?\\d+");
        Matcher m = p.matcher(pCode);
        while (m.find()) {
            compareValue += m.group().toString();
        }

        if (compareValue.length() == 0)
            return -1;

        if ((compareValue.contains("00")) &&(compareValue.charAt(0) == '0') && (compareValue.charAt(1) == '0')) {
            compareValue = compareValue.replaceFirst("00", "0.");
            try{
                res = Double.parseDouble(compareValue);
                return res;
            }catch (Exception e){
                return -1;
            }
        }
        else{
            res = Double.parseDouble(compareValue);
            return res;
        }
    }

}
