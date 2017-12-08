package com.radiusnetworks.campaignkitreference;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import java.util.EnumSet;

public final class PermissionUtil {
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
     * The basic flow for requesting permissions in Android 6.
     * <p/>
     * The only permission we need to request is access to location services. This is necessary
     * so we can detect beacons in the background - via {@link
     * android.Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_COARSE_LOCATION}. Additionally,
     * Google Play Service needs access to {@link
     * android.Manifest.permission#ACCESS_FINE_LOCATION ACCESS_FINE_LOCATION} for geofence
     * triggering.
     * <p/>
     * Since the permissions we need are critical. We ask upfront. As this is a sample app it
     * may, or may not, be clear why we need the permissions. Since this is only a reference we
     * are no providing any on boarding and defer the explanation until necessary.
     *
     * @see <a href="https://developer.android.com/training/permissions/index.html">Working with System Permissions</a>
     * @see <a href="https://www.google.com/design/spec/patterns/permissions.html">Patterns– Permissions</a>
     * @see <a href="https://www.youtube.com/watch?v=C8lUdPVSzDk">Runtime Permissions in Android 6.0 Marshmallow (Android Development Patterns Ep 3)</a>
     * @see <a href="https://www.youtube.com/watch?v=iZqDdvhTZj0">Android Marshmallow 6.0: Asking For Permission</a>
     */
    @TargetApi(Build.VERSION_CODES.M)
    public EnumSet<PermissionFeature> grantedPermissionFeatures(Activity activity) {
        // We may already have permission so we need to check
        // If you are not using geofences you should request ACCESS_COARSE_LOCATION:
        // String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        if (PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(permission)) {
            // Location permission has already been granted
            return EnumSet.of(PermissionFeature.GEOFENCES);
        }

        /*
         * Check if we should provide an additional rationale to the user if the permission was not
         * granted and the user would benefit from additional context for the use of the permission.
         *
         * Will return `false` if the permission is disabled on the device or if the user has
         * checked "Don't ask me again!". Will also be `false` the first time this permission
         * is being requested. Thus this returns `true` only if we've requested the
         * permission once before and were denied. This is a potential signal that the user
         * might be confused about the app behavior and why this permission is necessary.
         */
        if (activity.shouldShowRequestPermissionRationale(permission)) {
            Toast.makeText(
                    activity,
                    "Location access is needed to trigger both campaigns in the background and " +
                            "those attached to geofences",
                    Toast.LENGTH_LONG
            ).show();
        }
        activity.requestPermissions(new String[]{permission}, REQUEST_LOCATION_ACCESS);
        return EnumSet.noneOf(PermissionFeature.class);
    }

    public enum PermissionFeature {GEOFENCES}
}
