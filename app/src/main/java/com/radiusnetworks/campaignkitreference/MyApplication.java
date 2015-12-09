package com.radiusnetworks.campaignkitreference;

import android.app.Application;
import android.util.Log;

import com.radiusnetworks.campaignkit.Campaign;
import com.radiusnetworks.campaignkit.CampaignKitManager;
import com.radiusnetworks.campaignkit.CampaignKitNotifier;
import com.radiusnetworks.campaignkit.CampaignNotificationBuilder;
import com.radiusnetworks.campaignkit.Configuration;
import com.radiusnetworks.campaignkit.Content;
import com.radiusnetworks.campaignkit.Place;
import com.radiusnetworks.proximity.geofence.GooglePlayServicesException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * The {@link Application} class for the Campaign Kit Demo Client.
 * <p/>
 * This can be a good place to interact with Campaign Kit due to a single instance being created for
 * the lifetime of the app. Additionally, the {@link android.app.Activity Activity} classes have
 * access to this instance as well.
 * <p/>
 * This implements the four required methods for Campaign Kit event notifications:
 * {@link CampaignKitNotifier#didDetectPlace(Place, CKEventType)},
 * {@link CampaignKitNotifier#didFindCampaign(Campaign)},
 * {@link CampaignKitNotifier#didFailSync(Exception)}, and {@link CampaignKitNotifier#didSync()}.
 * These are the hooks into the campaign event lifecycle.
 *
 * @see {@link CampaignKitManager}
 * @see {@link CampaignKitNotifier}
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
    public static ArrayList<Campaign> triggeredCampaigns = new ArrayList<>();

    /**
     * Titles of all campaigns with their beacon within range, in same order
     */
    public static ArrayList<String> triggeredCampaignTitles = new ArrayList<>();

    /**
     * Setup the application including the Campaign Kit manager.
     * <p/>
     * It is the job of the application to ensure that Google Play services is available before
     * enabling geofences in Campaign Kit.
     * <p/>
     * A good place to do this is when we set the Campaign Kit manager instance. However there are
     * issues with this decision. See the notes in {@link #servicesConnected()} for details.
     *
     * @see #servicesConnected()
     * @see <a href="https://developer.android.com/google/play-services/setup.html">
     *          Setup Google Play services
     *      </a>
     */
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
        /*
         * Set the desired callback before calling `start()`.
         *
         * We can set this after calling `start()`. However, this means we will miss any
         * notifications posted in the time between those actions.
         */
        ckManager.setNotifier(this);

        /*
         * Now that we potentially have geofences setup and our notifier registered, we are ready
         * to start Campaign Kit.
         */
        ckManager.start();
    }

    public static void enableGeofences() throws GooglePlayServicesException {
        // As a safety mechanism, `enableGeofences()` throws a checked exception in case the
        // app does not properly handle Google Play support.
        ckManager.enableGeofences();
    }

    public static void disableGeofences() {
        ckManager.disableGeofences();
    }

    /**
     * Called when a {@link Campaign} has been found.
     * <p/>
     * This will be called the first time a {@code Campaign} is found. It may also be called again
     * with the same {@code Campaign} if a registered {@code Place} is re-entered after the
     * {@linkplain Campaign#getCanDetectAfter() recurrence threshold} has passed.
     *
     * @param campaign
     *         The {@link Campaign} found after entering a configured region.
     */
    @Override
    public void didFindCampaign(Campaign campaign) {
        // Force campaign to be shown in the found list
        triggeredCampaigns.add(campaign);

        // Send notification or alert based on if the app is in background or foreground
        new CampaignNotificationBuilder(mainActivity, campaign)
                .setSmallIcon(R.drawable.ic_launcher)
                .setOnClickActivity(DetailActivity.class)
                .show();

        // Make sure the UI is aware of the update
        refreshMainActivityList();
    }

    /**
     * Called when data has been sync'd with the Campaign Kit server.
     * <p/>
     * This is a good place to do any in app cleanup related to potentially modified content,
     * modified campaigns (e.g. extended promotion time periods), and/or expired/disabled campaigns.
     */
    @Override
    public void didSync() {
        Log.i(TAG, "didSync.");
    }

    /**
     * Called when syncing with the Campaign Kit server failed.
     *
     * @param e
     *         The exception encountered while syncing
     */
    @Override
    public void didFailSync(Exception e) {
        if (e == null) {
            Log.e(TAG, "didFailSync.");
        } else {
            Log.e(TAG, "didFailSync.", e);
        }
    }

    /**
     * Called on every {@link Place} related event.
     * <p/>
     * This is where more complex logic around {@link Content} and {@link Campaign} may take place.
     * In addition to {@link #didFindCampaign(Campaign)} being called, this will be called with a
     * {@link CKEventType#CKEventDidEnterRegion} event when {@code place} triggers a {@code
     * Campaign}.
     *
     * @param place
     *         The {@link Place} where {@code event} occurred
     * @param event
     *         Type of event which occurred
     * @see CKEventType
     */
    @Override
    public void didDetectPlace(Place place, CKEventType event) {
        Log.i(
                TAG,
                "didDetectPlace.  EventType: " + event.toString() + "  Place: " + place.toString()
        );
        Log.d(TAG, "place distance: " + place.getDistance());
    }

    /**
     * Register activity to for displaying campaign related messages.
     *
     * @param activity
     *         {@code MainActivity} instance to use for displaying in app alerts after finding a
     *         {@link Campaign}.
     * @see #didFindCampaign(Campaign)
     */
    public void setMainActivity(MainActivity activity) {
        mainActivity = activity;
    }

    /**
     * Refreshes {@link #triggeredCampaignTitles} before returning it.
     *
     * @return The modifiable list of current campaign titles.
     */
    public ArrayList<String> getTriggeredCampaignTitlesList() {
        if (triggeredCampaigns != null) {
            triggeredCampaignTitles.clear();
            for (Campaign c : triggeredCampaigns) {
                triggeredCampaignTitles.add(c.getTitle());
            }
        }
        return triggeredCampaignTitles;
    }

    /**
     * List of found {@link Campaign}s.
     *
     * @return The modifiable list of found campaigns.
     */
    public ArrayList<Campaign> getTriggeredCampaignArray() {
        return triggeredCampaigns;
    }

    /**
     * Helper for accessing a {@link Campaign} base on when it was found.
     * <p/>
     * This is a helpful wrapper around directly accessing the campaign list. It handles the {@code
     * null} checks and ensures the campaign list isn't exceeded.
     *
     * @param positionOnList
     *         The nth found {@link Campaign}. This is zero indexed, so the 0th found campaign was
     *         found first.
     * @return The found {@link Campaign} or {@code null} if there are no found campaigns or the
     * position exceeded the bounds.
     */
    public Campaign getCampaignFromList(int positionOnList) {
        if (triggeredCampaigns != null && triggeredCampaigns.size() > positionOnList) {
            return triggeredCampaigns.get(positionOnList);
        }
        return null;
    }

    /**
     * Refreshes the list of found campaigns on the {@link MainActivity}.
     */
    private void refreshMainActivityList() {
        if (mainActivity != null) {
            mainActivity.refreshVisibleList();
        } else {
            Log.d(TAG, "Main activity not started yet.");
        }
    }

    /**
     * Removes the nth found campaign.
     *
     * @param position
     *         The nth found {@link Campaign}. This is zero indexed, so the 0th found campaign was
     *         found first.
     */
    public void removeCampaign(int position) {
        ckManager.removeCampaign(getCampaignFromList(position));
        triggeredCampaigns.clear();
        triggeredCampaigns = ckManager.getFoundCampaigns();
        Log.d(TAG, "after removing. triggeredCampaignArray size = " + triggeredCampaigns.size());

        refreshMainActivityList();
    }

    /**
     * Mark a {@link Campaign} as having been viewed by the device.
     * <p/>
     * Asks the Campaign Kit manager to record an viewed analytic event for the {@code Campaign}.
     * The analytics are aggregated by the manager and reported to the Campaign Kit servers. After
     * upload the analytics will appear on the kit dashboard.
     */
    public void setCampaignViewed(Campaign c) {
        ckManager.setCampaignViewed(c);
    }

    /**
     * Generate the app's Campaign Kit configuration.
     * <p/>
     * This loads the properties for a kit from a {@code .properties} file bundled in the app. This
     * file was be downloaded from the <a href="https://campaignkit.radiusnetworks.com">Campaign
     * Kit server</a>.
     * <p/>
     * For newer Android applications, the file can be added to the {@code /assets} folder:
     * <p/>
     * <pre>
     * {@code Properties properties = new Properties();
     * try {
     *     properties.load(getAssets().open("CampaignKit.properties"));
     * } catch (IOException e) {
     *     throw new IllegalStateException("Unable to load properties file!", e);
     * }
     * new Configuration(properties);
     * }
     * </pre>
     * <p/>
     * For older Android applications, or if you just prefer using Java resources, the file can be
     * added to the {@code /resources} folder:
     * <p/>
     * <pre>
     * {@code Properties properties = new Properties();
     * InputStream in = getClassLoader().getResourceAsStream("CampaignKit.properties");
     * if (in == null) {
     *     throw new IllegalStateException("Unable to find CampaignKit.properties files");
     * }
     * try {
     *     properties.load(in);
     * } catch (IOException e) {
     *     throw new IllegalStateException("Unable to load properties file!", e);
     * }
     * new Configuration(properties);
     * }
     * </pre>
     * <p/>
     * These details could just as easily been statically compiled into the app. They also could
     * have been downloaded from a 3rd party server.
     *
     * @return A new {@link Configuration} configured for the app's kit.
     */
    private Configuration loadConfig() {
        Properties properties = new Properties();
        try {
            properties.load(getAssets().open("CampaignKit.properties"));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load properties file!", e);
        }
        return new Configuration(properties);
    }
}
