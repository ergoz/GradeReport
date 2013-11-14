package com.shinymetal.gradereport.objects;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.shinymetal.gradereport.db.Database;

public class Schedule extends FormTimeInterval {
	
	private final static String ID_NAME = "ID";
	private final static String FORMID_NAME = "FORMID";
	private final static String FORMTEXT_NAME = "FORMTEXT";
	private final static String PUPILID_NAME = "PUPILID";
	private final static String START_NAME = "START";
	private final static String STOP_NAME = "STOP";
	
	public final static String TABLE_NAME = "SCHEDULE";
	public final static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ ID_NAME + " INTEGER PRIMARY KEY ASC, "
			+ FORMID_NAME + " TEXT, "
			+ PUPILID_NAME + " INTEGER REFERENCES PUPIL (ID), "
			+ FORMTEXT_NAME + " TEXT, "
			+ START_NAME + " INTEGER, "
			+ STOP_NAME + " INTEGER, "
			+ " UNIQUE ( " + START_NAME + ", " + STOP_NAME + ", " + PUPILID_NAME + "));";
	
	private long mPupilId;
	private long mRowId;
	
	public Schedule(String formId, String schoolYear) {

		setFormId(formId);
		setFormText(schoolYear);
	}
	
	public long getRowId () { return mRowId; }
	
	public long insert(Pupil p) {		

        ContentValues values = new ContentValues();
        
        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());
        values.put(PUPILID_NAME, mPupilId = p.getRowId());
        
    	values.put(START_NAME, getStart().getTime());
    	values.put(STOP_NAME, getStop().getTime());	

