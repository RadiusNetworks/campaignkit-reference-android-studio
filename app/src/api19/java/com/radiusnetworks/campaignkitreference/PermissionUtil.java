package com.radiusnetworks.campaignkitreference;

import android.app.Activity;

import java.util.EnumSet;

class PermissionUtil {
    /**
     * Request code to use when launching the location permission resolution activity
     */
    static final int REQUEST_LOCATION_ACCESS = 1;

    /**
     * Singleton since of this utility class.
     */
    private static final PermissionUtil INSTANCE = new PermissionUtil();

    private PermissionUtil() {
    }

    public static PermissionUtil getInstance() {
        return INSTANCE;
    }

    /**
     * Prior to Android M (API 23) location permissions were granted at install time.
     */
    public EnumSet<PermissionFeature> grantedPermissionFeatures(Activity activity) {
        return EnumSet.allOf(PermissionFeature.class);
    }

    public enum PermissionFeature {GEOFENCES}
}
