package com.radiusnetworks.campaignkitreference;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Helper class for verifying that the Google Play services APK is available and up-to-date on
 * this device.
 * <p>
 * This is a wrapper class to support different build flavors support different Google Play services
 * versions with different APIs.
 */
public final class GooglePlayUtil {
    private static final String TAG = BuildConfig.FLAVOR + "-GooglePlayUtil";

    private static final GooglePlayUtil INSTANCE = new GooglePlayUtil();

    public static final int SUCCESS = ConnectionResult.SUCCESS;

    public static GooglePlayUtil getInstance() {
        return INSTANCE;
    }

    private GooglePlayUtil() {
    }

    /**
     * Verify that Google Play services is available.
     * <p/>
     * If the service is not available it could be due to several reasons. We take the easy way out
     * in this demo and simply log the error. We then use the utility class provided to pop a
     * notification to the end user with the message.
     * <p/>
     * Google Play services controls the text and content of this notification. We could roll our
     * own notification, display a dialog (which would require an Activity context), or do something
     * else. This is why it is our (the app) responsibility to make this decision and not left up to
     * Campaign Kit.
     *
     * @return {@code true} if Google Play services is available, otherwise {@code false}
     * @see <a href="https://developers.google.com/android/guides/setup">Setting up Google Play
     * Services</a>
     */
    public int isGooglePlayServicesAvailable(@NonNull Context c) {
        // Check that Google Play services is available
        int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(c);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == statusCode) {
            // In debug mode, log the status
            Log.d(TAG, "Google Play services available");
        } else {
            // Taking the easy way out: log it.
            Log.w(TAG, GooglePlayServicesUtil.getErrorString(statusCode));
        }
        return statusCode;
    }

    public void showErrorDialogFragment(@NonNull Activity activity,
                                        int errorCode,
                                        int requestCode,
                                        @NonNull DialogInterface.OnCancelListener cancelListener) {
        GooglePlayServicesUtil.showErrorDialogFragment(
                errorCode,
                activity,
                requestCode,
                cancelListener
        );
    }
}
