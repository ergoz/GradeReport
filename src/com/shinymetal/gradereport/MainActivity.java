package com.shinymetal.gradereport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.shinymetal.gradereport.R;
import com.shinymetal.objects.Week;
import com.shinymetal.utils.GshisLoader;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.view.Window;
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
	private SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	private Menu menu = null;
	private boolean naviMenuDisable = true;
	private Date weekStart = Week.getWeekStart(new Date ());
	
	private void enableNaviMenu () { naviMenuDisable = false; }
	private Date getWeekStart () { return weekStart; }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

//		This won't work :(
//		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.string.diary_name); 

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
	
	public boolean selectPupil (MenuItem item) {
		
		AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create(); //Read Update
        alertDialog.setTitle(getString(R.string.action_select_pupil));
        alertDialog.setMessage(getString(R.string.action_pupil_detail));

		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {

					public void onClick(final DialogInterface dialog,
							final int which) {
						// here you can add functions
					}
				});

        alertDialog.show();  //<-- See This!
        
		return true;
	}
	
	public boolean previousWeek (MenuItem item) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(weekStart);
		cal.add(Calendar.DATE, -7);
		weekStart = cal.getTime();

//		LessonSectionFragment page = (LessonSectionFragment) getSupportFragmentManager().findFragmentByTag(
//				"android:switcher:" + R.id.pager + ":"
//						+ mViewPager.getCurrentItem());
//		page.refresh();

		for (Fragment f : getSupportFragmentManager().getFragments()) {
			((LessonSectionFragment) f).refresh();
		}
		return true;
	}
	
	public boolean nextWeek (MenuItem item) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(weekStart);
		cal.add(Calendar.DATE, 7);
		weekStart = cal.getTime();

//		LessonSectionFragment page = (LessonSectionFragment) getSupportFragmentManager().findFragmentByTag(
//				"android:switcher:" + R.id.pager + ":"
//						+ mViewPager.getCurrentItem());
//		page.refresh();
		
		for (Fragment f : getSupportFragmentManager().getFragments()) {
			((LessonSectionFragment) f).refresh();
		}

		return true;
	}
	
	public boolean selectWeek (MenuItem item) {
		
		AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create(); //Read Update
        alertDialog.setTitle(getString(R.string.action_select_week));
        alertDialog.setMessage(getString(R.string.action_week_detail));

		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {

					public void onClick(final DialogInterface dialog,
							final int which) {
						// here you can add functions
					}
				});

        alertDialog.show();  //<-- See This!

		return true;
	}

	public boolean refresh (MenuItem item) {
		
		GshisLoader.getInstance().reset();
		
		naviMenuDisable = true; 
		invalidateOptionsMenu();

//		LessonSectionFragment page = (LessonSectionFragment) getSupportFragmentManager().findFragmentByTag(
//				"android:switcher:" + R.id.pager + ":"
//						+ mViewPager.getCurrentItem());
//		page.refresh();
		
		for (Fragment f : getSupportFragmentManager().getFragments()) {
			((LessonSectionFragment) f).refresh();
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		getMenu().findItem(R.id.action_select_pupil).setEnabled(
				!naviMenuDisable);
		getMenu().findItem(R.id.action_select_week)
				.setEnabled(!naviMenuDisable);
		getMenu().findItem(R.id.action_previous_week).setEnabled(
				!naviMenuDisable);
		getMenu().findItem(R.id.action_next_week).setEnabled(!naviMenuDisable);

		return true;
	}
	
    private Menu getMenu()
    {
        //use it like this
        return menu;
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
			Fragment fragment = new LessonSectionFragment();
			Bundle args = new Bundle();
			args.putInt(LessonSectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 6 total pages.
			return 6;
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
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class LessonSectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		public ArrayList<String> values;
		ListView listView;
		
		public LessonSectionFragment() {
			
		}
		
		private class UpdateListView extends AsyncTask<Integer, Void, ArrayList<String>> {
			
			protected MainActivity activity;
			protected ListView view;
						
			public void setUpdateTarget (MainActivity activity, ListView view) {
				
				this.activity = activity;
				this.view = view;				
			}			

			@Override
			protected ArrayList<String> doInBackground(Integer... dow) {
			
				ArrayList<String> values = new ArrayList<String> ();
				int wantDoW = dow [0];
				Date day = activity.getWeekStart ();
				
				switch (wantDoW) {
				case 1:
					wantDoW = Calendar.MONDAY;
					break;
				case 2:
					wantDoW = Calendar.TUESDAY;
					break;
				case 3:
					wantDoW = Calendar.WEDNESDAY;
					break;
				case 4:
					wantDoW = Calendar.THURSDAY;
					break;
				case 5:
					wantDoW = Calendar.FRIDAY;
					break;
				case 6:
					wantDoW = Calendar.SATURDAY;
					break;
				default:
					wantDoW = Calendar.SUNDAY;
					break;
				}

				Calendar cal = Calendar.getInstance();				
		        cal.setTime(day);
		        
		        if (wantDoW < cal.get(Calendar.DAY_OF_WEEK)) {
		        	while (cal.get(Calendar.DAY_OF_WEEK) != wantDoW) {
		        		cal.add(Calendar.DATE, -1);
		        	}
		        } else if (wantDoW > cal.get(Calendar.DAY_OF_WEEK))
		        	while (cal.get(Calendar.DAY_OF_WEEK) != wantDoW) {
		        		cal.add(Calendar.DATE, 1);
		        	}
		        
		        day = cal.getTime();
				values = GshisLoader.getInstance().getLessonsByDate(day);
				return values;
			}

			protected void onPreExecute() {
				
				activity.setProgressBarIndeterminateVisibility(true);
			}
			protected void onPostExecute(ArrayList<String> values) {

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						activity, R.layout.fragment_item_textview,
						R.id.itemName, values);

				Log.w("zzzdebug", "onPostExecute: " + values);

				view.setAdapter(adapter);
				view.invalidateViews();
			    
				activity.enableNaviMenu ();
				activity.invalidateOptionsMenu();

				activity.setProgressBarIndeterminateVisibility(false);
			}
		}
		
		public void refresh () {

			UpdateListView update = new UpdateListView ();
			update.setUpdateTarget((MainActivity) getActivity(), listView);
			update.execute(getArguments().getInt(ARG_SECTION_NUMBER));
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			listView = (ListView) rootView
					.findViewById(R.id.section_label);
			
			refresh ();
			return rootView;
		}
	}
}
