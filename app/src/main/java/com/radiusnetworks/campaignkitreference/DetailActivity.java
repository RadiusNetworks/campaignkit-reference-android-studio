package com.radiusnetworks.campaignkitreference;


import java.util.ArrayList;

import com.example.android.slidingtabscolors.SlidingTabsColorsFragment;
import com.radiusnetworks.campaignkitreference.R;
import com.radiusnetworks.campaignkit.Campaign;
import com.radiusnetworks.campaignkit.CampaignKitNotifier;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class DetailActivity extends FragmentActivity {
	protected static final String TAG = "DetailActivity";
	public static final String KEY_CAMPAIGN_ID = "campaignId";
	
	private DetailActivity _instance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try{
			_instance = this;
			setContentView(R.layout.activity_detail);
			getActionBar().setDisplayHomeAsUpEnabled(true);

			

		}catch(Exception e ){e.printStackTrace();}
	}
	
	@Override 
	public void onResume(){
		super.onResume();
		refreshList(getIntent().getExtras());
	}

	/**
	 * Called when the activity has been opened when it was already running.
	 * In this case, this usually occurs when the user tapped on an alert created by CampaignNotificationBuilder.
	 */
	@Override
	public void onNewIntent (Intent intent){		
		Log.i(TAG,"onNewIntent.");
        if (intent != null && intent.getExtras() != null && intent.getExtras().getString(KEY_CAMPAIGN_ID,"") != "") {
           	Log.i(TAG,"onNewIntent. campaignId = "+intent.getExtras().getString(KEY_CAMPAIGN_ID,""));
        	refreshList(intent.getExtras());
        }

	}
	public void refreshList(){
		refreshList(null);
	}

	private void refreshList(Bundle b){
		ArrayList<Campaign> campaignArray =  getCampaignArray();

		if (campaignArray != null){

		
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
	        SlidingTabsColorsFragment fragment = new SlidingTabsColorsFragment();
	        
	        //not working yet
	        if (b != null && b.getString(KEY_CAMPAIGN_ID,"") != ""){
	        	Log.d(TAG,"refreshing list with campaignId = "+b.getString(KEY_CAMPAIGN_ID,""));
	            Bundle args = new Bundle();
	            args.putString(KEY_CAMPAIGN_ID, b.getString(KEY_CAMPAIGN_ID,""));
	        	fragment.setArguments(args);
	        }
	        
	        transaction.replace(R.id.sample_content_fragment, fragment);
	        transaction.commit();
			
		} else Log.e(TAG,"CAMPAIGNARRAY == NULL!");

	}
	
	public ArrayList<Campaign> getCampaignArray(){
		return ((MyApplication) this.getApplication()).getTriggeredCampaignArray();
	}
	
}