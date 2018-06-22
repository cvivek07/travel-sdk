package com.ixigo.android.sdk.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class UuidHelper {

    private static final String PREFERENCES_FILE = "com.ixigo.android.sdk.uuid";
    private static final String PREF_UUID = "uuid";

    private static UUID mUuid;

    public static UuidHelper getInstance(Context context) {
        return new UuidHelper(context);
    }

    private UuidHelper(Context context) {

        if (mUuid == null) {
            synchronized (UuidHelper.class) {
                if (mUuid == null) {
                    final SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE, 0);
                    final String uuid = prefs.getString(PREF_UUID, null);

                    if (uuid != null) {
                        mUuid = UUID.fromString(uuid);
                    } else {
                        mUuid = UUID.randomUUID();
                        prefs.edit().putString(PREF_UUID, mUuid.toString()).apply();
                    }
                }
            }
        }

    }

    public UUID getUuid() {
        return mUuid;
    }

}