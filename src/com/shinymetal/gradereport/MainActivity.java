package com.shinymetal.gradereport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.shinymetal.gradereport.R;
import com.shinymetal.utils.GshisLoader;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);	
	}
	
	public boolean openPreferences (MenuItem item) {
		//Starting a new Intent
        Intent nextScreen = new Intent(getApplicationContext(), PreferencesActivity.class);
        startActivity(nextScreen);
        return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onPause () {
		
		GshisLoader.getInstance().reset();
		super.onPause();
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 7 total pages.
			return 7;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			case 3:
				return getString(R.string.title_section4).toUpperCase(l);
			case 4:
				return getString(R.string.title_section5).toUpperCase(l);
			case 5:
				return getString(R.string.title_section6).toUpperCase(l);
			case 6:
				return getString(R.string.title_section7).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public ArrayList<String> values;
		
		public DummySectionFragment() {
			
//			values = new ArrayList<String> ();
			
//			values.add("a");
//			values.add("b");
//			values.add("c");
			
		}
		
		private class UpdateListView extends AsyncTask<Integer, Void, ArrayList<String>> {
			
			protected Context context;
			protected ListView view;
			
			public void setUpdateTarget (Context context, ListView view) {
				
				this.context = context;
				this.view = view;				
			}
			

			@Override
			protected ArrayList<String> doInBackground(Integer... dow) {
			
				ArrayList<String> values = new ArrayList<String> ();
				
	//			int count = dow.length;
	//			if (count != 1) return values;
				
				Log.w("com.shinymetal.gradereport",
						"doInBackground() called with arg="
								+ dow[0]);
		        

				Date day = new Date ();
				int wantDoW = dow [0];
				Calendar cal = Calendar.getInstance();				
				
				
		        cal.setTime(day);
		        
		        // TODO: remove this
		        cal.add(Calendar.DATE, -7);
		        
		        if (wantDoW < cal.get(Calendar.DAY_OF_WEEK)) {
		        	while (cal.get(Calendar.DAY_OF_WEEK) != wantDoW) {
		        		cal.add(Calendar.DATE, -1);
		        	}
		        } else if (wantDoW > cal.get(Calendar.DAY_OF_WEEK))
		        	while (cal.get(Calendar.DAY_OF_WEEK) != wantDoW) {
		        		cal.add(Calendar.DATE, 1);
		        	}
		        
		        day = cal.getTime();
		        Log.w("com.shinymetal.gradereport",
						"cal.get(Calendar.DAY_OF_WEEK)="
								+ cal.get(Calendar.DAY_OF_WEEK) + " date: " + day);
		        
				values = GshisLoader.getInstance().killMeIAmTest(day);

				Log.w("com.shinymetal.gradereport",
						"values: " + values);		        

				return values;
			}
			
		     protected void onPostExecute(ArrayList<String> values) {
					
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
			                R.layout.fragment_item_textview, R.id.itemName, values);

					Log.w("com.shinymetal.gradereport",
							"onPostExecute: " + values);
					
			        view.setAdapter(adapter);
			        view.invalidateViews();
		     }			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			ListView listView = (ListView) rootView
					.findViewById(R.id.section_label);
//			dummyListView.setText(Integer.toString(getArguments().getInt(
//					ARG_SECTION_NUMBER)));
			
			UpdateListView update = new UpdateListView ();
			update.setUpdateTarget(getActivity(), listView);
			update.execute(getArguments().getInt(ARG_SECTION_NUMBER));
			
			Log.w("com.shinymetal.gradereport",
					"update.execute() called with arg="
							+ getArguments().getInt(ARG_SECTION_NUMBER));
	        
			return rootView;
		}
	}

}
