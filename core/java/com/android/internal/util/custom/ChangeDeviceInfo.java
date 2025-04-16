package com.android.internal.util.custom;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @hide
 */
public final class ChangeDeviceInfo {

    private static final String TAG = "ChangeDeviceInfo";

    private static final Map<String, Object> propsToChange;

    // Packages to Spoof as the most recent Pixel device
    private static final String[] packagesToChange = {
            //check apps
            "com.byxiaorun.detector",
            "io.github.vvb2060.mahoshojo",
            "krypton.tbsafetychecker",
            "com.zhenxi.hunter",
            "vn.com.techcombank.bb.app",
            "com.vnid",
            "com.example.adbcheck",
            "xyz.xfqlittlefan.notdeveloper",
    };

    static {
        if(isDeviceInfoAvailable()){
            propsToChange = new HashMap<>();
            propsToChange.put("TYPE", "user");
            propsToChange.put("TAGS", "release-keys");
            propsToChange.put("BRAND", ChangeDeviceInfoManager.getBrand());
            propsToChange.put("BOARD", ChangeDeviceInfoManager.getBrand());
            propsToChange.put("MANUFACTURER",  ChangeDeviceInfoManager.getManufacturer());
            propsToChange.put("DEVICE", ChangeDeviceInfoManager.getDevice());
            propsToChange.put("PRODUCT",  ChangeDeviceInfoManager.getProduct());
            propsToChange.put("HARDWARE", ChangeDeviceInfoManager.getDevice());
            propsToChange.put("MODEL",  ChangeDeviceInfoManager.getModel());
            propsToChange.put("ID", ChangeDeviceInfoManager.getId());
            propsToChange.put("FINGERPRINT", ChangeDeviceInfoManager.getFingerprint());
            propsToChange.put("VERSION.RELEASE", ChangeDeviceInfoManager.getRelease());
            propsToChange.put("VERSION.INCREMENTAL", ChangeDeviceInfoManager.getIncremental());
            propsToChange.put("VERSION.SECURITY_PATCH", ChangeDeviceInfoManager.getSecurityPatch());
            propsToChange.put("VERSION.DEVICE_INITIAL_SDK_INT", ChangeDeviceInfoManager.getDeviceInitialSdkInt());
        }
    }

    private static boolean isDeviceInfoAvailable(){
        return ChangeDeviceInfoManager.isFingerprintAvailable()
            && ChangeDeviceInfoManager.getManufacturer() != null
            && ChangeDeviceInfoManager.getModel() != null
            && ChangeDeviceInfoManager.getFingerprint() != null
            && ChangeDeviceInfoManager.getBrand() != null
            && ChangeDeviceInfoManager.getProduct() != null
            && ChangeDeviceInfoManager.getDevice() != null
            && ChangeDeviceInfoManager.getId() != null
            && ChangeDeviceInfoManager.getRelease() != null
            && ChangeDeviceInfoManager.getIncremental() != null
            && ChangeDeviceInfoManager.getSecurityPatch() != null
            && ChangeDeviceInfoManager.getDeviceInitialSdkInt() != null;
    }

    public static void setProps(String packageName) {
        if (packageName == null || packageName.isEmpty() || !isDeviceInfoAvailable()) {
            return;
        }

        if (Arrays.asList(packagesToChange).contains(packageName)) {
            for (Map.Entry<String, Object> prop : propsToChange.entrySet()) {
                String key = prop.getKey();
                Object value = prop.getValue();
                Log.i(TAG, "Defining " + key + " prop for: " + packageName);
                setPropValue(key, value);
            }
        }
    }

    private static void setPropValue(String key, Object value) {
        setPropValue(key, value.toString());
    }

    private static void setPropValue(String key, String value) {
        try {
            Log.i(TAG, "Defining prop " + key + " to " + value);
            Class<?> clazz = Build.class;
            if (key.startsWith("VERSION.")) {
                clazz = Build.VERSION.class;
                key = key.substring(8);
            }
            Field field = clazz.getDeclaredField(key);
            field.setAccessible(true);
            // Determine the field type and parse the value accordingly.
            if (field.getType().equals(Integer.TYPE)) {
                field.set(null, Integer.parseInt(value));
            } else if (field.getType().equals(Long.TYPE)) {
                field.set(null, Long.parseLong(value));
            } else {
                field.set(null, value);
            }
            field.setAccessible(false);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }
}