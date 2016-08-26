package com.radiusnetworks.campaignkitreference;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import com.radiusnetworks.proximity.geofence.GooglePlayServicesException;

/**
 * The Main <code>Activity</code> for the CampaignKit's Demo Client.
 * <p/>
 * A <code>ListFragment</code> is utilized in this class to display campaigns sent in from the
 * CampaignKitManager.getFoundCampaigns() method.
 */
public class MainActivity extends FragmentActivity {
    public static final String TAG = "MainActivity";

    /**
     * Unique tag for the error code in the dialog fragment bundle
     */
    private static final String DIALOG_ERROR = "dialog_error";

    /**
     * Request code to use when launching the location permission resolution activity
     */
    private static final int REQUEST_LOCATION_ACCESS = 1;

    /**
     * Request code to use when launching the Google Play Services resolution activity
     */
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    /**
     * Name of the saved {@link #resolvingError} value in the saved bundle.
     */
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    /**
     * Store access to the button which displays the triggered Campaigns
     */
    private Button campaignsButton;

    /**
     * Bool to track whether the app is already resolving a Google Play Services error
     */
    private boolean resolvingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * While the Google Play Services dialog is showing the user may rotate the screen or
         * perform another action which causes the activity to be recreated. We reload the state
         * we were in so that when `onStart()` is called again we know to abort the permission
         * check as it is still pending.
         */
        resolvingError = savedInstanceState != null &&
                savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);


        setContentView(R.layout.activity_main);

        final Context that = this;
        campaignsButton = (Button) findViewById(R.id.campaignsButton);
        campaignsButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(that, DetailActivity.class);
                        startActivity(intent);
                    }
                }
        );
        updateCampaignsVisibility();
    }

    /**
     * Verify critical permissions and settings every time the app is brought to the foreground.
     */
    @Override
    protected void onStart() {
        super.onStart();
        ((MyApplication) getApplication()).setMainActivity(this);
        verifyBluetooth();
        togglePermissionFeatures();
    }

    @Override
    protected void onStop() {
        clearReferences();
        super.onStop();
    }

    private void clearReferences() {
        MyApplication app = (MyApplication) getApplication();
        Activity currentActivity = app.getMainActivity();
        if (this.equals(currentActivity)) {
            app.setMainActivity(null);
        }
    }

    /**
     * Handle the result from the Google Play Services error dialog.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            resolvingError = false;
            if (resultCode == RESULT_OK) {
                enableGeofences();
            } else {
                disableGeofences();
            }
        }
    }

    /**
     * Save the state of any outstanding Google Play Services requests.
     * <p/>
     * To avoid executing the code in {@link #togglePermissionFeatures()} while a previous
     * attempt to resolve an error is ongoing, we need to retain a boolean that tracks whether
     * the app is already attempting to resolve an error.
     * <p/>
     * In {@code togglePermissionFeatures()} we set a boolean to {@code true} each time we call
     * it or when we display the dialog from {@link
     * GoogleApiAvailability#getErrorDialog(Activity, int, int)}. Then when we receive our {@link
     * #RESULT_OK} response we set it back to {@code false}.
     * <p/>
     * To keep track of the boolean across activity restarts (such as when the user rotates the
     * screen), we save the boolean in the saved instance data.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, resolvingError);
    }

    /**
     * Called after the user has either denied or granted our permission request.
     *
     * @see #togglePermissionFeatures()
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_ACCESS:
                // Received permission result for location access

                // Check if the only required permission has been granted
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location access has been granted, geofences can be enabled
                    enableGeofences();
                } else {
                    // Location access was denied, so we cannot enable geofences
                    Toast.makeText(
                            this,
                            "Both background beacon detection and geofence events are prevented.",
                            Toast.LENGTH_SHORT
                    ).show();
                    disableGeofences();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Called from ErrorDialogFragment when the dialog is dismissed.
     */
    public void onGooglePlayDialogDismissed() {
        resolvingError = false;
    }



    /**
     * Refreshes <code>Listview</code> with current campaign titles.
     */
    public void refreshVisibleList() {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        updateCampaignsVisibility();
                    }
                }
        );
    }

    private boolean areCampaignsTriggeredNow() {
        MyApplication app = (MyApplication) getApplication();
        return !app.getTriggeredCampaignArray().isEmpty();
    }

    private void disableGeofences() {
        MyApplication.disableGeofences();
    }

    /**
     * Check Google Play services, location status, and enable geofences in Campaign Kit.
     * <p/>
     * It is our job (the app) to ensure that Google Play services is available. If it is not then
     * attempting to enable geofences in Campaign Kit will fail, throwing a {@link
     * GooglePlayServicesException}. This will happen in the following conditions:
     * <ul>
     *     <li>We forget to include Google Play services as a dependency of our application</li>
     *     <li>The device the app is running on does not have Google Play services</li>
     *     <li>The device the app is running on has an outdated version of Google Play services</li>
     * </ul>
     * It is our responsibility to handle this, as we (the app), are the only one in a position
     * to decide how to behave if this service is not available.
     * <p/>
     * In this example, we've decided to check to make sure the service is available. In the event
     * we think the service is available, but enabling geofences still fails, we log the error and
     * continue without geofences.
     * <p/>
     * See {@link #isGooglePlayServicesAvailable()} for how we handle the cases where the device
     * doesn't have Google Play services or the version is out of date.
     */
    private void enableGeofences() {
        /*
         * It is our job (the app) to ensure that Google Play services is available. If it is not
         * then attempting to enable geofences in Campaign Kit will fail, throwing a
         * GooglePlayServicesException. This will happen in the following conditions:
         *
         * - We forget to include Google Play services as a dependency of our applicaiton
         * - The device the app is running on does not have Google Play services
         * - The device the app is running on has an outdated version of Google Play services
         *
         * It is our responsibility to handle this, as we (the app), are the only one in a position
         * to decide how to behave if this service is not available.
         *
         * In this example, we've decided to check to make sure the service is available. In the
         * event we think the service is available, but enabling geofences still fails, we log the
         * error and continue without geofences.
         */
        if (isGooglePlayServicesAvailable() && verifyLocationServices()) {
            // As a safety mechanism, `enableGeofences()` throws a checked exception in case the
            // app does not properly handle Google Play support.
            try {
                MyApplication.enableGeofences();
            } catch (GooglePlayServicesException e) {
                Log.e(TAG, "Expected Google Play to be available but enabling geofences failed", e);
            }
        }
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
     * @see GoogleApiAvailability#isGooglePlayServicesAvailable(Context)
     */
    private boolean isGooglePlayServicesAvailable() {
        if (resolvingError) {
            // Already attempting to resolve an error.
            return false;
        }

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        // Check that Google Play services is available
        int statusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        switch (statusCode) {
            case ConnectionResult.SUCCESS:
                Log.d(TAG, "Google Play Service available");
                return true;
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_UPDATING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
            case ConnectionResult.SERVICE_DISABLED:
            case ConnectionResult.SERVICE_INVALID:
                // Taking the easy way out: log it.
                Log.w(TAG, apiAvailability.getErrorString(statusCode));
                showGooglePlayErrorDialog(statusCode);
                resolvingError = true;
        }

        return false;
    }

    /**
     * Display a dialog to the user explaining the Google Play Service error.
     * <p/>
     * Try to get the error dialog from {@link GoogleApiAvailability} so it can properly provide a
     * consistent experience for the user.
     *
     * @param errorCode
     *         The code to provide to {@link GoogleApiAvailability} to tell it which dialog message
     *         is needed.
     */
    private void showGooglePlayErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();

        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
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
    private void togglePermissionFeatures() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // All permissions requested at install time
            enableGeofences();
            return;
        }

        // We may already have permission so we need to check
        // If you are not using geofences you should request ACCESS_COARSE_LOCATION:
        // String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            // Location permission has already been granted
            enableGeofences();
            return;
        }

        // Location access has not been granted

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
        if (shouldShowRequestPermissionRationale(permission)) {
            Toast.makeText(
                    this,
                    "Location access is needed to trigger both campaigns in the background and " +
                            "those attached to geofences",
                    Toast.LENGTH_LONG
            ).show();
        }
        requestPermissions(new String[]{permission}, REQUEST_LOCATION_ACCESS);
    }

    private void updateCampaignsVisibility() {
        int visibility = areCampaignsTriggeredNow() ? View.VISIBLE : View.GONE;
        campaignsButton.setVisibility(visibility);
    }

    /**
     * Check the phone settings to see if bluetooth is turned on.
     */
    private void verifyBluetooth() {
        try {
            if (!org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this)
                                                   .checkAvailability()) {
                Log.e(TAG, "Bluetooth not enabled.");
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage(
                        "Please enable bluetooth in settings and restart this application."
                );
                builder.setPositiveButton(android.R.string.ok, null);
                if (android.os.Build.VERSION.SDK_INT >= 17) {
                    builder.setOnDismissListener(
                            new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    dialog.dismiss();
                                }
                            }
                    );
                } else {
                    builder.setOnCancelListener(
                            new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    dialog.dismiss();
                                }
                            }
                    );
                }
                builder.show();
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Bluetooth LE not available.");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            if (android.os.Build.VERSION.SDK_INT >= 17) {
                builder.setOnDismissListener(
                        new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                dialog.dismiss();
                            }
                        }
                );
            } else {
                builder.setOnCancelListener(
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                dialog.dismiss();
                            }
                        }
                );
            }
            builder.show();
        }
    }

    /**
     * Check the phone settings to see if location services is turned on.
     */
    private boolean verifyLocationServices() {
        // TODO: Implement this
        return true;
    }

    /**
     * A fragment to display an error dialog
     */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    getActivity(),
                    errorCode,
                    REQUEST_RESOLVE_ERROR
            );
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity) getActivity()).onGooglePlayDialogDismissed();
        }
    }
}
