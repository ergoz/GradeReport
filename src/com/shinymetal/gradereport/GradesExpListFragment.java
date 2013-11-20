package com.shinymetal.gradereport;

import com.shinymetal.gradereport.objects.GradeSemester;
import com.shinymetal.gradereport.objects.Pupil;
import com.shinymetal.gradereport.objects.Schedule;
import com.shinymetal.gradereport.utils.GshisLoader;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class GradesExpListFragment extends Fragment implements
		UpdateableFragment {
	
	protected static final String ARG_SECTION_NUMBER = "section_number";
	protected GradesExpListAdapter mAdapter;
	protected ExpandableListView mExpListView;
	
	public UpdateableAdapter getAdapter() {

		return mAdapter;
	}

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_exp_list, container, false);
		mExpListView = (ExpandableListView) rootView.findViewById(R.id.section_label);
		
		int width = (int) (getResources().getDisplayMetrics().widthPixels - TypedValue
				.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources()
						.getDisplayMetrics()));
		
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		float scale = getResources().getDisplayMetrics().density;
		
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
			mExpListView.setIndicatorBounds(
					width - (int) (50 * scale),
					width - (int) (10 * scale));
		} else {
			mExpListView.setIndicatorBoundsRelative(
					width - (int) (50 * scale),
					width - (int) (10 * scale));
		}

		View header = getLayoutInflater(savedInstanceState).inflate(
				R.layout.lessons_header, null);
		mExpListView.addHeaderView(header);

		int wantSem = getArguments().getInt(ARG_SECTION_NUMBER);
		mExpListView.setOnGroupExpandListener(new OnGroupExpandListener() {

	        @Override
	        public void onGroupExpand(int groupPosition) {

	            if (groupPosition != mAdapter.getSelectedPosition()) {
	            	mExpListView.collapseGroup(mAdapter.getSelectedPosition());

	            }
	            mAdapter.setSelectedPosition(groupPosition);
	        }
	    });
		
		Pupil p = Pupil.getByFormName(((GradesActivity) getActivity()).getPupilName());
		GradeSemester sem = null;
		if (p != null) {
			
			Schedule s = p.getScheduleByDate(GshisLoader.getInstance().getCurrWeekStart());
			if (s != null ) sem = s.getSemesterByNumber(wantSem);
		}
		
		mAdapter = new GradesExpListAdapter((GradesActivity) getActivity(), sem);
		mExpListView.setAdapter(mAdapter);
		
		((TextView) header.findViewById(R.id.itemHeader))
				.setText("TODO: DATE HERE");
		
		return rootView;
	}
}
