package com.radiusnetworks.campaignkitreference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.radiusnetworks.proximity.geofence.GooglePlayServicesException;
import com.radiusnetworks.campaignkitreference.PermissionUtil.PermissionFeature;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * The Main <code>Activity</code> for the CampaignKit's Demo Client.
 * <p/>
 * A <code>ListFragment</code> is utilized in this class to display campaigns sent in from the
 * CampaignKitManager.getFoundCampaigns() method.
 */
public class MainActivity extends FragmentActivity {
    private static final String TAG = BuildConfig.FLAVOR + "-MainActivity";

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
        EnumSet<PermissionFeature> features = PermissionUtil.getInstance()
                                                            .grantedPermissionFeatures(this);
        if (features.contains(PermissionFeature.GEOFENCES)) {
            // Location permission has already been granted
            enableGeofences();
        } else {
            // Location access has not been granted
            disableGeofences();
        }
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
     * To avoid executing the code in {@link PermissionUtil#grantedPermissionFeatures(Activity)}
     * while a previous attempt to resolve an error is ongoing, we need to retain a boolean that
     * tracks whether the app is already attempting to resolve an error.
     * <p/>
     * In {@code togglePermissionFeatures()} we set a boolean to {@code true} each time we call
     * it or when we display the dialog from {@link #isGooglePlayServicesAvailable()}. Then when we
     * receive our {@link #RESULT_OK} response we set it back to {@code false}.
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
     * @see PermissionUtil#grantedPermissionFeatures(Activity)
     */
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtil.REQUEST_LOCATION_ACCESS:
                // Received permission result for location access

                // Check if the only required permission has been granted
                if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
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
                Log.w(
                        TAG,
                        "Unknown Permission Result: " + requestCode + " " +
                                Arrays.toString(permissions)
                );
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
     * If the service is not available it could be due to several reasons. We use the utility
     * class provided to pop a notification to the end user with the error message.
     *
     * @return {@code true} if Google Play services is available, otherwise {@code false}
     * @see <a href="https://developers.google.com/android/guides/setup">Setting up Google Play
     * Services</a>
     * @see GooglePlayUtil
     */
    private boolean isGooglePlayServicesAvailable() {
        if (resolvingError) {
            // Already attempting to resolve an error.
            return false;
        }
        GooglePlayUtil apiAvailability = GooglePlayUtil.getInstance();
        int statusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (GooglePlayUtil.SUCCESS == statusCode) {
            resolvingError = false;
        } else {
            resolvingError = true;
            apiAvailability.showErrorDialogFragment(
                    this,
                    statusCode,
                    REQUEST_RESOLVE_ERROR,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            onGooglePlayDialogDismissed();
                        }
                    }
            );
        }
        return !resolvingError;
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
}
