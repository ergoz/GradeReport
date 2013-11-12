package com.shinymetal.gradereport.objects;

public class Lesson extends FormTimeInterval {
	
	public static final String TABLE_NAME = "LESSON";
	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ "FORMID TEXT PRIMARY KEY ASC, "
			+ "SCHEDULEID TEXT REFERENCES SCHEDULE (FORMID), "
			+ "FORMTEXT TEXT, "
			+ "START INTEGER, "
			+ "STOP INTEGER, "
			+ "NUMBER INTEGER, "
			+ "START INTEGER, "
			+ "STOP INTEGER, "
			+ "TEACHER TEXT, "
			+ "THEME TEXT, "
			+ "HOMEWORK TEXT, "
			+ "MARKS TEXT, "
			+ "COMMENT TEXT);";

	protected String teacher;
	
	protected String theme;
	protected String homework;
	protected String marks;
	protected String comment;
	
	protected int number;

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
	
	public void setTeacher(String teacher) {
		this.teacher = teacher;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}

	public Lesson() {
	}

	public String toString() {

		String res = "" + number + " " + getStop().toString() + " - "
				+ getStop().toString() + " " + getFormText() + " / " + teacher
				+ ", H: " + homework + ", M: " + marks + ", C: " + comment;
		return res;
	}
}
