package com.shinymetal.gradereport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.shinymetal.gradereport.R;
import com.shinymetal.objects.TS;
import com.shinymetal.objects.Week;
import com.shinymetal.utils.GshisLoader;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
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
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;

public class DiaryActivity extends FragmentActivity implements LicenseCheckerCallback {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private LessonsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	private boolean mNaviMenuDisable = true;
	
	private DatePickerFragment mDateSetFragment;
	private ProgressDialog mProgressDialog;
	private UpdateLessonsTask mUpdate;
	
	private Spinner mPupilSpinner;
	
	private static volatile DiaryActivity instance;	
	private static final GshisLoader mGshisLoader = GshisLoader.getInstance();	
	
	private static int mLicState = Policy.RETRY;
	public LicenseValidator mLicValidator = null;
	
	private Handler mHandler;
	
	public Handler getHandler() {
		return mHandler;
	}

	public void setBusy() {
		
		if (!isFinishing()) {
			
			mNaviMenuDisable = true;
			invalidateOptionsMenu();
			
			mProgressDialog = new ProgressDialog(this);

			mProgressDialog.setMessage(getString(R.string.label_loading_data));
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.show();
		}		
	}
    
    public void setIdle () {
    
    	if (!isFinishing()) {
    		
    		mNaviMenuDisable = false;
    		invalidateOptionsMenu();
    		
    		if (mProgressDialog != null) {
    			
    			mProgressDialog.dismiss();
    			mProgressDialog = null;    			
    		}
    	}
    }
    
    public void startUpdateTask () {
    	
    	if (mUpdate == null || mUpdate.getStatus() == AsyncTask.Status.FINISHED) {
    		
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    		mGshisLoader.setLogin(prefs.getString("login", ""));
    		mGshisLoader.setPassword(prefs.getString("password", ""));
    	
			mUpdate = new UpdateLessonsTask ();
	
			mUpdate.setUpdateTarget(this);
			mUpdate.execute(mGshisLoader.getCurrWeekStart());
    	}
    }
    
