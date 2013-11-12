package com.shinymetal.gradereport.objects;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public class Schedule extends FormTimeInterval {
	
	private final static String FORMID_NAME = "FORMID";
	private final static String FORMTEXT_NAME = "FORMTEXT";
	private final static String PUPILID_NAME = "PUPILID";
	private final static String START_NAME = "START";
	private final static String STOP_NAME = "STOP";
	
	public final static String TABLE_NAME = "SCHEDULE";
	public final static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ FORMID_NAME + " TEXT PRIMARY KEY ASC, "
			+ PUPILID_NAME + " TEXT REFERENCES PUPIL (FORMID), "
			+ FORMTEXT_NAME + " TEXT, "
			+ START_NAME + " INTEGER, "
			+ STOP_NAME + " INTEGER);";

	protected SortedMap<Date, Lesson> lessons;
	protected SortedMap<Date, Week> weeks;
	
	protected SortedMap<String, GradeRec> gradeRecs;
	protected SortedMap<Date, GradeSemester> semesters;
	
	protected String currentSemesterId;

	public Schedule() {

		Comparator<Date> comp = new Comparator<Date>() {
			public int compare(Date d1, Date d2) {
				if (d1.equals(d2))
					return 0;
				if (d1.before(d2))
					return -1;

				return 1;
			}
		};

		lessons = new TreeMap<Date, Lesson>(comp);
		gradeRecs = new TreeMap<String, GradeRec> ();
		weeks = new TreeMap<Date, Week>(comp);
		semesters = new TreeMap<Date, GradeSemester> (comp);
	}

	public Schedule(String formId, String schoolYear) {

		this();

		setFormId(formId);
		setFormText(schoolYear);
	}

	public void addLesson(Lesson l) throws IllegalStateException {

		Week w = null;

		// Find week and mark as loaded
		if ((w = getWeek(l.getStart())) == null) {
			throw new IllegalStateException("Week is not loaded");
		}

		w.setLoaded();
		lessons.put(l.getStart(), l);
	}
	
	public void addSemester(GradeSemester s) {
		semesters.put(s.getStart(), s);
	}
	
	public String getCurrentSemesterId() {
		return currentSemesterId;
	}

	public void setCurrentSemesterId(String currentSemesterId) {
		this.currentSemesterId = currentSemesterId;
	}
	
	public GradeSemester getSemester(Date day) throws NullPointerException {

		SortedMap<Date, GradeSemester> subMapElements = semesters.subMap(day, day);
		for (Map.Entry<Date, GradeSemester> entry : subMapElements.entrySet()) {
			return entry.getValue();
		}
		
		throw new NullPointerException();
	}
	
	public GradeSemester getSemester(String formId) throws NullPointerException {

		for (Map.Entry<Date, GradeSemester> entry : semesters.entrySet()) {
			
			if (entry.getValue().getFormId().equals(formId))
				return entry.getValue();
		}
		
		throw new NullPointerException();
	}
	
	public GradeSemester getCurrentSemester () {

		return getSemester(currentSemesterId);
	}

	public void addGradeRec(GradeRec gr) throws IllegalStateException {

		gradeRecs.put(gr.getFormText(), gr);
	}

	public Lesson getLesson(Date start) {
		return lessons.get(start);
	}
	
	public Lesson getLessonByNumber(Date date, int number) throws NullPointerException {
		
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

		SortedMap<Date, Lesson> subMapElements = lessons.subMap(start, stop);
		for (Map.Entry<Date, Lesson> entry : subMapElements.entrySet()) {
			if( entry.getValue().getNumber() == number) {
				return entry.getValue();
			}
		}
		
		throw new NullPointerException();
	}

	public void addWeek(Week w) {
		weeks.put(w.getStart(), w);
	}

	public Week getWeek(Date day) throws NullPointerException {

		SortedMap<Date, Week> subMapElements = weeks.subMap(
				Week.getWeekStart(day), Week.getWeekStop(day));
		for (Map.Entry<Date, Week> entry : subMapElements.entrySet()) {
			return entry.getValue();
		}
		
		throw new NullPointerException();
	}
	
	public Week getWeek(String formId) throws NullPointerException {

		for (Map.Entry<Date, Week> entry : weeks.entrySet()) {
			
			if (entry.getValue().getFormId().equals(formId))
				return entry.getValue();
		}
		
		throw new NullPointerException();
	}

	public final Set<Entry<Date, Week>> getWeekSet() {
		return weeks.entrySet();
	}

	public final Set<Entry<String, GradeRec>> getGradeRecSet() {
		return gradeRecs.entrySet();
	}

	public String toString() {

		String res = "Weeks:\n";

		for (Map.Entry<Date, Week> entry : weeks.entrySet()) {
			res += entry.getValue().toString() + "\n";
		}

		res += "\nLessons:\n";

		for (Map.Entry<Date, Lesson> entry : lessons.entrySet()) {
			res += entry.getValue().toString() + "\n";
		}
		
		res += "\nGrade Records:\n";

		for (Map.Entry<String, GradeRec> entry : gradeRecs.entrySet()) {
			res += entry.getValue().toString() + "\n";
		}

		return res;
	}
}
