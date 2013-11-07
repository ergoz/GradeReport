package com.shinymetal.gradereport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.shinymetal.gradereport.R;
import com.shinymetal.objects.Lesson;
import com.shinymetal.objects.Week;
import com.shinymetal.utils.GshisLoader;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class DiaryActivity extends FragmentActivity {

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
	
	DatePickerFragment dateSetFragment;
	
	private static final GshisLoader gshisLoader = GshisLoader.getInstance();
	
	private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmGmVMb5v06NsnxMAt4iOaJGXuU9Tj3XR9/QMmE1lE4VMjUzMrYT96FX6obkOrukZrN3cgw+oduv4mgLQjmaavd5U8EdFXKjdGD753k01DN/YYaG96WNFUd1ES4sZlq0R/rRR8B+l+uRaEaVIAQdEvGMd1nH1s6lRkkzQHf34plpH0O4DxAJn+OWhyDxWVsyC8hY3uPTrpKpr6g0iTQJOS+77+LhdIHmrd0oNm3R7galW4qVC6V+6BTqUz0YgzdF383H+7dP7GE2RRld7AeFlYjo4JFU5LQJzmPhrz/w788hO/dGKe5U5CYkw2HV1iJlmdboz+lKzYDnYzJyXT3s9cwIDAQAB";
	private static final byte[] SALT = new byte[] { 34, 87, 35, -23, -34, -12,
			43, -87, 34, 18, -12, -65, 76, -98, -121, -32, -12, 76, -43, 43 };
	
	private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    
    // A handler on the UI thread.
//    private Handler mHandler;
    
    private void enableNaviMenu () { naviMenuDisable = false; }
    
    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow(int policyReason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            // Should allow user access.
            // displayResult(getString(R.string.allow));
        }

        public void dontAllow(int policyReason) {
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
    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_diary);
		
//		mHandler = new Handler();

	    // Try to use more data here. ANDROID_ID is a single point of attack.
	    String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
	    
        // Library calls this when it's done.
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        // Construct the LicenseChecker with a policy.
        mChecker = new LicenseChecker(
            this, new ServerManagedPolicy(this,
                new AESObfuscator(SALT, getPackageName(), deviceId)),
            BASE64_PUBLIC_KEY);

        doCheck();

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);	
	}
	
    private void doCheck() {
        setProgressBarIndeterminateVisibility(true);
        mChecker.checkAccess(mLicenseCheckerCallback);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mChecker.onDestroy();
	}
	
	public boolean openPreferences (MenuItem item) {
		//Starting a new Intent
        Intent nextScreen = new Intent(getApplicationContext(), PreferencesActivity.class);
        startActivity(nextScreen);
        return true;
	}
	
	public boolean selectPupil (MenuItem item) {
		
		AlertDialog alertDialog = new AlertDialog.Builder(DiaryActivity.this).create(); //Read Update
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
		cal.setTime(gshisLoader.getCurrWeekStart());
		cal.add(Calendar.DATE, -7);
		GshisLoader.getInstance().setCurrWeekStart(cal.getTime());

		for (Fragment f : getSupportFragmentManager().getFragments()) {
			
			if (f != null && f instanceof LessonSectionFragment)
				((LessonSectionFragment) f).refresh();
		}
		return true;
	}
	
	public boolean nextWeek (MenuItem item) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(gshisLoader.getCurrWeekStart());
		cal.add(Calendar.DATE, 7);
		GshisLoader.getInstance().setCurrWeekStart(cal.getTime());
		
		for (Fragment f : getSupportFragmentManager().getFragments()) {
			
			if (f != null && f instanceof LessonSectionFragment)
				((LessonSectionFragment) f).refresh();
		}

		return true;
	}
	
	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			Calendar c = Calendar.getInstance();
			c.setTime(gshisLoader.getCurrWeekStart());
			
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
			
			DiaryActivity activity = (DiaryActivity) getActivity(); 
			activity.mViewPager.setCurrentItem(item, true);
			
			for (Fragment f : activity.getSupportFragmentManager().getFragments()) {
				
				if (f != null && f instanceof LessonSectionFragment)
					((LessonSectionFragment) f).refresh();
			}
		}
	}
	
	public boolean selectDate (MenuItem item) {
        
		dateSetFragment = new DatePickerFragment();
		dateSetFragment.show(getSupportFragmentManager(), "dateDialog");
        
		return true;
	}

	public boolean refresh (MenuItem item) {
		
		gshisLoader.reset();
		
		naviMenuDisable = true; 
		invalidateOptionsMenu();
		
		for (Fragment f : getSupportFragmentManager().getFragments()) {
			
			if (f != null && f instanceof LessonSectionFragment)
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
		getMenu().findItem(R.id.action_select_date)
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

			Fragment fragment = new LessonSectionFragment();
			Bundle args = new Bundle();
			args.putInt(LessonSectionFragment.ARG_SECTION_NUMBER, position + 1);
			
			fragment.setArguments(args);
			fragment.setRetainInstance(true);
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
		protected static final String ARG_SECTION_NUMBER = "section_number";
		
		protected ArrayList<Lesson> values;
		protected ExpandableListView expListView;
		protected TextView headerView;
		
		protected UpdateListView update;
		protected ProgressDialog dialog;
		
		public LessonSectionFragment() {
			
		}
		
		private class LessonsArrayAdapter extends BaseExpandableListAdapter {
			
		    private final Context context;
		    private final ArrayList<Lesson> values;
		    private final SimpleDateFormat format;
		    
		    public LessonsArrayAdapter(Context context, ArrayList<Lesson> values) {

		    	this.context = context;
		    	this.values = values;
		    	this.format = new SimpleDateFormat("HH:mm ", Locale.ENGLISH);
		    }
		    
		    @Override
		    public int getGroupCount() {
		    	
		        return values.size();
		    }
		    
		    @Override
		    public int getChildrenCount(int groupPosition) {
		    	
		        return 1;
		    }
		    
		    @Override
		    public Object getGroup(int groupPosition) {
		        return values.get(groupPosition);
		    }
		    
		    @Override
		    public Object getChild(int groupPosition, int childPosition) {
		    	
		        return values.get(groupPosition);
		    }
		    
		    @Override
		    public long getGroupId(int groupPosition) {
		        return groupPosition;
		    }
		    
		    @Override
		    public long getChildId(int groupPosition, int childPosition) {
		        return childPosition;
		    }

		    @Override
		    public boolean hasStableIds() {
		        return true;
		    }
		    
		    @Override
		    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
		                             ViewGroup parent) {

		        if (convertView == null) {
		            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		            convertView = inflater.inflate(R.layout.lessons_list, null);
		        }

		        if (isExpanded) {
		           // Изменяем что-нибудь, если текущая Group раскрыта
		        }
		        else {
		            // Изменяем что-нибудь, если текущая Group скрыта
		        }

		        TextView itemNameView = (TextView) convertView.findViewById(R.id.itemName);
		        TextView itemDetailView = (TextView) convertView.findViewById(R.id.itemDetail);
		        
		        Lesson l = values.get(groupPosition);
		        
		        itemNameView.setText("" + l.getNumber() + ". " + l.getFormText());
		        itemDetailView.setText(format.format(l.getStart()) + l.getTeacher());

		        return convertView;
		    }
		    
		    @Override
		    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
		                             View convertView, ViewGroup parent) {
		    	
		        if (convertView == null) {
		            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		            convertView = inflater.inflate(R.layout.lessons_detail, null);
		        }

		        TextView textTheme = (TextView) convertView.findViewById(R.id.itemTheme);
		        String theme = values.get(groupPosition).getTheme();
		        if (theme == null) theme = "";
		        textTheme.setText(getString(R.string.label_theme) + ": " + theme);

		        TextView textHomework = (TextView) convertView.findViewById(R.id.itemHomework);
		        String homework = values.get(groupPosition).getHomework();
		        if (homework == null) homework = "";
		        textHomework.setText(getString(R.string.label_homework) + ": " + homework);

		        TextView textMarks = (TextView) convertView.findViewById(R.id.itemMarks);
		        String marks = values.get(groupPosition).getMarks();
		        if (marks == null) marks = "";
		        textMarks.setText(getString(R.string.label_marks) + ": " + marks);

		        TextView textComment = (TextView) convertView.findViewById(R.id.itemComment);
		        String comment = values.get(groupPosition).getComment();
		        if (comment == null) comment = "";
		        textComment.setText(getString(R.string.label_comment) + ": " + comment);

		        return convertView;
		    }
		    
		    @Override
		    public boolean isChildSelectable(int groupPosition, int childPosition) {
		        return true;
		    }
		}
		
		private class UpdateListView extends AsyncTask<Integer, Void, ArrayList<Lesson>> {
			
			protected DiaryActivity activity;			
			protected boolean isCancelled = false;
			
			public void cancel() { isCancelled = true; }
			
			protected final SimpleDateFormat format = new SimpleDateFormat(
					"dd.MM.yyyy", Locale.ENGLISH);
			protected Date day;
						
			public void setUpdateTarget (DiaryActivity activity) {
				
				this.activity = activity;
			}			

			@Override
			protected ArrayList<Lesson> doInBackground(Integer... dow) {
			
				ArrayList<Lesson> values = new ArrayList<Lesson> ();
				int wantDoW = dow [0];
				day = gshisLoader.getCurrWeekStart();
				
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
				
			    dialog = new ProgressDialog(activity);
			    
			    dialog.setMessage(getString(R.string.label_loading_data));
			    dialog.setIndeterminate(true);
			    dialog.setCancelable(true);
			    dialog.show();
			}

			protected void onPostExecute(ArrayList<Lesson> values) {
				
				if (!isCancelled) {

					dialog.dismiss();
					
					if (values != null) {
						LessonsArrayAdapter adapter = new LessonsArrayAdapter (activity, values);
	
						headerView.setText(format.format(day));
						
						expListView.setAdapter(adapter);						
						expListView.invalidateViews();
	
						activity.enableNaviMenu();
						activity.invalidateOptionsMenu();
					}					
				}
			}
		}
		
		public void refresh () {

			update = new UpdateListView ();

			update.setUpdateTarget((DiaryActivity) getActivity());
			update.execute(getArguments().getInt(ARG_SECTION_NUMBER));			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			View rootView = inflater.inflate(R.layout.fragment_lessons,
					container, false);
			expListView = (ExpandableListView) rootView
					.findViewById(R.id.section_label);
			
			View header = getLayoutInflater(savedInstanceState).inflate(R.layout.lessons_header, null);
			expListView.addHeaderView(header);
			
			headerView = (TextView) header.findViewById(R.id.itemHeader);
			
			refresh ();
			return rootView;
		}
		
		@Override
		public void onDetach() {
			
			if (update != null)
				update.cancel();
			
			super.onDetach();
		}
	}
}
