package com.ixigo.android.sdk;

import java.util.Map;

/**
 * Created by bharat on 10/04/18.
 */

public interface EventCallback {

    void onEvent(String eventName, Map<String, Object> eventPropertyMap);
}
