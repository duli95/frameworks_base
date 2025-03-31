package com.android.internal.util.custom;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import android.util.Log;
import org.json.JSONObject;


public class DeviceInfoManager {
    private static final String TAG = "DeviceInfoManager";
    private static final String DATA_PIF = ReadFileData.readFile("pif.json");

    private static FingerprintData fingerprintData = null;

    private static class FingerprintData {
        public String MANUFACTURER;
        public String MODEL;
        public String FINGERPRINT;
        public String BRAND;
        public String PRODUCT;
        public String DEVICE;
        public String RELEASE;
        public String ID;
        public String INCREMENTAL;
        public String TYPE;
        public String TAGS;
        public String SECURITY_PATCH;
        public String DEVICE_INITIAL_SDK_INT;
    }

    private static void parseFingerprint(){
        try {
            if(DATA_PIF == null || DATA_PIF.isEmpty()){
                Log.e(TAG, "Fingerprint content is empty");
                return;
            }

            JSONObject json_data = new JSONObject(DATA_PIF);
            FingerprintData data = new FingerprintData();
            
            data.MANUFACTURER = json_data.getString("MANUFACTURER");
            data.MODEL = json_data.getString("MODEL");
            data.FINGERPRINT = json_data.getString("FINGERPRINT");
            data.SECURITY_PATCH = json_data.getString("SECURITY_PATCH");
            data.DEVICE_INITIAL_SDK_INT = json_data.optString("DEVICE_INITIAL_SDK_INT", "21");
    
            String[] parts = data.FINGERPRINT.split("/");
    
            List<String> result = new ArrayList<>();
    
            for (String part : parts) {
                String[] subParts = part.split(":");
                result.addAll(Arrays.asList(subParts));
            }
    
            data.BRAND = result.get(0);
            data.PRODUCT = result.get(1);
            data.DEVICE = result.get(2);
            data.RELEASE = result.get(3);
            data.ID = result.get(4);
            data.INCREMENTAL = result.get(5);
            data.TYPE = result.get(6);
            data.TAGS = result.get(7);
    
            fingerprintData = data;
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse fingerprint", e);
            fingerprintData = null;
        }
    }

    public static boolean isFingerprintAvailable(){
        return DATA_PIF != null && !DATA_PIF.isEmpty();
    }

    public static String getManufacturer() {
        if(fingerprintData == null){
            parseFingerprint();
        }
        return fingerprintData != null ? fingerprintData.MANUFACTURER : null;
    }

    public static String getModel() {
        if(fingerprintData == null){
            parseFingerprint();
        }
        return fingerprintData != null ? fingerprintData.MODEL : null;
    }

    public static String getFingerprint() {
        if(fingerprintData == null){
            parseFingerprint();
        }
        return fingerprintData != null ? fingerprintData.FINGERPRINT : null;
    }

    public static String getBrand() {
        if(fingerprintData == null){
            parseFingerprint();
        }
        return fingerprintData != null ? fingerprintData.BRAND : null;
    }

    public static String getProduct() {
        if(fingerprintData == null){
            parseFingerprint();
        }
        return fingerprintData != null ? fingerprintData.PRODUCT : null;
    }

    public static String getDevice() {
        if(fingerprintData == null){
            parseFingerprint();
        }
        return fingerprintData != null ? fingerprintData.DEVICE : null;
    }

    public static String getRelease() {
        if(fingerprintData == null){
            parseFingerprint();
        }
        return fingerprintData != null ? fingerprintData.RELEASE : null;
    }

    public static String getId() {
        if(fingerprintData == null){
            parseFingerprint();
        }
        return fingerprintData != null ? fingerprintData.ID : null;
    }

    public static String getIncremental() {
        if(fingerprintData == null){
            parseFingerprint();
        }
        return fingerprintData != null ? fingerprintData.INCREMENTAL : null;
    }

    public static String getType() {
        if(fingerprintData == null){
            parseFingerprint();
        }
        return fingerprintData != null ? fingerprintData.TYPE : null;
    }

    public static String getTags() {
        if(fingerprintData == null){
            parseFingerprint();
        }
        return fingerprintData != null ? fingerprintData.TAGS : null;
    }

    public static String getSecurityPatch() {
        if(fingerprintData == null){
            parseFingerprint();
        }
        return fingerprintData != null ? fingerprintData.SECURITY_PATCH : null;
    }

    public static String getDeviceInitialSdkInt() {
        if(fingerprintData == null){
            parseFingerprint();
        }
        return fingerprintData != null ? fingerprintData.DEVICE_INITIAL_SDK_INT : null;
    }
}