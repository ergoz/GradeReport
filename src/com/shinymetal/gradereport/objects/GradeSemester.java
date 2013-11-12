package com.shinymetal.gradereport.objects;

public class GradeSemester extends FormTimeInterval {
	
	public static final String TABLE_NAME = "SEMESTER";
	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ "FORMID TEXT PRIMARY KEY ASC, "
			+ "SCHEDULEID TEXT REFERENCES SCHEDULE (FORMID), "
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
}