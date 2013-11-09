package com.shinymetal.objects;

import java.util.ArrayList;
import java.util.List;

public class GradeRec extends FormTimeInterval {
	
	public class MarkRec {
		
		public String marks;
		public String comment;
		
		public MarkRec () {
			
		}
		
		public MarkRec (String marks, String comment) {
		
			this.marks = marks;
			this.comment = comment;
		}
	}

	protected List<MarkRec> marks;

	protected int absent;
	protected int released;
	protected int sick;
	protected float average;
	protected int total;
	
	public GradeRec () {
		
		marks = new ArrayList<MarkRec> ();		
	}

	public GradeRec(String name) {
		
		this ();
		setFormText(name);
	}
	
	public void addMarcRec (MarkRec rec) {
		
		marks.add(rec);
	}
	
	public String toString() {
		
		String res = getFormText() + ": ";		
		res += total + " ( abs: " + absent + ", rel: " + released;
		res += ", sick: " + sick + ", av: " + average + ") m:";
		
		for (MarkRec rec : marks) {

			res += " " + rec.marks + " (" + rec.comment + ")"; 
		}
		
		return res;
	}
	
	public int getAbsent() {
		return absent;
	}
	public void setAbsent(int absent) {
		this.absent = absent;
	}
	public int getReleased() {
		return released;
	}
	public void setReleased(int released) {
		this.released = released;
	}
	public int getSick() {
		return sick;
	}
	public void setSick(int sick) {
		this.sick = sick;
	}
	public float getAverage() {
		return average;
	}
	public void setAverage(float average) {
		this.average = average;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
}