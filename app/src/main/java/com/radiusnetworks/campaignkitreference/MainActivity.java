package com.radiusnetworks.campaignkitreference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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
     * Store access to the button which displays the triggered Campaigns
     */
    private Button campaignsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MyApplication) getApplication()).setMainActivity(this);
        setContentView(R.layout.activity_main);

        verifyBluetooth();
        googlePlayServicesConnected();

        final Context that = this;
        campaignsButton = (Button) findViewById(R.id.campaignsButton);
        campaignsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Sending to DetailActivity
                Intent intent = new Intent();
                intent.setClass(_context, DetailActivity.class);
                startActivity(intent);
            }
        });
        updateCampaignsVisibility();
    }

    /**
     * Refreshes <code>Listview</code> with current campaign titles.
     */
    public void refreshVisibleList() {
        runOnUiThread(new Runnable() {
            public void run() {
                updateCampaignsVisibility();
            }
        });
    }

    private boolean areCampaignsTriggeredNow(){
        MyApplication app = (MyApplication) getApplication();
        return !app.getTriggeredCampaignArray().isEmpty();
    }

    private void updateCampaignsVisibility() {
        int visibility = areCampaignsTriggeredNow() ? View.VISIBLE : View.GONE;
        campaignsButton.setVisibility(visibility);
    }

    private void verifyBluetooth() {

        try {
            if (!org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                Log.e(TAG,"Bluetooth not enabled.");
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                if (android.os.Build.VERSION.SDK_INT >= 17) {
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            dialog.dismiss();
                        }
                    });
                } else {
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                        }
                    });
                }
                builder.show();
            }
        }
        catch (RuntimeException e) {
            Log.e(TAG,"Bluetooth LE not available.");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            if (android.os.Build.VERSION.SDK_INT >= 17) {
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
            } else {
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
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
