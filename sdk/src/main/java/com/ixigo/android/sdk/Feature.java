package com.ixigo.android.sdk;

import java.io.Serializable;

/**
 * Created by bharat on 05/04/18.
 */

public class Feature implements Serializable {

    public static Feature FLIGHT_SEARCH = new Feature("/pwa/initialpage?page=FLIGHT_HOME", true, false);
    public static Feature TRIPS = new Feature("/pwa/initialpage?page=FLIGHT_TRIPS", false, false);
    public static Feature WALLET = new Feature("/pwa/initialpage?page=IXIGO_MONEY", false, false);

    private String path;
    private boolean geolocationEnabled;
    private boolean progressBarEnabled;

    public Feature(String path, boolean geolocationEnabled, boolean progressBarEnabled) {
        this.path = path;
        this.geolocationEnabled = geolocationEnabled;
        this.progressBarEnabled = progressBarEnabled;
    }

    public Feature(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public boolean isGeolocationEnabled() {
        return geolocationEnabled;
    }

    public void setGeolocationEnabled(boolean geolocationEnabled) {
        this.geolocationEnabled = geolocationEnabled;
    }

    public boolean isProgressBarEnabled() {
        return progressBarEnabled;
    }

    public void setProgressBarEnabled(boolean progressBarEnabled) {
        this.progressBarEnabled = progressBarEnabled;
    }
}
