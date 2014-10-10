package com.radiusnetworks.campaignkitreference;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TableRow;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;

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
	static final String RADIUS_UUID = "842AF9C4-08F5-11E3-9282-F23C91AEC05E";


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
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						dialog.dismiss();

						//closing application
						//finish();
						//System.exit(0);					
					}					
				});
				builder.show();
			}			
		}
		catch (RuntimeException e) {
			Log.e(TAG,"Bluetooth LE not available.");
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth LE not available");			
			builder.setMessage("Sorry, this device does not support Bluetooth LE.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					dialog.dismiss();

					//closing application
					//finish();
					//System.exit(0);
				}

			});
			builder.show();

		}

	}
}
