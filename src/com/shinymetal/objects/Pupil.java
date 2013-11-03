package com.shinymetal.objects;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class Pupil {

	protected String name;
	protected String formId;

	protected SortedMap<String, Schedule> schedules;
	protected String currentScheduleId;

	public Pupil() {
		 schedules = new TreeMap<String, Schedule> ();
	}

	public Pupil(String n) {

		this();
		name = n;
	}

	public Pupil(String n, String fId) {

		this(n);
		formId = fId;
	}

	public final Set<Entry<String, Schedule>> getScheduleSet() {
		return schedules.entrySet();
	}

	public void addSchedule(Schedule s) {

		schedules.put(s.getFormId(), s);
	}

	public Schedule getScheduleByFormId(String fId) throws NullPointerException {
		
		Schedule s;
		
		if ((s = schedules.get(fId)) == null)
			throw new NullPointerException();
		
		return s;
	}

	public Schedule getScheduleBySchoolYear(String schoolYear)
			throws NullPointerException {

		for (Map.Entry<String, Schedule> entry : schedules.entrySet()) {

			if (entry.getValue().getSchoolYear().equals(schoolYear))
				return entry.getValue();
		}

		throw new NullPointerException();
	}

	public void setCurrentScheduleId(String formId) {
		currentScheduleId = formId;
	}

	public String getCurrentScheduleId() {
		return currentScheduleId;
	}

	public Schedule getCurrentSchedule() throws NullPointerException {

		if (currentScheduleId == null)
			throw new NullPointerException();

		return getScheduleByFormId(currentScheduleId);
	}

	public String getName() {
		return name;
	}

	public String getFormId() {
		return formId;
	}

	public String toString() {
		return name + " f: " + formId;
	}
}
