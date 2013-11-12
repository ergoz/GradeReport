package com.shinymetal.gradereport;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.utils.GshisLoader;

public class LessonsExpListFragment extends Fragment implements
		UpdateableFragment {
 
	protected static final String ARG_SECTION_NUMBER = "section_number";
	protected LessonsExpListAdapter mAdapter;

	public LessonsExpListFragment() {

	}

	public UpdateableAdapter getAdapter() {

		return mAdapter;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_exp_lessons, container,
				false);
		ExpandableListView expListView = (ExpandableListView) rootView
				.findViewById(R.id.section_label);

		View header = getLayoutInflater(savedInstanceState).inflate(
				R.layout.lessons_header, null);
		expListView.addHeaderView(header);

		Date day = GshisLoader.getInstance().getCurrWeekStart();
		int wantDoW = getArguments().getInt(ARG_SECTION_NUMBER);

		Log.i(this.toString(), TS.get() + "refresh (), ARG_SECTION_NUMBER="
				+ wantDoW);

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
		mAdapter = new LessonsExpListAdapter((DiaryActivity) getActivity(), day);
		expListView.setAdapter(mAdapter);

		((TextView) header.findViewById(R.id.itemHeader))
				.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
						.format(day));

		return rootView;
	}
}
