package com.shinymetal.gradereport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;

import com.shinymetal.gradereport.R;
import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.objects.Week;
import com.shinymetal.gradereport.utils.GshisLoader;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;

public class DiaryActivity extends AbstractActivity implements LicenseCheckerCallback {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private LessonsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private DatePickerFragment mDateSetFragment;
			
	private Spinner mPupilSpinner;
	
	private static volatile DiaryActivity instance;	
	private static int mLicState = Policy.RETRY;
	public LicenseValidatorHelper mLicValidator = null;
	
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
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	
		 super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onPause() {

    	if (mLicValidator != null) {
    		mLicValidator.onDestroy();
    		mLicValidator = null;
    	}

		super.onPause();
	}
	
	@Override
	public void onResume() {
		
		if (mLicState == Policy.RETRY)
			mLicValidator = new LicenseValidatorHelper (this, this);

		super.onResume();
	}
	
    public void allow(int policyReason) {
    	
    	mLicState = Policy.LICENSED;  	
    }
    
    public void dontAllow(int policyReason) {
    	
    	mLicState = policyReason;
    	final boolean bRetry = policyReason == Policy.RETRY;
    	
    	if (isFinishing()) {
    		// Don't update UI if Activity is finishing.
    		return;
    	}
    	
        // Should not allow access. In most cases, the app should assume
        // the user has access unless it encounters this. If it does,
        // the app should inform the user of their unlicensed ways
        // and then either shut down the app or limit the user to a
        // restricted set of features.
        // In this example, we show a dialog that takes the user to Market.
        // If the reason for the lack of license is that the service is
        // unavailable or there is another problem, we display a
        // retry button on the dialog and a different message.
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.unlicensed_dialog_title);
    	builder.setMessage(bRetry ? R.string.unlicensed_dialog_retry_body : R.string.unlicensed_dialog_body);
        builder.setPositiveButton(bRetry ? R.string.label_retry : R.string.label_buy, new DialogInterface.OnClickListener() {
            boolean mRetry = bRetry;
            public void onClick(DialogInterface dialog, int which) {
                if ( mRetry ) {

                	if (instance.mLicValidator != null) {
                		instance.mLicValidator.retry();
                	} else {
                		instance.mLicValidator = new LicenseValidatorHelper (instance, instance);
                	}
                } else {
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                            "http://market.android.com/details?id=" + getPackageName()));
                        startActivity(marketIntent);                        
                }
            }
        });
        
        builder.setNegativeButton(R.string.label_quit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).create();
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
        
		if (BuildConfig.DEBUG)
			Log.d(this.toString(), TS.get() + "applicationError (): "
					+ errorCode);    	
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
			setRecurringAlarm(this, true);
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
			
			Fragment fragment;
			
	        if (getResources().getConfiguration().orientation
	                == Configuration.ORIENTATION_LANDSCAPE) {

	        	fragment = new LessonsNestedFragment();
				if (BuildConfig.DEBUG)
					Log.d(this.toString(), TS.get() + this.toString()
							+ " getItem () LANDSCAPE");
	        	
	        } else {

	        	fragment = new LessonsExpListFragment();
	        	if (BuildConfig.DEBUG)
					Log.d(this.toString(), TS.get() + this.toString()
							+ " getItem () PORTRAIT");
	        }
	        
			Bundle args = new Bundle();
			args.putInt(LessonsExpListFragment.ARG_SECTION_NUMBER, position + 1);
			
			fragment.setArguments(args);
			return fragment;
		}
		
		@Override
	    public int getItemPosition(Object object)
	    {
			if (BuildConfig.DEBUG)
				Log.d(this.toString(), TS.get() + this.toString()
						+ " getItemPosition () started");
			
	        return POSITION_UNCHANGED;
	    }

		@Override
		public int getCount() {
			// Show 6 mTotal pages.
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
}
