package com.shinymetal.gradereport;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

import android.os.AsyncTask;

import com.shinymetal.objects.Lesson;
import com.shinymetal.utils.GshisLoader;

public class UpdateLessonsViewTask extends
		AsyncTask<Date, Void, ArrayList<Lesson>> {

	protected WeakReference<DiaryActivity> mActivity;

	protected Date day;

	public void setUpdateTarget(DiaryActivity activity) {

		mActivity = new WeakReference<DiaryActivity>(activity);
	}

	@Override
	protected ArrayList<Lesson> doInBackground(Date... dow) {

		return GshisLoader.getInstance().getLessonsByDate(dow[0], true);
	}

	protected void onPreExecute() {

		DiaryActivity activity = mActivity.get();

		if (activity != null) {
			activity.setBusy();
		}
	}

	protected void onPostExecute(ArrayList<Lesson> values) {

		DiaryActivity activity = mActivity.get();

		if (activity != null && values != null ) {

			activity.setIdle();
			activity.refreshFragments();
		}
	}
}
