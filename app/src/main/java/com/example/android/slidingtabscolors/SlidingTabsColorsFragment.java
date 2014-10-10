/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.slidingtabscolors;

import com.example.android.common.view.SlidingTabLayout;
import com.radiusnetworks.campaignkit.Campaign;
import com.radiusnetworks.campaignkit.CampaignKitNotifier;
import com.radiusnetworks.campaignkitreference.DetailActivity;
import com.radiusnetworks.campaignkitreference.MyApplication;
import com.radiusnetworks.campaignkitreference.R;
import com.radiusnetworks.campaignkitreference.R.id;
import com.radiusnetworks.campaignkitreference.R.layout;
import com.radiusnetworks.campaignkitreference.R.string;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * A basic sample which shows how to use {@link com.example.android.common.view.SlidingTabLayout}
 * to display a custom {@link ViewPager} title strip which gives continuous feedback to the user
 * when scrolling.
 */
public class SlidingTabsColorsFragment extends Fragment {

	/**
	 * This class represents a tab to be displayed by {@link ViewPager} and it's associated
	 * {@link SlidingTabLayout}.
	 */
	static class SamplePagerItem {
		private final CharSequence mTitle;
		private final String mHtmlContent;
		private final int mIndicatorColor;
		private final int mDividerColor;

		SamplePagerItem(CharSequence title, String htmlContent, int indicatorColor, int dividerColor) {
			mTitle = title;
			mHtmlContent = htmlContent;
			mIndicatorColor = indicatorColor;
			mDividerColor = dividerColor;
		}

		/**
		 * @return A new {@link Fragment} to be displayed by a {@link ViewPager}
		 */
		Fragment createFragment() {
			return ContentFragment.newInstance(mTitle, mHtmlContent);
		}

		/**
		 * @return the title which represents this tab. In this sample this is used directly by
		 * {@link android.support.v4.view.PagerAdapter#getPageTitle(int)}
		 */
		CharSequence getTitle() {
			return mTitle;
		}

		/**
		 * @return the color to be used for indicator on the {@link SlidingTabLayout}
		 */
		int getIndicatorColor() {
			return mIndicatorColor;
		}

		/**
		 * @return the color to be used for right divider on the {@link SlidingTabLayout}
		 */
		int getDividerColor() {
			return mDividerColor;
		}

		String getHtmlContent() {
			return mHtmlContent;
		}
	}

	static final String LOG_TAG = "SlidingTabsColorsFragment";

	/**
	 * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
	 * above, but is designed to give continuous feedback to the user when scrolling.
	 */
	private SlidingTabLayout mSlidingTabLayout;

	/**
	 * A {@link ViewPager} which will be used in conjunction with the {@link SlidingTabLayout} above.
	 */
	private ViewPager mViewPager;

	/**
	 * List of {@link SamplePagerItem} which represent this sample's tabs.
	 */
	private List<SamplePagerItem> mTabs = new ArrayList<SamplePagerItem>();
	private ArrayList<Campaign> mCampaignArray;
	private Activity mActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivity = this.getActivity();
		MyApplication app = ((MyApplication) this.getActivity().getApplication());
		mCampaignArray = app.getTriggeredCampaignArray();

		// BEGIN_INCLUDE (populate_tabs)

		for (Campaign c : mCampaignArray){
			mTabs.add(new SamplePagerItem(
					c.getTitle(), // Title
					c.getBody(), //HTML Content
					Color.parseColor(getResources().getString(R.color.radius_blue)), // Indicator color
					Color.parseColor(getResources().getString(R.color.radius_light_grey)) //  Divider color
					));
		}

