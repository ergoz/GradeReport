package com.shinymetal.gradereport.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.apache.http.auth.InvalidCredentialsException;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.shinymetal.gradereport.BuildConfig;
import com.shinymetal.gradereport.NotificationUtils;
import com.shinymetal.gradereport.R;
import com.shinymetal.gradereport.objects.GradeSemester;
import com.shinymetal.gradereport.objects.MarkRec;
import com.shinymetal.gradereport.objects.Pupil;
import com.shinymetal.gradereport.objects.Schedule;
import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.objects.Week;

public class DataLoader {
	
	private static volatile DataLoader instance;	

	protected volatile boolean mIsLastNetworkCallFailed = false;
	protected volatile boolean mIsLastNetworkCallRetriable = true;
	protected volatile String mLastNetworkFailureReason;
	
	protected Date mCurrWeekStart = Week.getWeekStart(new Date ());
	
	protected Context mContext;
	protected BasicParser mParser;
	
	public Date getCurrWeekStart() {
		return mCurrWeekStart;
	}

	public void setCurrWeekStart(Date currWeekStart) {
		this.mCurrWeekStart = currWeekStart;
	}

	public void setLogin(String login) { mParser.setLogin(login); }	
	public String getLogin() { return mParser.getLogin(); }
	public void setPassword(String password) { mParser.setPassword(password); }

	protected DataLoader(Context context) {

		mContext = context;
		mParser = new MRCOParser();
	}
	
	public static DataLoader getInstance(Context context) {

		DataLoader localInstance = instance;

		if (localInstance == null) {

			synchronized (DataLoader.class) {

				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new DataLoader(context);
				} else
					instance.mContext = context;
			}
		}

