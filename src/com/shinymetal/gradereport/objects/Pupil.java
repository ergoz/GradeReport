package com.shinymetal.gradereport.objects;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.shinymetal.gradereport.db.Database;
import com.shinymetal.gradereport.utils.GshisLoader;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class Pupil extends FormSelectableField {	
	
	private final static String ID_NAME = "ID";
	private final static String FORMID_NAME = "FORMID";
	private final static String FORMTEXT_NAME = "FORMTEXT";
	private final static String USERNAME_NAME = "USERNAME";
	
	public final static String TABLE_NAME = "PUPIL";
	public final static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ ID_NAME + " INTEGER PRIMARY KEY ASC, "
			+ FORMID_NAME + " TEXT, "
			+ FORMTEXT_NAME	+ " TEXT, "
			+ USERNAME_NAME + " TEXT);";
	
	private long mRowId;

	public Pupil(String n) {

		setFormText(n);
	}

	public Pupil(String n, String fId) {

		this(n);
		setFormId(fId);
	}
	
	public long getRowId () { return mRowId; }
	
	public long insert() {
		
        ContentValues values = new ContentValues();
        
        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());
        values.put(USERNAME_NAME, GshisLoader.getInstance().getLogin());
        
		

		Log.i("AAAAAAAAAAAa", TS.get() 
				+ " Pupil.insert() : started");


        return mRowId = Database.getWritable().insert(TABLE_NAME, null, values);		
	}
	
	public static Pupil getByFormId(String fId) {
		
		String selection = FORMID_NAME + " = ? AND " + USERNAME_NAME + " = ?";
        String[] args = new String[] { fId, GshisLoader.getInstance().getLogin() };
        String[] columns = new String[] {FORMTEXT_NAME, ID_NAME};

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);
        c.moveToFirst();
        if (c.getCount() <= 0)
        	return null;
        
        String formText = c.getString(c.getColumnIndex(FORMTEXT_NAME));
        Pupil p = new Pupil(formText, fId);
        p.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
        return p; 
	}
	
	public static Pupil getByFormName(String name) {
		
		String selection = FORMTEXT_NAME + " = ? AND " + USERNAME_NAME + " = ?";
        String[] args = new String[] { name, GshisLoader.getInstance().getLogin() };
        String[] columns = new String[] {FORMID_NAME, ID_NAME};

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);
        c.moveToFirst();
        if (c.getCount() <= 0)
        	return null;
        
        String formId = c.getString(c.getColumnIndex(FORMID_NAME));
        Pupil p = new Pupil(name, formId);
        p.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
        return p; 
	}
	
	public static final Set<Pupil> getSet() {
		
		Set<Pupil> set = new HashSet<Pupil> (); 
		
		String selection = USERNAME_NAME + " = ?";
        String[] args = new String[] { GshisLoader.getInstance().getLogin() };
        String[] columns = new String[] {FORMTEXT_NAME, FORMID_NAME};

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);

		c.moveToFirst();
		while (!c.isAfterLast()) {

			String formText = c.getString(c.getColumnIndex(FORMTEXT_NAME));
			String formId = c.getString(c.getColumnIndex(FORMID_NAME));
			Pupil p = new Pupil(formText, formId); 
			p.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
			set.add(p);
			
			c.moveToNext();
		}
		return set;
	}

	public final Set<Schedule> getScheduleSet() {
		
		return Schedule.getSet(this);
	}

	public Pupil addSchedule(Schedule s) {

		s.insert(this);
		return this;
	}

	public Schedule getScheduleByFormId(String fId) {
	
		return Schedule.getByFormId(this, fId);
	}

	public Schedule getScheduleBySchoolYear(String schoolYear) {

		return Schedule.getBySchoolYear(this, schoolYear);
	}

	public String toString() {
		return getFormText() + " f: " + getFormId ();
	}

	public Schedule getScheduleByDate(Date day) {

		return Schedule.getByDate(this, day);
	}
}