        return mRowId = Database.getWritable().insert(TABLE_NAME, null, values);	
	}
	
	public long update() {
		
        ContentValues values = new ContentValues();
        
        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());        
    	values.put(START_NAME, getStart().getTime());
    	values.put(STOP_NAME, getStop().getTime());	
    	
    	String selection = FORMID_NAME + " = ? AND " + ID_NAME + " = ?";
        String[] args = new String[] { getFormId(), "" + mRowId };
		
    	return Database.getWritable().update(TABLE_NAME, values, selection, args);
	}
	
	public static Schedule getByFormId(Pupil p, String fId) {
		
		String selection = FORMID_NAME + " = ? AND " + PUPILID_NAME + " = ?";
        String[] args = new String[] { fId, "" + p.getRowId() };
        String[] columns = new String[] {FORMTEXT_NAME, START_NAME, STOP_NAME, ID_NAME};

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);
        c.moveToFirst();
        if (c.getCount() <= 0)
        	return null;
		
		Schedule s = new Schedule (fId, c.getString(c.getColumnIndex(FORMTEXT_NAME)));
		long start = c.getLong(c.getColumnIndex(START_NAME));
		s.setStart(new Date(start));		
		long stop = c.getLong(c.getColumnIndex(STOP_NAME));
		s.setStop(new Date(stop));
		s.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		
		return s;
	}
	
	public static final Set<Schedule> getSet(Pupil p) {
		
		Set<Schedule> set = new HashSet<Schedule> (); 
		
		String selection = PUPILID_NAME + " = ?";
        String[] args = new String[] { "" + p.getRowId() };
        String[] columns = new String[] {FORMTEXT_NAME, FORMID_NAME, START_NAME, STOP_NAME, ID_NAME};

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);

		c.moveToFirst();
		while (!c.isAfterLast()) {

			String formText = c.getString(c.getColumnIndex(FORMTEXT_NAME));
			String formId = c.getString(c.getColumnIndex(FORMID_NAME));
			
			Schedule s = new Schedule (formId, formText);
			s.mPupilId = p.getRowId();
			s.setStart(new Date(c.getInt(c.getColumnIndex(START_NAME))));
			s.setStop(new Date(c.getInt(c.getColumnIndex(STOP_NAME))));
			s.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
			
			set.add(s);
			c.moveToNext();
		}
		return set;
	}
	
	public static Schedule getBySchoolYear(Pupil p, String schoolYear) {
		
		String selection = FORMTEXT_NAME + " = ? AND " + PUPILID_NAME + " = ?";
        String[] args = new String[] { schoolYear, "" + p.getRowId() };
        String[] columns = new String[] {FORMID_NAME, START_NAME, STOP_NAME, ID_NAME};

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);
        c.moveToFirst();
        if (c.getCount() <= 0)
        	return null;
		
		Schedule s = new Schedule (c.getString(c.getColumnIndex(FORMID_NAME)), schoolYear);
		s.mPupilId = p.getRowId();
		long start = c.getLong(c.getColumnIndex(START_NAME));
		s.setStart(new Date(start));		
		long stop = c.getLong(c.getColumnIndex(STOP_NAME));
		s.setStop(new Date(stop));
		s.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		
		return s;
	}
	
	public static Schedule getByDate(Pupil p, Date day) {
		
		long date = day.getTime();

		String selection = START_NAME + " <= ? AND " + STOP_NAME + " >= ? AND "
				+ PUPILID_NAME + " = ?";
        String[] args = new String[] { "" + date, "" + date, "" + p.getRowId() };
        String[] columns = new String[] {FORMID_NAME, FORMTEXT_NAME, START_NAME, STOP_NAME, ID_NAME};

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);
        c.moveToFirst();
        if (c.getCount() <= 0)
        	return null;
		
		Schedule s = new Schedule(c.getString(c.getColumnIndex(FORMID_NAME)),
				c.getString(c.getColumnIndex(FORMTEXT_NAME)));
		s.mPupilId = p.getRowId();
		long start = c.getLong(c.getColumnIndex(START_NAME));
		s.setStart(new Date(start));		
		long stop = c.getLong(c.getColumnIndex(STOP_NAME));
		s.setStop(new Date(stop));
		s.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		
		return s;
	}

	public Schedule addLesson(Lesson l) throws IllegalStateException {

		Week w = null;

		// Find week and mark as loaded
		if ((w = getWeek(l.getStart())) == null) {
			throw new IllegalStateException("Week is not loaded");
		}

		w.setLoaded().update();
		l.insert(this);
		return this;
	}
	
	public Schedule addSemester(GradeSemester s) {
		s.insert(this);
		return this;
	}
	
	public GradeSemester getSemester(Date day) {

		return GradeSemester.getByDate(this, day);
	}
	
	public GradeSemester getSemester(String formId) {

		return GradeSemester.getByFormId(this, formId);
	}

	public Schedule addGradeRec(GradeRec gr) {

		GradeRec.insert(this);
		return this;
	}

	public Lesson getLessonByStart(Date start) {
		
		return Lesson.getByStart(this, start);
	}
	
	public ArrayList<Lesson> getAllLessonsByDate(Date date) {
		
		Date start;
		Date stop;
		
		Calendar cal = Calendar.getInstance();				
		
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        start = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        stop = cal.getTime();
		
		return Lesson.getAllByDate(this, start, stop);
	}
	
	public Lesson getLessonByNumber(Date date, int number) {
		
		Date start;
		Date stop;
		
		Calendar cal = Calendar.getInstance();				
		
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        start = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        stop = cal.getTime();

		return Lesson.getByNumber(this, start, stop, number);
	}

	public Schedule addWeek(Week w) {
		
//		Log.d(this.toString(), TS.get() + this.toString()
//				+ " addWeek() = " + w );
		w.insert(this);
		return this;
	}

	public Week getWeek(Date day) {
		
		Week w = Week.getByDate(this, day);
		Log.d(this.toString(), TS.get() + this.toString()
				+ " getWeek() = " + w );
		return w;
	}
	
	public Week getWeek(String formId) {

		Week w = Week.getByFormId(this, formId);
		Log.d(this.toString(), TS.get() + this.toString()
				+ " getWeek() = " + w );
		return w;
	}

	public final Set<Week> getWeekSet() {
		
		return Week.getSet(this);
	}
	
	public final Set<Lesson> getLessonSet() {
		
		return Lesson.getSet(this);
	}

	public final Set<GradeRec> getGradeRecSet() {
		
		return GradeRec.getSet(this);
	}
	
	public String toString() {

		String res = "Weeks:\n";

		for (Week entry : getWeekSet()) {
			res += entry.toString() + "\n";
		}

		res += "\nLessons:\n";

		for (Lesson entry : getLessonSet()) {
			res += entry.toString() + "\n";
		}

//		res += "\nGrade Records:\n";
//
//		for (GradeRec entry : getGradeRecSet()) {
//			res += entry.toString() + "\n";
//		}

		return res;
	}
}
