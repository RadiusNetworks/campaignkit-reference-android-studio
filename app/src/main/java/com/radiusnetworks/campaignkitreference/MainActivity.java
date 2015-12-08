package com.radiusnetworks.campaignkitreference;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * The Main <code>Activity</code> for the CampaignKit's Demo Client.
 * <p/>
 * <p/>
 * A <code>ListFragment</code> is utilized in this class to display campaigns sent in from the
 * CampaignKitManager.getFoundCampaigns() method.
 */
public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    /**
     * Request code to use when launching the location permission resolution activity
     */
    private static final int REQUEST_LOCATION_ACCESS = 1;

    /**
     * Store access to the button which displays the triggered Campaigns
     */
    private Button campaignsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MyApplication) getApplication()).setMainActivity(this);
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
        verifyBluetooth();
        googlePlayServicesConnected();
        togglePermissionFeatures();
    }

    /**
     * Called after the user has either denyed or granted our permission request.
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
                } else {
                    // Location access was denied, so we cannot enable geofences
                    Toast.makeText(
                            this,
                            "Both background beacon detection and geofence events are prevented.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
            return;
        }

        // We may already have permission so we need to check
        // If you are not using geofences you should request ACCESS_COARSE_LOCATION:
        // String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            // Location permission has already been granted
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
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean googlePlayServicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            // In debug mode, log the status
            Log.d(TAG, "Google Play services available");

            // Continue
            return true;

            // Google Play services was not available for some reason
        } else {

            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                    errorFragment.setDialog(dialog);
                    errorFragment.show(getFragmentManager(), TAG);
                } else {
                    dialog.show();
                }
            }
            return false;
        }
    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

}