		// END_INCLUDE (populate_tabs)
	}

	/**
	 * Inflates the {@link View} which will be displayed by this {@link Fragment}, from the app's
	 * resources.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_sample, container, false);
	}



	// BEGIN_INCLUDE (fragment_onviewcreated)
	/**
	 * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
	 * Here we can pick out the {@link View}s we need to configure from the content view.
	 *
	 * We set the {@link ViewPager}'s adapter to be an instance of
	 * {@link SampleFragmentPagerAdapter}. The {@link SlidingTabLayout} is then given the
	 * {@link ViewPager} so that it can populate itself.
	 *
	 * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.i("SlidingTabsColorsFragment","ViewPager.onViewCreated: "+view);


		// BEGIN_INCLUDE (setup_viewpager)
		// Get the ViewPager and set it's PagerAdapter so that it can display items
		mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
		mViewPager.setAdapter(new SampleFragmentPagerAdapter(getChildFragmentManager()));






		// END_INCLUDE (setup_viewpager)


		// BEGIN_INCLUDE (setup_slidingtablayout)
		// Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
		// it's PagerAdapter set.
		mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
		mSlidingTabLayout.setViewPager(mViewPager);

		// BEGIN_INCLUDE (tab_colorizer)
		// Set a TabColorizer to customize the indicator and divider colors. Here we just retrieve
		// the tab at the position, and return it's set color
		mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

			@Override
			public int getIndicatorColor(int position) {
				return mTabs.get(position).getIndicatorColor();
			}

			@Override
			public int getDividerColor(int position) {
				return mTabs.get(position).getDividerColor();
			}

		});
		// END_INCLUDE (tab_colorizer)
		// END_INCLUDE (setup_slidingtablayout)

		//NOT WORKING YET
		//Sending to the specific Campaign that was triggered
		try{
			if (getArguments() != null){
				String campaignId = getArguments().getString(DetailActivity.KEY_CAMPAIGN_ID,"");
				Log.d("SlidingTabsColorsFragment","setCurrentItem to campaignId: "+campaignId );
				mViewPager.setCurrentItem(Integer.parseInt(campaignId), true);
			}
		}
		catch(NumberFormatException nfe){ nfe.printStackTrace(); }
		catch(Exception e){ e.printStackTrace(); }

	}
	// END_INCLUDE (fragment_onviewcreated)

	/**
	 * The {@link FragmentPagerAdapter} used to display pages in this sample. The individual pages
	 * are instances of {@link ContentFragment} which just display three lines of text. Each page is
	 * created by the relevant {@link SamplePagerItem} for the requested position.
	 * <p>
	 * The important section of this class is the {@link #getPageTitle(int)} method which controls
	 * what is displayed in the {@link SlidingTabLayout}.
	 */
	class SampleFragmentPagerAdapter extends FragmentPagerAdapter {

		SampleFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		/**
		 * Return the {@link android.support.v4.app.Fragment} to be displayed at {@code position}.
		 * <p>
		 * Here we return the value returned from {@link SamplePagerItem#createFragment()}.
		 */
		@Override
		public Fragment getItem(int i) {
			Log.i("SlidingTabsColorsFragment","SampleFragmentPagerAdapter.getItem position = "+i);
			if (mActivity != null && mCampaignArray != null && mCampaignArray.get(i) != null)
				((MyApplication) mActivity.getApplication())
				.recordAnalytics(CampaignKitNotifier.CKAnalyticsType.viewed, mCampaignArray.get(i));
			else
				Log.i("SlidingTabsColorsFragment","SampleFragmentPagerAdapter.what's null? mActivity="
						+mActivity+". mCampaignArray="+mCampaignArray);
			return mTabs.get(i).createFragment();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public void setPrimaryItem (ViewGroup container, int position, Object object) {
			Log.i("SlidingTabsColorsFragment","SampleFragmentPagerAdapter.setPrimaryItem position = "+position);

			super.setPrimaryItem ( container,  position, object);
		}
		// BEGIN_INCLUDE (pageradapter_getpagetitle)
		/**
		 * Return the title of the item at {@code position}. This is important as what this method
		 * returns is what is displayed in the {@link SlidingTabLayout}.
		 * <p>
		 * Here we return the value returned from {@link SamplePagerItem#getTitle()}.
		 */
		@Override
		public CharSequence getPageTitle(int position) {
			return mTabs.get(position).getTitle();
		}
		// END_INCLUDE (pageradapter_getpagetitle)


	}

}