package com.shinymetal.gradereport;

import java.lang.ref.WeakReference;
import java.util.Date;

import android.os.AsyncTask;
import android.util.Log;

import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.utils.GshisLoader;

public class UpdateLessonsTask extends
		AsyncTask<Date, Void, Void> {

	protected WeakReference<DiaryActivity> mActivity;
	protected boolean mSuccess = false;
	protected Date mDay;

	public void setUpdateTarget(DiaryActivity activity) {

		mActivity = new WeakReference<DiaryActivity>(activity);
	}

	@Override
	protected Void doInBackground(Date... dow) {
		
		Log.i (this.toString(), TS.get() + "doInBackground ()");
		GshisLoader.getInstance().getNonCachedLessonsByDate(dow[0], null);
		
		return null;
	}

	protected void onPreExecute() {

		DiaryActivity activity = mActivity.get();

		if (activity != null) {
			activity.setBusy();
		}
	}

	protected void onPostExecute() {
		
		Log.i (this.toString(), TS.get() + this.toString() + " onPostExecute() started");
		
		if (isCancelled()) {
			
			Log.i (this.toString(), TS.get() + this.toString() + " cancelled!");
			return;
		}

		DiaryActivity activity = mActivity.get();

		if (activity != null ) {
			
			activity.getHandler().postDelayed(new Runnable() {
				public void run() {

					mActivity.get().setIdle();
					mActivity.get().onUpdateLessonsTaskComplete();
				}
			}, 100);
		}
		
		Log.i (this.toString(), TS.get() + this.toString() + " onPostExecute() finished");
	}
}