    public void onUpdateLessonsTaskComplete () {
    	
    	Log.i (this.toString(), TS.get() + this.toString() + " onUpdateLessonsTaskComplete() started");
    	
    	if (!mGshisLoader.isLastNetworkCallFailed()) {

			for (Fragment f : getSupportFragmentManager().getFragments()) {

				if (f != null && f instanceof LessonSectionFragment) {

					LessonsArrayAdapter a = ((LessonSectionFragment) f).getAdapter();
					if (a != null)
						a.onUpdateTaskComplete();

					Log.i(this.toString(), TS.get() + this.toString()
							+ " onUpdateLessonsTaskComplete() : " + f.getId());
				}
			}
			
			return;
    	}
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_error)); 
        builder.setMessage(mGshisLoader.getLastNetworkFailureReason());
        builder.setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });
        
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_diary);
		instance = this;
		
		mSectionsPagerAdapter = new LessonsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		mHandler = new Handler();
		
		if (savedInstanceState != null) {
			mLicState = savedInstanceState.getInt("mLicState");
			mNaviMenuDisable = savedInstanceState.getBoolean("mNaviMenuDisable");
		}
		
		if (mLicState == Policy.RETRY)
			mLicValidator = new LicenseValidator (this, this);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	 
		super.onSaveInstanceState(savedInstanceState);
		
		savedInstanceState.putInt("mLicState", mLicState);
		savedInstanceState.putBoolean("mNaviMenuDisable", mNaviMenuDisable);
	}
	
	@Override
	public void onPause() {
		
		setIdle ();
		
    	if (mUpdate != null && mUpdate.getStatus() == AsyncTask.Status.RUNNING)
    		mUpdate.cancel(false);
		
		super.onPause();
	}
    
    public void allow(int policyReason) {
    	
    	mLicState = Policy.LICENSED;
    	
    	if (isFinishing()) {
    		// Don't update UI if Activity is finishing.
    		return;
    	}
    	
        // Should allow user access.
        // displayResult(getString(R.string.allow));
    }
    
    public void dontAllow(int policyReason) {
    	
    	mLicState = Policy.NOT_LICENSED;
    	
    	if (isFinishing()) {
    		// Don't update UI if Activity is finishing.
    		return;
    	}
    	
        // displayResult(getString(R.string.dont_allow));
        
        // Should not allow access. In most cases, the app should assume
        // the user has access unless it encounters this. If it does,
        // the app should inform the user of their unlicensed ways
        // and then either shut down the app or limit the user to a
        // restricted set of features.
        // In this example, we show a dialog that takes the user to Market.
        // If the reason for the lack of license is that the service is
        // unavailable or there is another problem, we display a
        // retry button on the dialog and a different message.
        
        // displayDialog(policyReason == Policy.RETRY);
    }
    
    public void applicationError(int errorCode) {
    	
    	mLicState = Policy.RETRY;
    	
    	if (isFinishing()) {
    		// Don't update UI if Activity is finishing.
    		return;
    	}
    	
        // This is a polite way of saying the developer made a mistake
        // while setting up or calling the license checker library.
        // Please examine the error code and fix the error.
        
        // String result = String.format(getString(R.string.application_error), errorCode);
        // displayResult(result);
    }
	
    @Override
	public boolean onOptionsItemSelected (MenuItem item) {
    	
    	Calendar cal = null;
		
		switch (item.getItemId()) {
		case R.id.action_settings:
			//Starting a new Intent
	        Intent nextScreen = new Intent(getApplicationContext(), PreferencesActivity.class);
	        startActivity(nextScreen);
	        return true;
	        
		case R.id.action_select_pupil:		
			AlertDialog alertDialog;

			LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.pupil_dialog, null);
			
			ArrayList<String> names = mGshisLoader.getPupilNames();
			ArrayAdapter<String> adp = new ArrayAdapter<String>(DiaryActivity.this,
					android.R.layout.simple_spinner_item, names);
			
			mPupilSpinner = (Spinner) layout.findViewById(R.id.pupilSpinner);
			mPupilSpinner.setAdapter(adp);

		    AlertDialog.Builder builder = new AlertDialog.Builder(DiaryActivity.this);
		    builder.setView(layout);

		    alertDialog = builder.create();
		    alertDialog.setTitle(getString(R.string.action_select_pupil));
		    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.label_submit),
					new DialogInterface.OnClickListener() {
	
						public void onClick(final DialogInterface dialog,
								final int which) {
					
							GshisLoader.getInstance().selectPupilByName(mPupilSpinner.getSelectedItem().toString());
						}
					});
		    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.label_cancel),
					new DialogInterface.OnClickListener() {
	
						public void onClick(final DialogInterface dialog,
								final int which) {

						}
					});

		    alertDialog.show();
			return true;

		case R.id.action_previous_week:
			cal = Calendar.getInstance();
			cal.setTime(mGshisLoader.getCurrWeekStart());
			cal.add(Calendar.DATE, -7);

			mGshisLoader.setCurrWeekStart(cal.getTime());
			recreate();
			return true;

		case R.id.action_next_week:
			cal = Calendar.getInstance();
			cal.setTime(mGshisLoader.getCurrWeekStart());
			cal.add(Calendar.DATE, 7);

			mGshisLoader.setCurrWeekStart(cal.getTime());
			recreate();
			return true;
			
		case R.id.action_select_date:
			mDateSetFragment = new DatePickerFragment();
			mDateSetFragment.show(getSupportFragmentManager(), "dateDialog");	        
			return true;
			
		case R.id.action_reload:
			mGshisLoader.reset();
			
			recreate();
			return true;

		}
		return true;
	}
	
	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			Calendar c = Calendar.getInstance();
			c.setTime(mGshisLoader.getCurrWeekStart());
			
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH) + ((DiaryActivity) getActivity()).mViewPager.getCurrentItem();

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}		

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month);
			c.set(Calendar.DAY_OF_MONTH, day);
			
			Date weekStart = Week.getWeekStart(c.getTime());
			c.setTime(weekStart);
			
			int item = 0;
			
			while (c.get(Calendar.DAY_OF_MONTH) != day) {
				
				c.add(Calendar.DATE, 1);
				item++;
			}

			GshisLoader.getInstance().setCurrWeekStart(weekStart);
			instance.mViewPager.setCurrentItem(item, true);
			
			// this picker should not load again
			instance.getHandler().postDelayed(new Runnable() {
				public void run() {

					instance.recreate();
				}
			}, 1);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the mMenu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.findItem(R.id.action_select_pupil).setEnabled(
				!mNaviMenuDisable);
		menu.findItem(R.id.action_select_date)
				.setEnabled(!mNaviMenuDisable);
		menu.findItem(R.id.action_previous_week).setEnabled(
				!mNaviMenuDisable);
		menu.findItem(R.id.action_next_week).setEnabled(!mNaviMenuDisable);

		return true;
	}
	
    	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class LessonsPagerAdapter extends FragmentPagerAdapter {

		public LessonsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			Fragment fragment = new LessonSectionFragment();
			Bundle args = new Bundle();
			args.putInt(LessonSectionFragment.ARG_SECTION_NUMBER, position + 1);
			
			fragment.setArguments(args);
			return fragment;
		}
		
		@Override
	    public int getItemPosition(Object object)
	    {
			Log.i (this.toString(), TS.get() + this.toString() + " getItemPosition () started");
			
			if (object instanceof LessonSectionFragment) {
	            return POSITION_NONE;
			}
			
	        return POSITION_UNCHANGED;
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
		protected static final String ARG_SECTION_NUMBER = "section_number";
		protected LessonsArrayAdapter mAdapter;
		
		public LessonSectionFragment() {
			
		}
		
		public LessonsArrayAdapter getAdapter () {
			
			return mAdapter;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			View rootView = inflater.inflate(R.layout.fragment_lessons,
					container, false);
			ExpandableListView expListView = (ExpandableListView) rootView
					.findViewById(R.id.section_label);
			
			View header = getLayoutInflater(savedInstanceState).inflate(R.layout.lessons_header, null);
			expListView.addHeaderView(header);
			
			Date day = GshisLoader.getInstance().getCurrWeekStart();
			int wantDoW = getArguments().getInt(ARG_SECTION_NUMBER);

			Log.i (this.toString(), TS.get() + "refresh (), ARG_SECTION_NUMBER=" + wantDoW);
			
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
			mAdapter = new LessonsArrayAdapter((DiaryActivity) getActivity(), day);
			expListView.setAdapter(mAdapter);

			((TextView) header.findViewById(R.id.itemHeader))
					.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
							.format(day));

			return rootView;
		}
	}
}
