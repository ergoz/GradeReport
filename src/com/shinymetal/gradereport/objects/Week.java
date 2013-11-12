package com.shinymetal.gradereport.objects;

import java.util.Calendar;
import java.util.Date;

public class Week extends FormTimeInterval {
	
	public static final String TABLE_NAME = "WEEK";
	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ "FORMID TEXT PRIMARY KEY ASC, "
			+ "SCHEDULEID TEXT REFERENCES SCHEDULE (FORMID), "
			+ "FORMTEXT TEXT, "
			+ "START INTEGER, "
			+ "STOP INTEGER, "
			+ "LOADED INTEGER);";

	protected boolean loaded;
	
	public Week () {
		
		loaded = false;
	}

	@Override
	public void setStart (Date day) {
		
		super.setStart(getWeekStart(day));
		super.setStop(getWeekStop(day));		
	}
	
	@Override
	public void setStop (Date day) {
		
		super.setStart(getWeekStart(day));
		super.setStop(getWeekStop(day));		
	}
	
	public void setLoaded () { loaded = true; }	
	public boolean getLoaded () { return loaded; }

		public static Date getWeekStart (Date day) {
		Calendar cal = Calendar.getInstance();				
		
        cal.setTime(day);
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DATE, -1);
        }
        
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        return cal.getTime();
	}
	
	public static Date getWeekStop (Date day) {
		Calendar cal = Calendar.getInstance();				
		
        cal.setTime(day);
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            cal.add(Calendar.DATE, 1);
        }

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
	}
	
	public String toString() {
		return getStart().toString() + " - " + getStop().toString() + " l: " + loaded + " f: " + getFormId();
	}
}
