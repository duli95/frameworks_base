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

public class HideVPNStatusUtils {
    private static final String TAG = "HideVPNStatusUtils";

    enum Action {
        ADD,
        REMOVE,
        SET
    }

    private static final Set<String> whiteListHideDeveloperStatus = new HashSet<>(Arrays.asList(
        "android",
        "com.android.settings",
        "com.android.systemui",
        "com.android.shell"
    ));

    private static boolean isBootCompleted() {
        return SystemProperties.getBoolean("sys.boot_completed", false);
    }

    public static boolean shouldHideVPNStatus(ContentResolver cr, String packageName) {
        if (cr == null || packageName == null || !isBootCompleted()) {
            return false;
        }

        Log.i(TAG, "shouldHideVPNStatus: packageName = " + packageName);

        Set<String> apps = getApps(cr);

        boolean result = !apps.contains(packageName) && !whiteListHideDeveloperStatus.contains(packageName);

        Log.i(TAG, "shouldHideVPNStatus: result = " + (result ? "true" : "false"));

        return result;
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

        String apps = Settings.Secure.getString(cr, Settings.Secure.HIDE_VPN_STATUS);
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