package com.android.internal.util.custom;

import android.content.ContentResolver;
import android.provider.Settings;

import android.telephony.SubscriptionInfo;

/**
 * @hide
 */
public class SpoofSim {
    private static final String SPOOF_STATUS = "spoof_status";
    private static final String SPOOF_SIMCATTIERID = "spoof_simcarrierid";
    private static final String SPOOF_ICCID = "spoof_iccid";
    private static final String SPOOF_SIMNUMBER = "spoof_simnumber";
    private static final String SPOOF_COUNTRYISO = "spoof_countryiso";
    private static final String SPOOF_IMEI = "spoof_imei";
    private static final String SPOOF_MEID = "spoof_meid";
    private static final String SPOOF_OPERATOR = "spoof_operator";
    private static final String SPOOF_OPERATORNAME = "spoof_operatorname";
    private static final String SPOOF_SIMSERIALNUMBER = "spoof_simserialnumber";

    private static String getSpoofSetting(String value){
        ContentResolver cr = android.app.ActivityThread.currentApplication().getContentResolver();
        return Settings.Global.getString(cr, value);
    }

    public static boolean getSpoofStatus(){
        try {
            String status = getSpoofSetting(Settings.Global.SIM_STATE);
            if(status != null && !status.isEmpty()){
                return true;
            }else{
                status = getSpoofSetting(SPOOF_STATUS);
                if(status != null && !status.isEmpty()){
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static int spoofCarrierId(){
        try {
            return Integer.parseInt(getSpoofSetting(SPOOF_SIMCATTIERID));
        } catch (Exception e) {
            return -1;
        }
    }

    public static String spoofIccId(){
        try {
            if(getSpoofSetting(Settings.Global.ICCID) != null && !getSpoofSetting(Settings.Global.ICCID).isEmpty()){
                return getSpoofSetting(Settings.Global.ICCID);
            }
            return getSpoofSetting(SPOOF_ICCID) != null ? getSpoofSetting(SPOOF_ICCID) : "";
        } catch (Exception e) {
            return "";
        }
    }

    public static String spoofSimNumber(){
        try {
            if(getSpoofSetting(Settings.Global.LINE1_NUMBER) != null && !getSpoofSetting(Settings.Global.LINE1_NUMBER).isEmpty()){
                return getSpoofSetting(Settings.Global.LINE1_NUMBER);
            }
            return  getSpoofSetting(SPOOF_SIMNUMBER) != null ? getSpoofSetting(SPOOF_SIMNUMBER) : "";
        } catch (Exception e) {
            return "";
        }
    }

    public static String spoofCountryIso(){
        try {
            return getSpoofSetting(SPOOF_COUNTRYISO) != null ? getSpoofSetting(SPOOF_COUNTRYISO) : "";
        } catch (Exception e) {
            return "";
        }
    }

    public static SubscriptionInfo customSubscripInfo(){
        try {
            return new SubscriptionInfo(
                0,
                "unknow", 
                0, 
                "unknow", 
                "unknow", 
                0, 
                0, 
                "unknow", 
                0, 
                null, 
                "unknow", 
                "unknow", 
                "unknow", 
                false, 
                null, 
                "unknow");
        } catch (Exception e) {
            return null;
        }
    }

    public static String spoofImei(){
        try {
            return getSpoofSetting(SPOOF_IMEI) != null ? getSpoofSetting(SPOOF_IMEI) : "";
        } catch (Exception e) {
            return "";
        }
    }

    public static String spoofMeid(){
        try {
            return getSpoofSetting(SPOOF_MEID) != null ? getSpoofSetting(SPOOF_MEID) : "";
        } catch (Exception e) {
            return "";
        }
    }

    public static String spoofOperator(){
        try {
            return getSpoofSetting(SPOOF_OPERATOR) != null ? getSpoofSetting(SPOOF_OPERATOR) : "";
        } catch (Exception e) {
            return "";
        }
    }

    public static String spoofOperatorName(){
        try {
            return getSpoofSetting(SPOOF_OPERATORNAME) != null ? getSpoofSetting(SPOOF_OPERATORNAME) : "";
        } catch (Exception e) {
            return "";
        }
    }

    public static  String spoofSimSerialNumber(){
        try {
            return getSpoofSetting(SPOOF_SIMSERIALNUMBER) != null ? getSpoofSetting(SPOOF_SIMSERIALNUMBER) : "";
        } catch (Exception e) {
            return "";
        }
    }
}