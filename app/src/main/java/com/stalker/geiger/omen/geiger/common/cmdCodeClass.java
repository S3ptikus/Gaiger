package com.stalker.geiger.omen.geiger.common;

/**
 * Created by p.yurkin on 02.08.16.
 */
public class cmdCodeClass {
    public String _code;
    public cmdCodeType _type;
    public double _value;

    public cmdCodeClass(String pCode, cmdCodeType pType, double pValue) {
        _code = pCode;
        _type = pType;
        _value = pValue;
    }
}
