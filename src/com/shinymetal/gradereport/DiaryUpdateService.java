package com.shinymetal.gradereport;

import java.util.Date;

import com.shinymetal.gradereport.objects.TS;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class DiaryUpdateService extends IntentService {

	public DiaryUpdateService() {
		
		super("DiaryUpdate");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {

		Log.i(this.toString(), TS.get() + this.toString()
				+ " About to update current week for current pupil.");
		
		UpdateCurPupilLessonsByDateTask task = new UpdateCurPupilLessonsByDateTask();
		task.execute(new Date());		
	}
}
