package com.radiusnetworks.campaignkitreference;

import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.widget.TableRow;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.radiusnetworks.campaignkitreference.DetailActivity;
import com.radiusnetworks.campaignkitreference.MyApplication;
import com.radiusnetworks.campaignkitreference.R;

/**
 * The Main <code>Activity</code> for the CampaignKit's Demo Client.
 * 
 * <p>
 * A <code>ListFragment</code> is utilized in this class to display campaigns sent in from the
 * CampaignKitManager.getCurrentCampaigns() method.
 * 
 * 
 * 
 * @author Matt Tyler
 *
 */
public class MainActivity extends Activity {
	public static final String TAG = "MainActivity";
	Map<String,TableRow> rowMap = new HashMap<String,TableRow>();

	private boolean _visible = false;
	private static MyApplication _application;
	private Context _context;

	public static ArrayAdapter<String> listAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (_application == null){
			Log.d(TAG,"_application was null. initializing _application value");
			_application = (MyApplication) this.getApplication();
		}
		_application.setMainActivity(this);
		_context = this;
		setContentView(R.layout.activity_main);

        verifyBluetooth();
        googlePlayServicesConnected();
		
		findViewById(R.id.campaignsButton).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				//Sending to DetailActivity
				Intent intent = new Intent();
				intent.setClass(_context, DetailActivity.class);
				startActivity(intent);
				
			}
		});
		findViewById(R.id.campaignsButton).setVisibility((areCampaignsTriggeredNow())? View.VISIBLE : View.GONE);

	}

	/**
	 * Refreshes <code>Listview</code> with current campaign titles.
	 */
	public void refreshVisibleList() {
		runOnUiThread(new Runnable() {
			public void run() {
				findViewById(R.id.campaignsButton).setVisibility((areCampaignsTriggeredNow())? View.VISIBLE : View.GONE);
			}
		});
	}

	private boolean areCampaignsTriggeredNow(){
		if (_application == null){
			Log.d(TAG,"_application was null. initializing _application value");
			_application = (MyApplication) this.getApplication();
			_application.setMainActivity(this);
		}
		
		if (_application.getTriggeredCampaignArray() != null && _application.getTriggeredCampaignArray().size() >0){
			Log.d(TAG,"_application.getTriggeredCampaignArray().size() = "+_application.getTriggeredCampaignArray().size());
			return true;
		}
		
		return false;
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
