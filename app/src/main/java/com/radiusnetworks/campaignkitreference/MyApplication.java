package com.radiusnetworks.campaignkitreference;

import android.app.Application;
import android.util.Log;

import com.radiusnetworks.campaignkit.Campaign;
import com.radiusnetworks.campaignkit.CampaignKitManager;
import com.radiusnetworks.campaignkit.CampaignKitNotifier;
import com.radiusnetworks.campaignkit.CampaignNotificationBuilder;
import com.radiusnetworks.campaignkit.Configuration;
import com.radiusnetworks.campaignkit.Place;
import com.radiusnetworks.proximity.geofence.GooglePlayServicesException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

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

    /**
     * Storage for an instance of the manager
     */
    private static CampaignKitManager ckManager = null;

    /**
     * Object to use as a thread-safe lock
     */
    private static final Object ckManagerLock = new Object();

    /**
     * Current main activity for notifications.
     */
    private static volatile MainActivity mainActivity = null;

    /**
     * All campaigns with their beacon within range, in order of appearance
     */
    public static ArrayList<Campaign> triggeredCampaigns = new ArrayList<Campaign>();

    /**
     * Titles of all campaigns with their beacon within range, in same order
     */
    public static ArrayList<String> triggeredCampaignTitles = new ArrayList<String>();

    @Override
    public void onCreate() {
        super.onCreate();

        /*
         * The app is responsible for handling the singleton instance of the Campaign Kit manager.
         * To ensure we have a single instance we synchronize our creation process.
         *
         * While this is not necessary inside an `Application` subclass it is necessary if the
         * single manager instance is created inside an `Activity` or other Android/Java component.
         * We're including the pattern here to show a method of ensuring a singleton instance.
         */
        synchronized (ckManagerLock) {
            if (ckManager == null) {
                ckManager = CampaignKitManager.getInstance(this, loadConfig());
            }
        }

        try {
            ckManager.enableGeofences();
        } catch (GooglePlayServicesException gpse) {
            gpse.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ckManager.setNotifier(this);

        ckManager.start();
    }

    @Override
    public void didFindCampaign(Campaign campaign) {
        //adding Campaign to triggeredCampaignArray, this will force it to be shown on the triggeredCampaignList
        triggeredCampaigns.add(campaign);

        //sending notification or alert, depending on whether app is in background or foreground
        new CampaignNotificationBuilder(mainActivity, campaign)
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
        Log.d(TAG,"place distance: "+place.getDistance());


    }

    public void setMainActivity(MainActivity _mainActivity) {
        this.mainActivity = _mainActivity;
    }

    /**
     * Refreshes triggeredCampaignTitles <code>Arraylist</code> and returns it.
     * @return refreshed triggeredCampaignTitles <code>Arraylist</code>.
     */
    public ArrayList<String> getTriggeredCampaignTitlesList(){
        if (triggeredCampaigns != null){
            triggeredCampaignTitles.clear();
            for (Campaign c : triggeredCampaigns){
                triggeredCampaignTitles.add(c.getTitle());
            }
        }
        return triggeredCampaignTitles;
    }

    public ArrayList<Campaign> getTriggeredCampaignArray(){
        return triggeredCampaigns;
    }

    public Campaign getCampaignFromList(int positionOnList){
        if (triggeredCampaigns != null  &&  triggeredCampaigns.size() > positionOnList){
            return triggeredCampaigns.get(positionOnList);
        }
        return null;
    }

    /**
     * Refreshes <code>Listview</code> on the MainActivity to properly display
     * campaigns associated with newly triggered beacons.
     */
    private void refreshMainActivityList(){
        if (mainActivity != null) {
            mainActivity.refreshVisibleList();
        }
        else {
            Log.d(TAG, "Main activity not started yet.");
        }
    }

    public void removeCampaign(int position){
        ckManager.removeCampaign(getCampaignFromList(position));
        triggeredCampaigns.clear();
        triggeredCampaigns = ckManager.getFoundCampaigns();
        Log.d(TAG,"after removing. triggeredCampaignArray size = "+triggeredCampaigns.size());

        refreshMainActivityList();
    }


    /**
     * Records an analytics event that will be aggregated and reported within the Campaign Kit.
     *
     */
    public void setCampaignViewed(Campaign c){
        ckManager.setCampaignViewed(c);
    }

    private Configuration loadConfig() {
        Properties properties = new Properties();
        InputStream in = getClassLoader().getResourceAsStream("CampaignKit.properties");
        if (in == null) {
            throw new IllegalStateException("Unable to find CampaignKit.properties files");
        }
        try {
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load properties file!", e);
        }
        return new Configuration(properties);
    }
}
