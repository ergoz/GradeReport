package com.shinymetal.gradereport;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LessonsNestedFragment extends Fragment implements
		UpdateableFragment {
	
	private LessonsListFragment mLessonsFragment;
	private LessonDetailsFragment mLessonsDetailsFragment;

	@Override
	public UpdateableAdapter getAdapter() {

		return mLessonsFragment.getAdapter();
	}
	
	public LessonDetailsFragment getDetailsFragment () {
		
		return mLessonsDetailsFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_nested, container,
				false);
		
		mLessonsFragment = new LessonsListFragment();
		mLessonsFragment.setArguments(getArguments());
		mLessonsDetailsFragment = new LessonDetailsFragment();
		
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

		transaction.add(R.id.fragment_list, mLessonsFragment, "left");
		transaction.add(R.id.fragment_detail, mLessonsDetailsFragment, "right");

		transaction.commit();
				
		return rootView;
	}
}
