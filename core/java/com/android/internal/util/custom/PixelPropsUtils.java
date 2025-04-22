/*
 * Copyright (C) 2022 The Pixel Experience Project
 *               2021-2022 crDroid Android Project
 *           (C) 2023 ArrowOS
 *           (C) 2023 The LibreMobileOS Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util.custom;

import android.app.ActivityTaskManager;
import android.app.ActivityThread;
import android.app.Application;
import android.app.TaskStackListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Build;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.android.internal.R;

public class PixelPropsUtils {

    private static final String TAG = PixelPropsUtils.class.getSimpleName();
    private static final String DEVICE = "org.pixelexperience.device";

    private static final String PACKAGE_GMS = "com.google.android.gms";
    private static final ComponentName GMS_ADD_ACCOUNT_ACTIVITY = ComponentName.unflattenFromString(
            "com.google.android.gms/.auth.uiflows.minutemaid.MinuteMaidActivity");

    private static final boolean DEBUG = false;

    public static final String SPOOF_PIXEL_GMS = "persist.sys.pixelprops.gms";

    private static final String PACKAGE_NEXUS_LAUNCHER = "com.google.android.apps.nexuslauncher";
    private static Set<String> mLauncherPkgs;
    private static Set<String> mExemptedUidPkgs;

    private static final Map<String, Object> propsToChangeGeneric;
    private static final Map<String, Object> propsToChangePixel7Pro;
    private static final Map<String, Object> propsToChangePixel5;
    private static final Map<String, Object> propsToChangePixelXL;
    private static final Map<String, ArrayList<String>> propsToKeep;

    private static final String[] packagesToChangePixel7Pro = {
            "com.google.android.apps.privacy.wildlife",
            "com.google.android.apps.wallpaper.pixel",
            "com.google.android.apps.wallpaper",
            "com.google.android.apps.subscriptions.red",
            "com.google.pixel.livewallpaper",
            "com.google.android.wallpaper.effects",
            "com.google.android.apps.emojiwallpaper",
    };

    private static final String[] extraPackagesToChange = {
            "com.android.chrome",
            "com.breel.wallpapers20",
            "com.nhs.online.nhsonline",
            "com.netflix.mediaclient",
            "com.nothing.smartcenter"
    };

    private static final String[] packagesToKeep = {
            "com.google.android.dialer",
            "com.google.android.euicc",
            "com.google.ar.core",
            "com.google.android.youtube",
            "com.google.android.apps.youtube.kids",
            "com.google.android.apps.youtube.music",
            "com.google.android.apps.recorder",
            "com.google.android.apps.wearables.maestro.companion",
            "com.google.android.apps.tachyon",
            "com.google.android.apps.tycho",
            "com.google.android.as",
            "com.google.android.gms",
            "com.google.android.apps.restore"
    };

    private static final String[] customGoogleCameraPackages = {
            "com.google.android.MTCL83",
            "com.google.android.UltraCVM",
            "com.google.android.apps.cameralite"
    };

    private static volatile boolean sIsGms, sIsFinsky, sIsPhotos;

    static {
        propsToKeep = new HashMap<>();
        propsToKeep.put("com.google.android.settings.intelligence", new ArrayList<>(Collections.singletonList("FINGERPRINT")));
        propsToChangeGeneric = new HashMap<>();
        propsToChangeGeneric.put("TYPE", "user");
        propsToChangeGeneric.put("TAGS", "release-keys");
        propsToChangePixel7Pro = new HashMap<>();
        propsToChangePixel7Pro.put("BRAND", "google");
        propsToChangePixel7Pro.put("MANUFACTURER", "Google");
        propsToChangePixel7Pro.put("DEVICE", "cheetah");
        propsToChangePixel7Pro.put("PRODUCT", "cheetah");
        propsToChangePixel7Pro.put("MODEL", "Pixel 7 Pro");
        propsToChangePixel7Pro.put("FINGERPRINT", "google/cheetah/cheetah:13/TQ3A.230901.001/10750268:user/release-keys");
        propsToChangePixel5 = new HashMap<>();
        propsToChangePixel5.put("BRAND", "google");
        propsToChangePixel5.put("MANUFACTURER", "Google");
        propsToChangePixel5.put("DEVICE", "redfin");
        propsToChangePixel5.put("PRODUCT", "redfin");
        propsToChangePixel5.put("MODEL", "Pixel 5");
        propsToChangePixel5.put("FINGERPRINT", "google/redfin/redfin:13/TQ3A.230901.001/10750268:user/release-keys");
        propsToChangePixelXL = new HashMap<>();
        propsToChangePixelXL.put("BRAND", "google");
        propsToChangePixelXL.put("MANUFACTURER", "Google");
        propsToChangePixelXL.put("DEVICE", "marlin");
        propsToChangePixelXL.put("PRODUCT", "marlin");
        propsToChangePixelXL.put("MODEL", "Pixel XL");
        propsToChangePixelXL.put("FINGERPRINT", "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys");
    }

    private static boolean isGoogleCameraPackage(String packageName) {
        return packageName.startsWith("com.google.android.GoogleCamera") ||
                Arrays.asList(customGoogleCameraPackages).contains(packageName);
    }
    
    public static boolean setPropsForGms(String packageName) {
        if (packageName.equals("com.android.vending")) {
            sIsFinsky = true;
        }
        if (packageName.equals(PACKAGE_GMS)
                || packageName.toLowerCase().contains("androidx.test")
                || packageName.equalsIgnoreCase("com.google.android.apps.restore")) {
            setPropValue("TIME", System.currentTimeMillis());
            final String processName = Application.getProcessName();
            if (processName.toLowerCase().contains("unstable")
                    || processName.toLowerCase().contains("pixelmigrate")
                    || processName.toLowerCase().contains("instrumentation")) {
                sIsGms = true;

                final boolean was = isGmsAddAccountActivityOnTop();
                final TaskStackListener taskStackListener = new TaskStackListener() {
                    @Override
                    public void onTaskStackChanged() {
                        final boolean is = isGmsAddAccountActivityOnTop();
                        if (is ^ was) {
                            dlog("GmsAddAccountActivityOnTop is:" + is + " was:" + was + ", killing myself!");
                            // process will restart automatically later
                            Process.killProcess(Process.myPid());
                        }
                    }
                };
                try {
                    ActivityTaskManager.getService().registerTaskStackListener(taskStackListener);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to register task stack listener!", e);
                }
                if (was) return true;

                dlog("Spoofing build for GMS");
                // Alter build parameters to pixel for avoiding hardware attestation enforcement

                if(!checkFingerprintData()){
                    Log.i(TAG, "No fingerprint data available, using default values");
                    setpropValueGms("MANUFACTURER", "Google");
                    setpropValueGms("MODEL", "Pixel 6");
                    setpropValueGms("FINGERPRINT", "google/oriole_beta/oriole:16/BP22.250221.010/13193326:user/release-keys");
                    setpropValueGms("BRAND", "google");
                    setpropValueGms("BOARD", "google");
                    setpropValueGms("HARDWARE", "oriole");
                    setpropValueGms("PRODUCT", "oriole_beta");
                    setpropValueGms("DEVICE", "oriole");
                    setpropValueGms("ID", "BP22.250221.010");
                    setpropValueGms("TYPE", "user");
                    setpropValueGms("TAGS", "release-keys");
                    setpropValueGms("VERSION.RELEASE", "16");
                    setpropValueGms("VERSION.INCREMENTAL", "13193326");
                    setpropValueGms("VERSION.SECURITY_PATCH", "2025-03-05");
                    setpropValueGms("VERSION.DEVICE_INITIAL_SDK_INT", "21");
                }else{
                    Log.i(TAG, "Fingerprint data available, using values from fingerprint data");
                    setpropValueGms("MANUFACTURER", DeviceInfoManager.getManufacturer());
                    setpropValueGms("MODEL", DeviceInfoManager.getModel());
                    setpropValueGms("FINGERPRINT", DeviceInfoManager.getFingerprint());
                    setpropValueGms("BRAND", DeviceInfoManager.getBrand());
                    setpropValueGms("BOARD", DeviceInfoManager.getBrand());
                    setpropValueGms("HARDWARE", DeviceInfoManager.getDevice());
                    setpropValueGms("PRODUCT", DeviceInfoManager.getProduct());
                    setpropValueGms("DEVICE", DeviceInfoManager.getDevice());
                    setpropValueGms("ID", DeviceInfoManager.getId());
                    setpropValueGms("TYPE", "user");
                    setpropValueGms("TAGS", "release-keys");
                    setpropValueGms("VERSION.RELEASE", DeviceInfoManager.getRelease());
                    setpropValueGms("VERSION.INCREMENTAL", DeviceInfoManager.getIncremental());
                    setpropValueGms("VERSION.SECURITY_PATCH", DeviceInfoManager.getSecurityPatch());
                    setpropValueGms("VERSION.DEVICE_INITIAL_SDK_INT", DeviceInfoManager.getDeviceInitialSdkInt());
                }

                return true;
            }
        }
        return false;
    }

    private static boolean checkFingerprintData(){
        return DeviceInfoManager.isFingerprintAvailable()
            && DeviceInfoManager.getManufacturer() != null
            && DeviceInfoManager.getModel() != null
            && DeviceInfoManager.getFingerprint() != null
            && DeviceInfoManager.getBrand() != null
            && DeviceInfoManager.getProduct() != null
            && DeviceInfoManager.getDevice() != null
            && DeviceInfoManager.getId() != null
            && DeviceInfoManager.getRelease() != null
            && DeviceInfoManager.getIncremental() != null
            && DeviceInfoManager.getSecurityPatch() != null
            && DeviceInfoManager.getDeviceInitialSdkInt() != null;
    }

    private static void setpropValueGms(String key, String value) {
        try {
            if (DEBUG) Log.d(TAG, "Defining prop " + key + " to " + value);
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

    public static void setProps(String packageName) {
        propsToChangeGeneric.forEach((k, v) -> setPropValue(k, v));
        if (packageName == null || packageName.isEmpty()) {
            return;
        }
        if (setPropsForGms(packageName)){
            return;
        }
        if (Arrays.asList(packagesToKeep).contains(packageName)) {
            return;
        }
        if (isGoogleCameraPackage(packageName)) {
            return;
        }

        Map<String, Object> propsToChange = new HashMap<>();

        if (packageName.startsWith("com.google.")
                || packageName.startsWith("com.samsung.")
                || Arrays.asList(extraPackagesToChange).contains(packageName)) {

            if (packageName.equals("com.google.android.apps.photos")) {
                propsToChange.putAll(propsToChangePixelXL);
            }else {
                if (Arrays.asList(packagesToChangePixel7Pro).contains(packageName)) {
                    propsToChange.putAll(propsToChangePixel7Pro);
                } else {
                    propsToChange.putAll(propsToChangePixel5);
                }
            }

            if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
            for (Map.Entry<String, Object> prop : propsToChange.entrySet()) {
                String key = prop.getKey();
                Object value = prop.getValue();
                if (propsToKeep.containsKey(packageName) && propsToKeep.get(packageName).contains(key)) {
                    if (DEBUG) Log.d(TAG, "Not defining " + key + " prop for: " + packageName);
                    continue;
                }
                if (DEBUG) Log.d(TAG, "Defining " + key + " prop for: " + packageName);
                setPropValue(key, value);
            }
            // Set proper indexing fingerprint
            if (packageName.equals("com.google.android.settings.intelligence")) {
                setPropValue("FINGERPRINT", Build.VERSION.INCREMENTAL);
            }
        }
    }

    private static void setPropValue(String key, Object value) {
        try {
            if (DEBUG) Log.d(TAG, "Defining prop " + key + " to " + value.toString());
            Field field = Build.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    private static void setBuildField(String key, String value) {
        try {
            // Unlock
            Field field = Build.class.getDeclaredField(key);
            field.setAccessible(true);

            // Edit
            field.set(null, value);

            // Lock
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to spoof Build." + key, e);
        }
    }

    private static void setVersionField(String key, Object value) {
        try {
            // Unlock
            if (DEBUG) Log.d(TAG, "Defining version field " + key + " to " + value.toString());
            Field field = Build.VERSION.class.getDeclaredField(key);
            field.setAccessible(true);

            // Edit
            field.set(null, value);

            // Lock
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set version field " + key, e);
        }
    }

    private static void setVersionFieldString(String key, String value) {
        try {
            Field field = Build.VERSION.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to spoof Build." + key, e);
        }
    }

    private static boolean isGmsAddAccountActivityOnTop() {
        try {
            final ActivityTaskManager.RootTaskInfo focusedTask =
                    ActivityTaskManager.getService().getFocusedRootTaskInfo();
            return focusedTask != null && focusedTask.topActivity != null
                    && focusedTask.topActivity.equals(GMS_ADD_ACCOUNT_ACTIVITY);
        } catch (Exception e) {
            Log.e(TAG, "Unable to get top activity!", e);
        }
        return false;
    }

    private static String[] getStringArrayResSafely(int resId) {
        String[] strArr = Resources.getSystem().getStringArray(resId);
        if (strArr == null) strArr = new String[0];
        return strArr;
    }

    public static boolean isPackageGoogle(String pkg) {
        return pkg != null && pkg.toLowerCase().contains("google");
    }

    private static Set<String> getLauncherPkgs() {
        if (mLauncherPkgs == null || mLauncherPkgs.isEmpty()) {
            mLauncherPkgs =
                    new HashSet<>(
                            Arrays.asList(
                                    getStringArrayResSafely(R.array.config_launcherPackages)));
        }
        return mLauncherPkgs;
    }

    private static Set<String> getExemptedUidPkgs() {
        if (mExemptedUidPkgs == null || mExemptedUidPkgs.isEmpty()) {
            mExemptedUidPkgs = new HashSet<>();
            mExemptedUidPkgs.add(PACKAGE_GMS);
            mExemptedUidPkgs.addAll(getLauncherPkgs());
        }
        return mExemptedUidPkgs;
    }

    public static boolean isNexusLauncher(Context context) {
        try {
            return PACKAGE_NEXUS_LAUNCHER.equals(
                    context.getPackageManager().getNameForUid(android.os.Binder.getCallingUid()));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isSystemLauncher(Context context) {
        try {
            return isSystemLauncherInternal(
                    context.getPackageManager().getNameForUid(android.os.Binder.getCallingUid()));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isSystemLauncher(int callingUid) {
        try {
            return isSystemLauncherInternal(
                    ActivityThread.getPackageManager().getNameForUid(callingUid));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isSystemLauncherInternal(String callerPackage) {
        return getLauncherPkgs().contains(callerPackage);
    }

    public static boolean shouldBypassTaskPermission(int callingUid) {
        for (String pkg : getExemptedUidPkgs()) {
            try {
                ApplicationInfo appInfo =
                        ActivityThread.getPackageManager()
                                .getApplicationInfo(pkg, 0, UserHandle.getUserId(callingUid));
                if (appInfo.uid == callingUid) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    public static boolean shouldBypassManageActivityTaskPermission(Context context) {
        final int callingUid = Binder.getCallingUid();
        return isSystemLauncher(callingUid)
                || isPackageGoogle(context.getPackageManager().getNameForUid(callingUid));
    }

    public static boolean shouldBypassMonitorInputPermission(Context context) {
        final int callingUid = Binder.getCallingUid();
        return shouldBypassTaskPermission(callingUid)
                || isPackageGoogle(context.getPackageManager().getNameForUid(callingUid));
    }

    // Whitelist of package names to bypass FGS type validation
    public static boolean shouldBypassFGSValidation(String packageName) {
        // Check if the app is whitelisted
        if (Arrays.asList(getStringArrayResSafely(R.array.config_fgsTypeValidationBypassPackages))
                .contains(packageName)) {
            dlog(
                    "shouldBypassFGSValidation: "
                            + "Bypassing FGS type validation for whitelisted app: "
                            + packageName);
            return true;
        }
        return false;
    }

    // Whitelist of package names to bypass alarm manager validation
    public static boolean shouldBypassAlarmManagerValidation(String packageName) {
        // Check if the app is whitelisted
        if (Arrays.asList(
                        getStringArrayResSafely(
                                R.array.config_alarmManagerValidationBypassPackages))
                .contains(packageName)) {
            dlog(
                    "shouldBypassAlarmManagerValidation: "
                            + "Bypassing alarm manager validation for whitelisted app: "
                            + packageName);
            return true;
        }
        return false;
    }

    // Whitelist of package names to bypass broadcast reciever validation
    public static boolean shouldBypassBroadcastReceiverValidation(String packageName) {
        // Check if the app is whitelisted
        if (Arrays.asList(
                        getStringArrayResSafely(
                                R.array.config_broadcaseReceiverValidationBypassPackages))
                .contains(packageName)) {
            dlog(
                    "shouldBypassBroadcastReceiverValidation: "
                            + "Bypassing broadcast receiver validation for whitelisted app: "
                            + packageName);
            return true;
        }
        return false;
    }

    private static boolean isCallerSafetyNet() {
        return sIsGms || Arrays.stream(Thread.currentThread().getStackTrace())
                .anyMatch(elem -> elem.getClassName().contains("DroidGuard"));
    }

    public static void onEngineGetCertificateChain() {
        if(KeyProviderManager.isKeyboxAvailable()) {
            return;
        }

        boolean isPixelGmsEnabled = SystemProperties.getBoolean(SPOOF_PIXEL_GMS, true);
        if (!isPixelGmsEnabled)
            return;

        throw new UnsupportedOperationException();
    }

    public static void dlog(String msg) {
        if (DEBUG) Log.d(TAG, msg);
    }

}
