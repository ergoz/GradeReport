package com.shinymetal.gradereport.objects;

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;

import com.shinymetal.gradereport.db.Database;

public class Lesson extends FormTimeInterval {
	
	private final static String ID_NAME = "ID";
	private final static String FORMID_NAME = "FORMID";
	private final static String FORMTEXT_NAME = "FORMTEXT";
	private final static String SCHEDULEID_NAME = "SCHEDULEID";
	private final static String START_NAME = "START";
	private final static String STOP_NAME = "STOP";
	private final static String NUMBER_NAME = "NUMBER";
	private final static String TEACHER_NAME = "TEACHER";
	private final static String THEME_NAME = "THEME";
	private final static String HOMEWORK_NAME = "HOMEWORK";
	private final static String MARKS_NAME = "MARKS";
	private final static String COMMENT_NAME = "COMMENT";
	
	public static final String TABLE_NAME = "LESSON";
	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ ID_NAME + " PRIMARY KEY ASC, "
			+ FORMID_NAME + " TEXT, "
			+ SCHEDULEID_NAME + " INTEGER REFERENCES SCHEDULE (ID), "
			+ FORMTEXT_NAME + " TEXT, "
			+ START_NAME + " INTEGER, "
			+ STOP_NAME + " INTEGER, "
			+ NUMBER_NAME + " INTEGER, "
			+ TEACHER_NAME + " TEXT, "
			+ THEME_NAME + " TEXT, "
			+ HOMEWORK_NAME + " TEXT, "
			+ MARKS_NAME + " TEXT, "
			+ COMMENT_NAME + " TEXT);";

	protected String teacher;
	
	protected String theme;
	protected String homework;
	protected String marks;
	protected String comment;
	
	protected int number;
	
	private long mScheduleId;
	private long mRowId;

	public String getTeacher() {
		return teacher;
	}

	public String getTheme() {
		return theme;
	}

	public String getHomework() {
		return homework;
	}

	public String getMarks() {
		return marks;
	}
	
	public String getComment() {
		return comment;
	}

	public int getNumber() {
		return number;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public void setHomework(String homework) {
		this.homework = homework;
	}

	public void setMarks(String marks) {
		this.marks = marks;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public void setTeacher(String teacher) {
		this.teacher = teacher;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}

	public Lesson() {
	}

	public String toString() {

		String res = "" + number + " " + getStop().toString() + " - "
				+ getStop().toString() + " " + getFormText() + " / " + teacher
				+ ", H: " + homework + ", M: " + marks + ", C: " + comment;
		return res;
	}

	public long insert(Schedule schedule) {
		
        ContentValues values = new ContentValues();
        
        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());
        values.put(SCHEDULEID_NAME, mScheduleId = schedule.getRowId());
    	values.put(START_NAME, getStart().getTime());
    	values.put(STOP_NAME, getStop().getTime());
    	values.put(NUMBER_NAME, getNumber());
    	values.put(TEACHER_NAME, getTeacher());
    	values.put(THEME_NAME, getTheme());
    	values.put(HOMEWORK_NAME, getHomework());
    	values.put(MARKS_NAME, getMarks());
    	values.put(COMMENT_NAME, getComment());

        return mRowId = Database.getWritable().insert(TABLE_NAME, null, values);		
	}
	
	public long update() {
		
        ContentValues values = new ContentValues();

        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());        
    	values.put(START_NAME, getStart().getTime());
    	values.put(STOP_NAME, getStop().getTime());
    	values.put(NUMBER_NAME, getNumber());
    	values.put(TEACHER_NAME, getTeacher());
    	values.put(THEME_NAME, getTheme());
    	values.put(HOMEWORK_NAME, getHomework());
    	values.put(MARKS_NAME, getMarks());
    	values.put(COMMENT_NAME, getComment());
    	
    	String selection = FORMID_NAME + " = ? AND " + ID_NAME + " = ?";
        String[] args = new String[] { getFormId(), "" + mRowId };
		
    	return Database.getWritable().update(TABLE_NAME, values, selection, args);		
	}

	public static Lesson getByDate(Schedule schedule, Date start) {

		long date = start.getTime();

		String selection = START_NAME + " <= ? AND " + STOP_NAME + " >= ? AND "
				+ SCHEDULEID_NAME + " = ?";
        String[] args = new String[] { "" + date, "" + date, "" + schedule.getRowId() };
		String[] columns = new String[] { FORMID_NAME, FORMTEXT_NAME,
				START_NAME, STOP_NAME, ID_NAME, NUMBER_NAME, TEACHER_NAME,
				THEME_NAME, HOMEWORK_NAME, MARKS_NAME, COMMENT_NAME };

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);
        c.moveToFirst();
        if (c.getCount() <= 0)
        	return null;
		
		Lesson l = new Lesson();
		
		l.setFormId(c.getString(c.getColumnIndex(FORMID_NAME)));
		l.setFormText(c.getString(c.getColumnIndex(FORMTEXT_NAME)));
		l.mScheduleId = schedule.getRowId();
		l.setStart(new Date(c.getInt(c.getColumnIndex(START_NAME))));
		l.setStop(new Date(c.getInt(c.getColumnIndex(STOP_NAME))));
		l.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		l.setNumber(c.getInt(c.getColumnIndex(NUMBER_NAME)));
		l.setTeacher(c.getString(c.getColumnIndex(TEACHER_NAME)));
		l.setTheme(c.getString(c.getColumnIndex(THEME_NAME)));
		l.setHomework(c.getString(c.getColumnIndex(HOMEWORK_NAME)));
		l.setMarks(c.getString(c.getColumnIndex(MARKS_NAME)));
		l.setComment(c.getString(c.getColumnIndex(COMMENT_NAME)));
		
		return l;
	}

	public static Lesson getByNumber(Schedule schedule, Date start, Date stop,
			int number) {

		long date1 = start.getTime();
		long date2 = start.getTime();

		String selection = START_NAME + " <= ? AND " + STOP_NAME + " >= ? AND "
				+ SCHEDULEID_NAME + " = ? AND " + NUMBER_NAME + " = ?";
        String[] args = new String[] { "" + date1, "" + date2, "" + schedule.getRowId(), "" + number };
		String[] columns = new String[] { FORMID_NAME, FORMTEXT_NAME,
				START_NAME, STOP_NAME, ID_NAME, NUMBER_NAME, TEACHER_NAME,
				THEME_NAME, HOMEWORK_NAME, MARKS_NAME, COMMENT_NAME };

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);
        c.moveToFirst();
        if (c.getCount() <= 0)
        	return null;
		
		Lesson l = new Lesson();
		
		l.setFormId(c.getString(c.getColumnIndex(FORMID_NAME)));
		l.setFormText(c.getString(c.getColumnIndex(FORMTEXT_NAME)));
		l.mScheduleId = schedule.getRowId();
		l.setStart(new Date(c.getInt(c.getColumnIndex(START_NAME))));
		l.setStop(new Date(c.getInt(c.getColumnIndex(STOP_NAME))));
		l.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		l.setNumber(c.getInt(c.getColumnIndex(NUMBER_NAME)));
		l.setTeacher(c.getString(c.getColumnIndex(TEACHER_NAME)));
		l.setTheme(c.getString(c.getColumnIndex(THEME_NAME)));
		l.setHomework(c.getString(c.getColumnIndex(HOMEWORK_NAME)));
		l.setMarks(c.getString(c.getColumnIndex(MARKS_NAME)));
		l.setComment(c.getString(c.getColumnIndex(COMMENT_NAME)));
		
		return l;
	}
}
