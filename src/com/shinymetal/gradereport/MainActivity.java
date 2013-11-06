package com.shinymetal.gradereport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.shinymetal.gradereport.R;
import com.shinymetal.objects.Lesson;
import com.shinymetal.utils.GshisLoader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

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
	
	private void enableNaviMenu () { naviMenuDisable = false; }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
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
		cal.setTime(GshisLoader.getInstance().getCurrWeekStart());
		cal.add(Calendar.DATE, -7);
		GshisLoader.getInstance().setCurrWeekStart(cal.getTime());

		for (Fragment f : getSupportFragmentManager().getFragments()) {
			((LessonSectionFragment) f).refresh();
		}
		return true;
	}
	
	public boolean nextWeek (MenuItem item) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(GshisLoader.getInstance().getCurrWeekStart());
		cal.add(Calendar.DATE, 7);
		GshisLoader.getInstance().setCurrWeekStart(cal.getTime());
		
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
		protected static final String ARG_SECTION_NUMBER = "section_number";
		protected ArrayList<Lesson> values;
		protected ExpandableListView expListView;
		protected TextView textView;
		
		public LessonSectionFragment() {
			
		}
		
		private class LessonsArrayAdapter extends BaseExpandableListAdapter {
			
//			private class LessonDetail {
//				
//				protected String theme;
//				protected String homework;
//				protected String marks;
//				protected String comment;
//
//				public String getTheme() {
//					return theme;
//				}
//				public String getHomework() {
//					return homework;
//				}
//				public String getMarks() {
//					return marks;
//				}
//				public String getComment() {
//					return comment;
//				}
//				public void setTheme(String theme) {
//					this.theme = theme;
//				}
//				public void setHomework(String homework) {
//					this.homework = homework;
//				}
//				public void setMarks(String marks) {
//					this.marks = marks;
//				}
//				public void setComment(String comment) {
//					this.comment = comment;
//				}
//			}
			
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
		    	
		    	Lesson l = values.get(groupPosition);
//		    	LessonDetail d = new LessonDetail ();
//		    	
//		    	d.setComment(l.getComment());
//		    	d.setHomework(l.getHomework());
//		    	d.setMarks(l.getMarks());
//		    	d.setTheme(l.getTheme());
		    	
		        return l;
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
		           //Изменяем что-нибудь, если текущая Group раскрыта
		        }
		        else {
		            //Изменяем что-нибудь, если текущая Group скрыта
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
			
			protected MainActivity activity;
			protected ExpandableListView view;
			protected TextView header;
			
			protected final SimpleDateFormat format = new SimpleDateFormat(
					"dd.MM.yyyy", Locale.ENGLISH);
			protected Date day;
						
			public void setUpdateTarget (MainActivity activity, ExpandableListView view, TextView header) {
				
				this.activity = activity;
				this.view = view;
				this.header = header;
			}			

			@Override
			protected ArrayList<Lesson> doInBackground(Integer... dow) {
			
				ArrayList<Lesson> values = new ArrayList<Lesson> ();
				int wantDoW = dow [0];
				day = GshisLoader.getInstance().getCurrWeekStart();
				
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

			protected void onPostExecute(ArrayList<Lesson> values) {

				LessonsArrayAdapter adapter = new LessonsArrayAdapter (activity, values);

				if (view != null) {

					view.setAdapter(adapter);
					header.setText(format.format(day));
					view.invalidateViews();
				}
			    
				if (activity != null) {
					
					activity.enableNaviMenu ();
					activity.invalidateOptionsMenu();

					activity.setProgressBarIndeterminateVisibility(false);
				}
			}
		}
		
		public void refresh () {

			UpdateListView update = new UpdateListView ();

			update.setUpdateTarget((MainActivity) getActivity(), expListView, textView);
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
			
			textView = (TextView) header.findViewById(R.id.itemHeader);
			
			refresh ();
			return rootView;
		}
	}
}
