package com.android.internal.util.custom;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.provider.Settings;

public class HideDeveloperStatusUtils {
    private static final String TAG = "HideDeveloperStatusUtils";
    private static final Set<String> settingsToHide =
        new HashSet<>(
            Arrays.asList(
                Settings.Global.ADB_ENABLED,
                Settings.Global.ADB_WIFI_ENABLED,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED
            ));

    enum Action {
        ADD,
        REMOVE,
        SET
    }

    private static final Set<String> defaultApps = new HashSet<>(Arrays.asList(
            "com.amazon.avod.thirdpartyclient",
            "com.android.chrome",
            "com.breel.wallpapers20",
            "com.disney.disneyplus",
            "com.google.android.aicore",
            "com.google.android.apps.accessibility.magnifier",
            "com.google.android.apps.aiwallpapers",
            "com.google.android.apps.bard",
            "com.google.android.apps.customization.pixel",
            "com.google.android.apps.emojiwallpaper",
            "com.google.android.apps.nexuslauncher",
            "com.google.android.apps.pixel.agent",
            "com.google.android.apps.pixel.creativeassistant",
            "com.google.android.apps.pixel.support",
            "com.google.android.apps.privacy.wildlife",
            "com.google.android.apps.subscriptions.red",
            "com.google.android.apps.wallpaper",
            "com.google.android.apps.wallpaper.pixel",
            "com.google.android.apps.weather",
            "com.google.android.gms",
            "com.google.android.googlequicksearchbox",
            "com.google.android.soundpicker",
            "com.google.android.wallpaper.effects",
            "com.google.pixel.livewallpaper",
            "com.microsoft.android.smsorganizer",
            "com.nhs.online.nhsonline",
            "com.nothing.smartcenter",
            "com.realme.link",
            "in.startv.hotstar",
            "jp.id_credit_sp2.android",
            "com.google.android.apps.photos",

            //check apps
            "com.byxiaorun.detector",
            "io.github.vvb2060.mahoshojo",
            "krypton.tbsafetychecker",
            "com.zhenxi.hunter",
            "vn.com.techcombank.bb.app",
            "com.vnid",
            "com.example.adbcheck",
            "xyz.xfqlittlefan.notdeveloper",
    
            //game
            "com.bid.master.war.auction.battle",
            "com.justplay.app",
            "com.play.lucky.real.earn.money.free.fun.games.play.reward.income",
            "com.mistplay.mistplay",
            "com.habit.record.reward.tracker",
            "com.tapchamps.tap",
            "com.ss.android.ugc.trill"
    ));

    private static final Set<String> settingUsbStateToHide = new HashSet<>(Arrays.asList(
            UsbManager.USB_FUNCTION_ADB
    ));

    private static boolean isBootCompleted() {
        return SystemProperties.getBoolean("sys.boot_completed", false);
    }

    public static boolean shouldHideDevStatus(ContentResolver cr, String packageName, String name) {
        if (cr == null || packageName == null || name == null || !isBootCompleted()) {
            return false;
        }
        
        Log.i(TAG, "shouldHideDevStatus: packageName = " + packageName + ", name = " + name);

        boolean result = shouldHidePackageName(cr, packageName) && settingsToHide.contains(name);

        Log.i(TAG, "shouldHideDevStatus: result = " + (result ? "true" : "false"));

        return result;
    }

    public static boolean shouldHidePackageName(ContentResolver cr, String packageName) {
        if (cr == null || packageName == null || !isBootCompleted()) {
            return false;
        }

        Log.i(TAG, "shouldHidePackageName: packageName = " + packageName);

        Set<String> apps = getApps(cr);

        boolean result = apps.contains(packageName) || defaultApps.contains(packageName);

        Log.i(TAG, "shouldHidePackageName: result = " + (result ? "true" : "false"));

        return result;
    }

    public static boolean shouldHideDevStatusIntent(String name){
        return settingUsbStateToHide.contains(name);
    }

    private static Set<String> getApps(Context context) {
        if (context == null) {
            return new HashSet<>();
        }

        return getApps(context.getContentResolver());
    }

    private static Set<String> getApps(ContentResolver cr) {
        if (cr == null) {
            return new HashSet<>();
        }

        String apps = Settings.Secure.getString(cr, Settings.Secure.HIDE_DEVELOPER_STATUS);
        if (apps != null && !apps.isEmpty() && !apps.equals(",")) {
            return new HashSet<>(Arrays.asList(apps.split(",")));
        }

        return new HashSet<>();
    }

    private static void putAppsForUser(
            Context context, String packageName, int userId, Action action) {
        if (context == null || userId < 0) {
            return;
        }

        final Set<String> apps = getApps(context);
        switch (action) {
            case ADD:
                apps.add(packageName);
                break;
            case REMOVE:
                apps.remove(packageName);
                break;
            case SET:
                // Don't change
                break;
        }

        Settings.Secure.putStringForUser(
                context.getContentResolver(),
                Settings.Secure.HIDE_DEVELOPER_STATUS,
                String.join(",", apps),
                userId);
    }

    public void addApp(Context mContext, String packageName, int userId) {
        if (mContext == null || packageName == null || userId < 0) {
            return;
        }

        putAppsForUser(mContext, packageName, userId, Action.ADD);
    }

    public void removeApp(Context mContext, String packageName, int userId) {
        if (mContext == null || packageName == null || userId < 0) {
            return;
        }

        putAppsForUser(mContext, packageName, userId, Action.REMOVE);
    }

    public void setApps(Context mContext, int userId) {
        if (mContext == null || userId < 0) {
            return;
        }

        putAppsForUser(mContext, null, userId, Action.SET);
    }
}