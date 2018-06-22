package com.ixigo.android.sdk.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

    @Nullable
    public static String getStringVal(JSONObject jsonObject, String key) {
        try {
            return jsonObject.has(key) && !jsonObject.isNull(key) ? jsonObject.getString(key) : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    public static String getStringVal(JSONObject jsonObject, String key, @NonNull String defaultValue) {
        String stringVal = getStringVal(jsonObject, key);
        return stringVal == null ? defaultValue : stringVal;
    }

    @Nullable
    public static Long getLongVal(JSONObject jsonObject, String key) {
        try {
            return jsonObject.has(key) && !jsonObject.isNull(key) ? jsonObject.getLong(key) : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getLongVal(JSONObject jsonObject, String key, long defaultValue) {
        Long longVal = getLongVal(jsonObject, key);
        return longVal == null ? defaultValue : longVal;
    }

    @Nullable
    public static Integer getIntegerVal(JSONObject jsonObject, String key) {
        try {
            return jsonObject.has(key) && !jsonObject.isNull(key) ? jsonObject.getInt(key) : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getIntegerVal(JSONObject jsonObject, String key, int defaultValue) {
        Integer integerVal = getIntegerVal(jsonObject, key);
        return integerVal == null ? defaultValue : integerVal;
    }

    @Nullable
    public static Double getDoubleVal(JSONObject jsonObject, String key) {
        try {
            return jsonObject.has(key) && !jsonObject.isNull(key) ? jsonObject.getDouble(key) : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double getDoubleVal(JSONObject jsonObject, String key, double defaultValue) {
        Double doubleVal = getDoubleVal(jsonObject, key);
        return doubleVal == null ? defaultValue : doubleVal;
    }

    @Nullable
    public static Boolean getBooleanVal(JSONObject jsonObject, String key) {
        try {
            return jsonObject.has(key) && !jsonObject.isNull(key) ? jsonObject.getBoolean(key) : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean getBooleanVal(JSONObject jsonObject, String key, boolean defaultValue) {
        Boolean booleanVal = getBooleanVal(jsonObject, key);
        return booleanVal == null ? defaultValue : booleanVal;
    }

    @Nullable
    public static JSONObject getJsonObject(JSONObject jsonObject, String key) {
        try {
            return jsonObject.has(key) && !jsonObject.isNull(key) ? jsonObject.getJSONObject(key) : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    public static JSONObject getJsonObject(JSONObject jsonObject, String key, @NonNull JSONObject defaultValue) {
        JSONObject jsonObjectVal = getJsonObject(jsonObject, key);
        return jsonObjectVal == null ? defaultValue : jsonObjectVal;
    }

    @Nullable
    public static JSONArray getJsonArray(JSONObject jsonObject, String key) {
        try {
            return jsonObject.has(key) && !jsonObject.isNull(key) ? jsonObject.getJSONArray(key) : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    public static JSONArray getJsonArray(JSONObject jsonObject, String key, @NonNull JSONArray defaultValue) {
        JSONArray jsonArray = getJsonArray(jsonObject, key);
        return jsonArray == null ? defaultValue : jsonArray;
    }


    public static boolean isParsable(JSONObject jsonObject, String key) {
        return jsonObject != null && jsonObject.has(key) && !jsonObject.isNull(key);
    }

}