		return localInstance;
	}

	public Cursor getCursorLessonsByDate(Date day, String uName) {
		
		if (uName == null) {
				
			ArrayList<String> names = getPupilNames();
			if (names.size() > 0)
				uName = names.get(0);
			else
				Log.e(this.toString(), TS.get() + this.toString()
						+ " getCachedLessonsByDate() : PupilNames set is empty!");
		}
			
		if (uName == null)
			return null;
		
		if (BuildConfig.DEBUG)
			Log.d(this.toString(), TS.get()
				+ "getCursorLessonsByDate () : pupil = " + uName);

		try {
			return Pupil.getByFormName(mParser.getLogin(), uName).getScheduleByDate(day)
					.getCursorLessonsByDate(day);

		} catch (Exception e) { // either NullPointerException or IllegalArgumentException
			
			Log.e(this.toString(), TS.get() + this.toString()
					+ " getCachedLessonsByDate() : Exception: " + e.toString());
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Cursor getCursorGradesBySemester(String uName, int wantSem) {
		
		if (uName == null)
			return null;

		Pupil p = Pupil.getByFormName(mParser.getLogin(), uName);
		if (p == null)
			return null;

		Schedule s = p.getScheduleByDate(getCurrWeekStart());
		if (s == null)
			return null;

		GradeSemester sem = s.getSemesterByNumber(wantSem);
		if (sem == null)
			return null;

		return s.getCursorGradesByDate(sem.getStart());
	}
	
	public boolean getAllPupilsLessons (Date day) {
		
		if (BuildConfig.DEBUG)
			Log.d(this.toString(), TS.get() + "getAllPupilsLessons () : started");
		
		NetLogger.add("Update started");
		
		String page;
		
		mIsLastNetworkCallFailed = false;
		mIsLastNetworkCallRetriable = true;
		mLastNetworkFailureReason = "";
		
		for (int i = 0; i < 2; i++) {

			try {
				if (mParser.loginSequence())
					break;

			} catch (InvalidCredentialsException e) {

				mLastNetworkFailureReason = mContext
						.getString(R.string.error_cannot_login);
				NetLogger.add(mLastNetworkFailureReason);

				mIsLastNetworkCallRetriable = false;
				mIsLastNetworkCallFailed = true;
				
				return false; // else try one more time
				
			} catch (IllegalStateException e) {
				
				mLastNetworkFailureReason = mContext
						.getString(R.string.error_cannot_fetch);

				NetLogger.add(mLastNetworkFailureReason);
				mIsLastNetworkCallFailed = true;

			} catch (Exception e) {

				mIsLastNetworkCallFailed = true;
				if ((mLastNetworkFailureReason = e.getMessage()) == null)
					mLastNetworkFailureReason = e.toString();
				
				NetLogger.add(mLastNetworkFailureReason);
			}
		}
		
		try {
			
			NetLogger.add("Get current lessons");
			mParser.getLessons();
			
			NetLogger.add("Get current diary");
			mParser.getLessonsDetails();			
			
			NetLogger.add("Get current marks");
			mParser.getGrades();

//			ArrayList<MarkRec> newMarks = GshisParser.getNewMarks();
//			if (newMarks.size() > 0)	// this will create notifications each time
//				NotificationUtils.getInstance(mContext).createGradeNotification(newMarks);

			Date curWeek = Week.getWeekStart(new Date ());
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(curWeek);
			cal.add(Calendar.DATE, 7+1);
			
			Date pastNextWeek = cal.getTime();

			Set<Pupil> set = Pupil.getSet(mParser.getLogin());
			if ( set != null && set.size() > 0) {
				for (Pupil p : set) {
					
					Schedule s = p.getScheduleByDate(new Date());
					
					for ( Week w : s.getWeekSet()) {
						
						if (w.getLoaded() && w.getStart().before(curWeek) && !w.getStart().equals(Week.getWeekStart(day))) {

							// too old & loaded, skipping
							if (BuildConfig.DEBUG)
								Log.d(this.toString(), TS.get()
									+ "getAllPupilsLessons () [2]: skipping week "
									+ w + " for " + p.getFormText());
							continue;
						}
						
						if (w.getStart().after(pastNextWeek)) {

							// too far in future, skipping
							if (BuildConfig.DEBUG)
								Log.d(this.toString(), TS.get()
									+ "getAllPupilsLessons () [3]: skipping week "
									+ w + " for " + p.getFormText());
							continue;
						}
						
						NetLogger.add("Get lessons for week " + w.getFormText());
						mParser.getLessons(p, s, w);

						NetLogger.add("Get diary for week " + w.getFormText());
						mParser.getLessonsDetails(p, s, w);
					}
					
					for (GradeSemester sem : s.getSemesterSet()) {
						
						// not current and already loaded, skip
						if (BuildConfig.DEBUG)
							Log.d(this.toString(), TS.get()
								+ "getAllPupilsLessons (): day = " + day
								+ " Sem start = " + sem.getStart() + " stop = " + sem.getStop() + " loaded = " + sem.getLoaded());
					
//						if (sem.getLoaded() && !(sem.getStart().getTime() <= day.getTime() && sem.getStop().getTime() >= day.getTime() )) {
						if (sem.getStart().getTime() > day.getTime()) {

							// not current and already loaded, skip
							if (BuildConfig.DEBUG)
								Log.d(this.toString(), TS.get()
									+ "getAllPupilsLessons (): skipping semester "
									+ sem.getStart() + " for " + p.getFormText());
							continue;							
						}
						
						NetLogger.add("Get marks for semester " + sem.getFormText());
						mParser.getGrades(p, s, sem);
					}
				}
			}

		} catch (Exception e) {
			
			NetLogger.add(e.toString());

			mIsLastNetworkCallFailed = true;
			mLastNetworkFailureReason = e.toString() + " " + e.getMessage();

			// TODO: add method
//			mIsLoggedIn = false;
			e.printStackTrace();
		}

		if (BuildConfig.DEBUG)
			Log.d(this.toString(), TS.get()
					+ "getAllPupilsLessons () : finished");

		NetLogger.add("Finished");
		return true;
	}
	
	public boolean isLastNetworkCallFailed() {
		return mIsLastNetworkCallFailed;
	}

	public boolean isLastNetworkCallRetriable() {
		return mIsLastNetworkCallRetriable;
	}

	public String getLastNetworkFailureReason() {
		return mLastNetworkFailureReason;
	}

	public String getPupilIdByName(String name) {
		
		for (Pupil p : Pupil.getSet(mParser.getLogin())) {
			
			if (p.getFormText().equals(name)) {
				return p.getFormId();
			}			
		}
		return null;
	}
	
	public ArrayList<String> getPupilNames () {
		
		if (BuildConfig.DEBUG)
			Log.d(this.toString(), TS.get() + "getPupilNames () : started");
		
		ArrayList<String> res = new ArrayList<String> (); 

		for (Pupil p : Pupil.getSet(mParser.getLogin())) {
			res.add(p.getFormText());
		}
		
		if (BuildConfig.DEBUG)
			Log.d(this.toString(), TS.get() + "getPupilNames () : finished");
			
		return res;
	}
}