package com.radiusnetworks.campaignkitreference;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.radiusnetworks.campaignkitreference.MainActivity;
import com.radiusnetworks.campaignkitreference.R;
import com.radiusnetworks.campaignkit.Campaign;
import com.radiusnetworks.campaignkit.CampaignKitNotifier;
import com.radiusnetworks.campaignkit.CampaignKitManager;
import com.radiusnetworks.campaignkit.CampaignNotificationBuilder;
import com.radiusnetworks.campaignkit.Place;
import com.radiusnetworks.proximity.geofence.GooglePlayServicesException;


import java.util.ArrayList;

/**
 * The <code>Application</code> class for the CampaignKit's Demo Client. It is ideal
 * to place interactions with the Campaign Kit here, due to the accessibility of the 
 * <code>Application</code> class from any Activity.
 * 
 * This class implements three required methods for the <code>CampaignKitNotifier, didFindCampaign, didSync</code>
 * and <code>didFailSync</code>. The <code>CampaignKitManager</code> constructor is called within this class's
 * onCreate method, as is necessary for <code>CampaignKit</code> functionality.
 * 
 * @author Matt Tyler
 */
public class MyApplication extends Application implements CampaignKitNotifier {
	public static final String TAG = "MyApplication";

	public static ArrayList<Campaign> triggeredCampaignArray = new ArrayList<Campaign>(); //all campaigns with their beacon within range, in order of appearance
	public static ArrayList<String> triggeredCampaignTitles = new ArrayList<String>(); //titles of all campaigns with their beacon within range, in same order
	public CampaignKitManager _ckManager;
	private MainActivity _mainActivity = null;

	
	@Override
	public void onCreate() {
		super.onCreate();

		_ckManager = CampaignKitManager.getInstanceForApplication(this);

		try{
			_ckManager.enableGeofences();
			Log.i(TAG,"onCreate. geofences enabled.");
		}catch(GooglePlayServicesException gpse){
			gpse.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		_ckManager.setNotifier(this);

		_ckManager.start();

	}

	@Override
	public void didFindCampaign(Campaign campaign) {
		//adding Campaign to triggeredCampaignArray, this will force it to be shown on the triggeredCampaignList
		triggeredCampaignArray.add(campaign);
		//triggeredCampaignArray.clear();
		//triggeredCampaignArray = _ckManager.getAllCampaigns();

		//sending notification or alert, depending on whether app is in background or foreground
		new CampaignNotificationBuilder(_mainActivity, campaign)
		.setSmallIcon(R.drawable.ic_launcher)
		.setOnClickActivity(DetailActivity.class)
		.show();
		//refreshing the visible list of campaigns
		refreshMainActivityList();
	}


	@Override
	public void didSync() {
		Log.i(TAG,"didSync.");
	}
	
	@Override
	public void didFailSync(Exception e) {
		Log.e(TAG,"didFailSync.");
		if (e != null)
			e.printStackTrace();
		
	}

	@Override
	public void didDetectPlace(Place place, CKEventType event) {
		Log.i(TAG,"didDetectPlace.  EventType: "+event.toString()+"  Place: "+place.toString());

		
	}
	
	public void setMainActivity(MainActivity _mainActivity) {
		this._mainActivity = _mainActivity;
	}

	/**
	 * Refreshes triggeredCampaignTitles <code>Arraylist</code> and returns it.
	 * @return refreshed triggeredCampaignTitles <code>Arraylist</code>.
	 */
	public ArrayList<String> getTriggeredCampaignTitlesList(){
		if (triggeredCampaignArray != null){
			triggeredCampaignTitles.clear();
			for (Campaign c : triggeredCampaignArray){
				triggeredCampaignTitles.add(c.getTitle());
			}
		}
		return triggeredCampaignTitles;
	}
	
	public ArrayList<Campaign> getTriggeredCampaignArray(){
		return triggeredCampaignArray;
	}
	
	public Campaign getCampaignFromList(int positionOnList){
		if (triggeredCampaignArray != null  &&  triggeredCampaignArray.size() > positionOnList){
			return triggeredCampaignArray.get(positionOnList);
		}
		return null;
	}

	/**
	 * Refreshes <code>Listview</code> on the MainActivity to properly display 
	 * campaigns associated with newly triggered beacons.
	 */
	private void refreshMainActivityList(){
		if (_mainActivity != null) {
			_mainActivity.refreshVisibleList();			
		}
		else {
			Log.d(TAG, "Main activity not started yet.");
		}
	}
	
	public void removeCampaign(int position){
		_ckManager.removeCampaign(getCampaignFromList(position));
		triggeredCampaignArray.clear();
		triggeredCampaignArray = _ckManager.getAllCampaigns();
		Log.d(TAG,"after removing. triggeredCampaignArray size = "+triggeredCampaignArray.size());

		refreshMainActivityList();
	}
	

    /**
     * Records an analytics event that will be aggregated and reported within the Campaign Kit.
     *
     */
    public void recordAnalytics(CampaignKitNotifier.CKAnalyticsType type, Campaign campaign, Place place){
    	_ckManager.recordAnalytics(type, campaign, place);
    }
    public void recordAnalytics(CampaignKitNotifier.CKAnalyticsType type, String campaignId , String placeId){
    	_ckManager.recordAnalytics(type, campaignId, placeId);
    }
    public void recordAnalytics(CampaignKitNotifier.CKAnalyticsType type, int campaignIdAsInt, int placeIdAsInt){
    	_ckManager.recordAnalytics(type, campaignIdAsInt, placeIdAsInt);
    }
    public void recordAnalytics(CampaignKitNotifier.CKAnalyticsType type, Campaign campaign){
    	_ckManager.recordAnalytics(type, campaign);
    }
    public void recordAnalytics(CampaignKitNotifier.CKAnalyticsType type, String campaignId){
    	_ckManager.recordAnalytics(type, campaignId);
    }
    public void recordAnalytics(CampaignKitNotifier.CKAnalyticsType type, int campaignIdAsInt){
    	_ckManager.recordAnalytics(type, campaignIdAsInt);
    }

}
