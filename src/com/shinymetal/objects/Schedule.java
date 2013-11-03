package com.shinymetal.objects;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public class Schedule {

	protected SortedMap<Date, Lesson> lessons;
	protected SortedMap<Date, Week> weeks;

	protected String formId;
	protected String schoolYear;

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
		weeks = new TreeMap<Date, Week>(comp);
	}

	public Schedule(String formId, String schoolYear) {

		this();
		this.formId = formId;
		this.schoolYear = schoolYear;
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

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public void setSchoolYear(String formYear) {
		this.schoolYear = formYear;
	}

	public String getFormId() {
		return formId;
	}

	public String getSchoolYear() {
		return schoolYear;
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

	public String toString() {

		String res = "Weeks:\n";

		for (Map.Entry<Date, Week> entry : weeks.entrySet()) {
			res += entry.getValue().toString() + "\n";
		}

		res += "\nLessons:\n";

		for (Map.Entry<Date, Lesson> entry : lessons.entrySet()) {
			res += entry.getValue().toString() + "\n";
		}

		return res;
	}
}
