package com.shinymetal.gradereport.objects;

import java.util.Date;

public class GradeSemester extends FormTimeInterval {
	
	public static final String TABLE_NAME = "SEMESTER";
	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ "ID INTEGER PRIMARY KEY ASC, "
			+ "FORMID TEXT, "
			+ "SCHEDULEID INTEGER REFERENCES SCHEDULE (ID), "
			+ "FORMTEXT TEXT, "
			+ "START INTEGER, "
			+ "STOP INTEGER, "
			+ "LOADED INTEGER);";
	
	protected boolean loaded;

	public GradeSemester() {

		loaded = false;
	}
	
	public void setLoaded () { loaded = true; }	
	public boolean getLoaded () { return loaded; }

	public long insert(Schedule schedule) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static GradeSemester getByDate(Schedule schedule, Date day) {
		// TODO Auto-generated method stub
		return null;
	}

	public static GradeSemester getByFormId(Schedule schedule, String formId) {
		// TODO Auto-generated method stub
		return null;
	}
}