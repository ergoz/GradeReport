package com.shinymetal.gradereport.objects;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;

import com.shinymetal.gradereport.db.Database;

public class GradeSemester extends FormTimeInterval {
	
	public static final String TABLE_NAME = "SEMESTER";
	
	private final static String ID_NAME = "ID";
	private final static String FORMID_NAME = "FORMID";
	private final static String FORMTEXT_NAME = "FORMTEXT";
	private final static String SCHEDULEID_NAME = "SCHEDULEID";
	private final static String START_NAME = "START";
	private final static String STOP_NAME = "STOP";
	private final static String LOADED_NAME = "LOADED";
	
	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ ID_NAME + " INTEGER PRIMARY KEY ASC, "
			+ FORMID_NAME + " TEXT, "
			+ SCHEDULEID_NAME + " INTEGER REFERENCES SCHEDULE (ID), "
			+ FORMTEXT_NAME + " TEXT, "
			+ START_NAME + " INTEGER, "
			+ STOP_NAME + " INTEGER, "
			+ LOADED_NAME + " INTEGER, "
			+ " UNIQUE ( " + START_NAME + ", " + STOP_NAME + ", " + SCHEDULEID_NAME + "));";
	
	protected boolean mLoaded;
	
	private static volatile GradeSemester mLastSemester;

	private long mScheduleId;
	private long mRowId;

	public GradeSemester() {

		mLoaded = false;
	}
	
	public GradeSemester setLoaded() {
		mLoaded = true;
		return this;
	}
	
	public boolean getLoaded () { return mLoaded; }

	public long insert(Schedule schedule) {
		
        ContentValues values = new ContentValues();
        
        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());
        values.put(SCHEDULEID_NAME, mScheduleId = schedule.getRowId());
        values.put(LOADED_NAME, getLoaded());
    	values.put(START_NAME, getStart().getTime());
    	values.put(STOP_NAME, getStop().getTime());	

        return mRowId = Database.getWritable().insert(TABLE_NAME, null, values);
	}

	public static GradeSemester getByDate(Schedule schedule, Date day) {

		long date = day.getTime();
		
		// Elementary caching
		if (mLastSemester != null && mLastSemester.getStart().getTime() <= date
				&& mLastSemester.getStop().getTime() >= date
				&& mLastSemester.mScheduleId == schedule.getRowId()) {
			
			return mLastSemester;
		}

		String selection = START_NAME + " <= ? AND " + STOP_NAME + " >= ? AND "
				+ SCHEDULEID_NAME + " = ?";
        String[] args = new String[] { "" + date, "" + date, "" + schedule.getRowId() };
        String[] columns = new String[] {FORMID_NAME, FORMTEXT_NAME, START_NAME, STOP_NAME, LOADED_NAME, ID_NAME};

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);
        c.moveToFirst();
        if (c.getCount() <= 0) {
        	
        	c.close();
        	return null;
        }
		
        GradeSemester gr = new GradeSemester();
		
		gr.setFormId(c.getString(c.getColumnIndex(FORMID_NAME)));
		gr.setFormText(c.getString(c.getColumnIndex(FORMTEXT_NAME)));
		gr.mScheduleId = schedule.getRowId();
		
		long start = c.getLong(c.getColumnIndex(START_NAME));
		gr.setStart(new Date(start));		
		long stop = c.getLong(c.getColumnIndex(STOP_NAME));
		gr.setStop(new Date(stop));
		gr.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		
		if (c.getInt(c.getColumnIndex(LOADED_NAME)) > 0)
			gr.setLoaded();

		c.close();
		return mLastSemester = gr;

	}

	public static GradeSemester getByFormId(Schedule schedule, String formId) {

		// Elementary caching
		if (mLastSemester != null && mLastSemester.getFormId().equals(formId)
				&& mLastSemester.mScheduleId == schedule.getRowId()) {
			
			return mLastSemester;
		}

		String selection = FORMID_NAME + " = ? AND " + SCHEDULEID_NAME + " = ?";
        String[] args = new String[] { formId, "" + schedule.getRowId() };
        String[] columns = new String[] {FORMTEXT_NAME, START_NAME, STOP_NAME, LOADED_NAME, ID_NAME};

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);
        c.moveToFirst();
        if (c.getCount() <= 0) {
        	
        	c.close();
        	return null;
        }
        
        GradeSemester gr = new GradeSemester();
		
		gr.setFormText(c.getString(c.getColumnIndex(FORMTEXT_NAME)));
		gr.setFormId(formId);
		gr.mScheduleId = schedule.getRowId();
		
		long start = c.getLong(c.getColumnIndex(START_NAME));
		gr.setStart(new Date(start));		
		long stop = c.getLong(c.getColumnIndex(STOP_NAME));
		gr.setStop(new Date(stop));
		gr.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		
		if (c.getInt(c.getColumnIndex(LOADED_NAME)) > 0)
			gr.setLoaded();
		
		c.close();
		return mLastSemester = gr;
	}

	public long update() {
		
		ContentValues values = new ContentValues();
		
        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());        
    	values.put(START_NAME, getStart().getTime());
    	values.put(STOP_NAME, getStop().getTime());
    	values.put(LOADED_NAME, getLoaded());
    	
    	String selection = ID_NAME + " = ?";
        String[] args = new String[] { "" + mRowId };
		
    	return Database.getWritable().update(TABLE_NAME, values, selection, args);	
	}

	public static Set<GradeSemester> getSet(Schedule schedule) {

		Set<GradeSemester> set = new HashSet<GradeSemester> (); 
		
		String selection = SCHEDULEID_NAME + " = ?";
        String[] args = new String[] { "" + schedule.getRowId() };
        String[] columns = new String[] {FORMTEXT_NAME, FORMID_NAME, START_NAME, STOP_NAME, LOADED_NAME, ID_NAME};

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);

		c.moveToFirst();
		while (!c.isAfterLast()) {

			GradeSemester gr = new GradeSemester();
			
			gr.setFormId(c.getString(c.getColumnIndex(FORMID_NAME)));
			gr.setFormText(c.getString(c.getColumnIndex(FORMTEXT_NAME)));
			gr.mScheduleId = schedule.getRowId();
			
			long start = c.getLong(c.getColumnIndex(START_NAME));
			gr.setStart(new Date(start));			
			long stop = c.getLong(c.getColumnIndex(STOP_NAME));
			gr.setStop(new Date(stop));
			gr.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
			
			if (c.getInt(c.getColumnIndex(LOADED_NAME)) > 0)
				gr.setLoaded();
			
			set.add(gr);
			c.moveToNext();
		}
		
		c.close();
		return set;
	}
}