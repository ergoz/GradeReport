package com.shinymetal.gradereport.objects;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;

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
			+ STOP_NAME + " INTEGER);";
	
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
		s.setStart(new Date(c.getInt(c.getColumnIndex(START_NAME))));
		s.setStop(new Date(c.getInt(c.getColumnIndex(STOP_NAME))));
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
		s.setStart(new Date(c.getInt(c.getColumnIndex(START_NAME))));
		s.setStop(new Date(c.getInt(c.getColumnIndex(STOP_NAME))));
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
		s.setStart(new Date(c.getInt(c.getColumnIndex(START_NAME))));
		s.setStop(new Date(c.getInt(c.getColumnIndex(STOP_NAME))));
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

	public Lesson getLesson(Date start) {
		
		return Lesson.getByDate(this, start);
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
        stop = cal.getTime();

		return Lesson.getByNumber(this, start, stop, number);
	}

	public Schedule addWeek(Week w) {
		
		w.insert(this);
		return this;
	}

	public Week getWeek(Date day) {
		
		return Week.getByDate(this, day);
	}
	
	public Week getWeek(String formId) {

		return Week.getByFormId(this, formId);
	}

	public final Set<Week> getWeekSet() {
		
		return Week.getSet(this);
	}

	public final Set<GradeRec> getGradeRecSet() {
		
		return GradeRec.getSet(this);
	}
}