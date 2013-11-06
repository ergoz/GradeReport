package com.shinymetal.objects;

public class GradeSemester extends FormTimeInterval {
	
	protected boolean loaded;

	public GradeSemester() {

		loaded = false;
	}
	
	public void setLoaded () { loaded = true; }	
	public boolean getLoaded () { return loaded; }
}