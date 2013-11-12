package com.shinymetal.gradereport.db;

import com.shinymetal.gradereport.objects.GradeRec;
import com.shinymetal.gradereport.objects.GradeSemester;
import com.shinymetal.gradereport.objects.Lesson;
import com.shinymetal.gradereport.objects.Pupil;
import com.shinymetal.gradereport.objects.Schedule;
import com.shinymetal.gradereport.objects.Week;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database {
	
    private static final String DATABASE_NAME = "gradereport";    
    private static final int DATABASE_VERSION = 1;
    
    private static volatile Context mContext = null;

    
    public static void setContext(Context context) {

    	if (mContext == null)
    		mContext = context;
    }
    
    public static SQLiteOpenHelper getHelper() {
    	
    	return DatabaseOpenHelper.getInstance(mContext);
    }
    
    public static SQLiteDatabase getWritable() {
    	
    	return getHelper().getWritableDatabase();
    }
    
    public static SQLiteDatabase getReadable() {
    	
    	return getHelper().getReadableDatabase();
    }

	/**
	 * This creates/opens the database.
	 */
	private static class DatabaseOpenHelper extends SQLiteOpenHelper {

		private SQLiteDatabase mDatabase;

		private DatabaseOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		private static volatile DatabaseOpenHelper instance;

		public static DatabaseOpenHelper getInstance(Context context) {

			DatabaseOpenHelper localInstance = instance;

			if (localInstance == null) {

				synchronized (DatabaseOpenHelper.class) {

					localInstance = instance;
					if (localInstance == null) {
						instance = localInstance = new DatabaseOpenHelper(context);
					}
				}
			}

			return localInstance;
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {

			mDatabase = db;
			
			mDatabase.execSQL(Pupil.TABLE_CREATE);
			mDatabase.execSQL(Schedule.TABLE_CREATE);
			mDatabase.execSQL(Week.TABLE_CREATE);	 
			mDatabase.execSQL(GradeSemester.TABLE_CREATE);
			mDatabase.execSQL(Lesson.TABLE_CREATE);
			mDatabase.execSQL(GradeRec.TABLE_CREATE);			 
			mDatabase.execSQL(GradeRec.MarkRec.TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.w(this.toString(), "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            
            db.execSQL("DROP TABLE IF EXISTS " + GradeRec.MarkRec.TABLE_CREATE);
            db.execSQL("DROP TABLE IF EXISTS " + GradeRec.TABLE_CREATE);
            db.execSQL("DROP TABLE IF EXISTS " + Lesson.TABLE_CREATE);
            db.execSQL("DROP TABLE IF EXISTS " + GradeSemester.TABLE_CREATE);
            db.execSQL("DROP TABLE IF EXISTS " + Week.TABLE_CREATE);
            db.execSQL("DROP TABLE IF EXISTS " + Schedule.TABLE_CREATE);
            db.execSQL("DROP TABLE IF EXISTS " + Pupil.TABLE_CREATE);

            onCreate(db);
		}
	}
}
