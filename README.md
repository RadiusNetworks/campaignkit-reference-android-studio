Campaign Kit for Android Reference App
======================================

This is a reference app for the Campaign Kit Library that is structured to run using the Android Studio Bundle. If you are using the Eclipse ADT with Android SDK, please use [this reference app instead](https://github.com/RadiusNetworks/campaignkit-reference-android).

Requirements for use:

* Android API Level 18 or higher to use AltBeacon features.

* Android API Level 9 or higher to use Geofencing features.

* Google Play Services library version 5.+ set as a dependency.

* `CampaignKit.properties` file downloaded from https://campaignkit.radiusnetworks.com.


## Setup Instructions

* Open Android Studio v1+.0 with gradle 2.2.1 If you don't have those yet, you can find them [here](https://developer.android.com/sdk/installing/studio.html) and [here](https://services.gradle.org/distributions/gradle-2.2.1-all.zip).

* Import the campaignkit-reference-android-studio project into Android Studio.

* Go to https://campaignkit.radiusnetworks.com and create a new kit with some sample content. Once completed, from the overview page of your kit click the small button with the Android robot icon at the top right. This will download your `CampaignKit.properties` file. Copy this file into the [`app/src/main/assets`](app/src/main/assets) folder overwriting the existing file.

At this point the project will run, granted you have the proper Android build tools and APIs installed into your Android SDK Manager. This project is currently set to Android API level 23, so please install that through your SDK Manager.
