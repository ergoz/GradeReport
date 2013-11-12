package com.shinymetal.gradereport.objects;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.shinymetal.gradereport.db.Database;
import com.shinymetal.gradereport.utils.GshisLoader;

import android.content.ContentValues;
import android.database.Cursor;

public class Pupil extends FormSelectableField {	
	
	private final static String FORMID_NAME = "FORMID";
	private final static String FORMTEXT_NAME = "FORMTEXT";
	private final static String USERNAME_NAME = "USERNAME";
	
	public final static String TABLE_NAME = "PUPIL";
	public final static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ " (" + FORMID_NAME + " TEXT PRIMARY KEY ASC, "
			+ FORMTEXT_NAME	+ " TEXT, "
			+ USERNAME_NAME + " TEXT);";

	public Pupil(String n) {

		setFormText(n);
	}

	public Pupil(String n, String fId) {

		this(n);
		setFormId(fId);
	}
	
	public long insert() {
		
        ContentValues values = new ContentValues();
        
        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());
        values.put(USERNAME_NAME, GshisLoader.getInstance().getLogin());

        return Database.getWritable().insert(TABLE_NAME, null, values);		
	}
	
	public static Pupil getByFormId(String fId) {
		
		String selection = FORMID_NAME + " = ? AND " + USERNAME_NAME + " = ?";
        String[] args = new String[] { fId, GshisLoader.getInstance().getLogin() };
        String[] columns = new String[] {FORMTEXT_NAME};

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);
        c.moveToFirst();
        if (c.getCount() <= 0)
        	return null;
        
        String formText = c.getString(c.getColumnIndex(FORMTEXT_NAME));
        return new Pupil(formText, fId); 
	}
	
	public static Pupil getByFormName(String name) {
		
		String selection = FORMTEXT_NAME + " = ? AND " + USERNAME_NAME + " = ?";
        String[] args = new String[] { name, GshisLoader.getInstance().getLogin() };
        String[] columns = new String[] {FORMID_NAME};

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);
        c.moveToFirst();
        if (c.getCount() <= 0)
        	return null;
        
        String formId = c.getString(c.getColumnIndex(FORMID_NAME));
        return new Pupil(name, formId); 
	}
	
	public static final Set<Pupil> getPupilSet() {
		
		Set<Pupil> set = new HashSet<Pupil> (); 
		
		String selection = USERNAME_NAME + " = ?";
        String[] args = new String[] { GshisLoader.getInstance().getLogin() };
        String[] columns = new String[] {FORMTEXT_NAME, FORMID_NAME};

        Cursor c = Database.getReadable().query(TABLE_NAME, columns, selection, args, null, null, null);

		c.moveToFirst();
		while (!c.isAfterLast()) {

			String formText = c.getString(c.getColumnIndex(FORMTEXT_NAME));
			String formId = c.getString(c.getColumnIndex(FORMID_NAME));
			set.add(new Pupil(formText, formId));
			c.moveToNext();
		}
		return set;
	}

	public final Set<Schedule> getScheduleSet() {
		
		// TODO Auto-generated method stub
		return null;
	}

	public void addSchedule(Schedule s) {

		// TODO Auto-generated method stub
	}

	public Schedule getScheduleByFormId(String fId) {
	
		// TODO Auto-generated method stub
		return null;
	}

	public Schedule getScheduleBySchoolYear(String schoolYear) {

		// TODO Auto-generated method stub
		return null;
	}

	public String toString() {
		return getFormText() + " f: " + getFormId ();
	}

	public Schedule getScheduleByDate(Date day) {

		// TODO Auto-generated method stub
		return null;
	}
}
