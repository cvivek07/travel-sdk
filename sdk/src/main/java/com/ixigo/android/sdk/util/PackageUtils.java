package com.ixigo.android.sdk.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class PackageUtils {

    public static boolean isDebuggable(Context context) {
        boolean isDebuggable = false;
        try {
            isDebuggable = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isDebuggable;
    }

    public static String getName(Context context) {
        return StringUtils.toString(context.getApplicationInfo().loadLabel(context.getPackageManager()));
    }

    public static String getVersionName(Context context) {
        String versionName = null;
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static Integer getVersionCode(Context context) {
        Integer versionName = null;
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static boolean isPackagePresent(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Intent getAppLaunchIntent(Context context, String packageName) {
        return context.getPackageManager().getLaunchIntentForPackage(packageName);
    }

}
