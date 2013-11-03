package com.shinymetal.objects;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Lesson {
	
	protected Date start;
	protected Date stop;
	
	protected String name;
	protected String teacher;
	
	protected String theme;
	protected String homework;
	protected String marks;
	protected String comment;
	
	protected int number;

	public void setTimeframe (String date, String timeframe) throws ParseException {
		
		if (start == null) start = new Date ();
		if (stop == null) stop = new Date ();

		String timeB = timeframe.substring(0, timeframe.indexOf("-") - 1);
		String timeE = timeframe.substring(timeframe.indexOf("-") + 2,
				timeframe.length());
		
		start = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
				.parse(date + " " + timeB);
		stop = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
				.parse(date + " " + timeE);
	}

	public static Date getStart(String date, String timeframe)
			throws ParseException {

		String timeB = timeframe.substring(0, timeframe.indexOf("-") - 1);
		return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
				.parse(date + " " + timeB);
	}

	public Date getStart() {
		return start;
	}

	public Date getStop() {
		return stop;
	}

	public String getName() {
		return name;
	}

	public String getTeacher() {
		return teacher;
	}

	public String getTheme() {
		return theme;
	}

	public String getHomework() {
		return homework;
	}

	public String getMarks() {
		return marks;
	}
	
	public String getComment() {
		return comment;
	}

	public int getNumber() {
		return number;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public void setHomework(String homework) {
		this.homework = homework;
	}

	public void setMarks(String marks) {
		this.marks = marks;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getTimeframe () {
		
		if (start == null || stop == null)
			return null;

		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
		
	    String res = fmt.format(start) + " - " + fmt.format(stop);
		return res;
	}

	public Lesson() {
		
		this( "-1", "", "", new Date (), new Date ());
	}

	public Lesson(String number, String name, String teacher) {

		this.name = name;
		this.teacher = teacher;
		this.number = Integer.parseInt(number);
	}
	
	public Lesson(String number, String name, String teacher, Date start, Date stop) {

		this(number, name, teacher);
		
		this.start = start;
		this.stop = stop;		
	}

	public String toString() {

		String res = "" + number + " " + start.toString() + " - "
				+ stop.toString() + " " + name + " / " + teacher
				+ ", H: " + homework + ", M: " + marks + ", C: " + comment;
		return res;
	}
}
