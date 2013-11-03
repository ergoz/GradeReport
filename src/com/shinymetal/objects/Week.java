package com.shinymetal.objects;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Week {
	
	protected boolean loaded;
	protected String formId;
	
	protected Date start;
	protected Date stop;
	
	public Week () {
		
		loaded = false;
	}

	public void setStartStop(String weekStartStop, String schoolYear)
			throws ParseException {

		start = getWeekStart(weekStartStop, schoolYear);
		stop = getWeekStop(weekStartStop, schoolYear);
	}
	
	public void setFormValue(String formId) { this.formId = formId; }
	public String getFormId () { return formId; }
	
	public void setLoaded () { loaded = true; }	
	public boolean getLoadedState () { return loaded; }
	
	public Date getStart () { return start; }
	public Date getStop () { return stop; }

	public static Date getWeekStart(String week, String schoolYear)
			throws ParseException {

		String begin = week.substring(0, week.indexOf("-") - 1);
		String month = begin.substring(begin.indexOf(".") + 1, begin.length());

		String year;
		if (Integer.parseInt(month) > 7) {
			year = schoolYear.substring(0, schoolYear.indexOf("-") - 1);
		} else {
			year = schoolYear.substring(schoolYear.indexOf("-") + 2,
					schoolYear.length());
		}

		return new SimpleDateFormat("yyyy dd.MM", Locale.ENGLISH).parse(year
				+ " " + begin);
	}

	public static Date getWeekStop(String week, String schoolYear)
			throws ParseException {

		String timeB = week.substring(0, week.indexOf("-") - 1);
		String timeE = week.substring(week.indexOf("-") + 2,
				week.length());
		String month = timeB.substring(timeB.indexOf(".") + 1, timeB.length());

		String year;
		if (Integer.parseInt(month) > 7) {
			year = schoolYear.substring(0, schoolYear.indexOf("-") - 1);
		} else {
			year = schoolYear.substring(schoolYear.indexOf("-") + 2,
					schoolYear.length());
		}

		return new SimpleDateFormat("yyyy dd.MM", Locale.ENGLISH).parse(year
				+ " " + timeE);
	}
	
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
		return start.toString() + " - "	+ stop.toString() + " l: " + loaded + " f: " + formId;
	}
}
