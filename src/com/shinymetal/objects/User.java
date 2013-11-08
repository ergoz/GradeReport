package com.shinymetal.objects;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public class User {

	protected String name;

	protected SortedMap<String, Pupil> pupils;
	protected String currentPupilId;

	public User() {

		pupils = new TreeMap<String, Pupil>();
	}

	public User(String n) {

		this();
		name = n;
	}

	public void setName(String desc) {
		name = desc;
	}

	public void addPupil(Pupil p) {

		pupils.put(p.getFormId(), p);
	}

	public void setCurrentPupilId(String fId) {
		currentPupilId = fId;
	}
	
	public String getCurrentPupilId() {
		return currentPupilId;
	}

	public final Set<Entry<String, Pupil>> getPupilSet() {
		return pupils.entrySet();
	}

	public Pupil getPupilByFormId(String fId) throws NullPointerException {

		Pupil p;
		
		if ((p = pupils.get(fId)) == null)
			throw new NullPointerException();
		
		return p;
	}

	public Pupil getCurrentPupil() throws NullPointerException {

		if (currentPupilId == null)
				throw new NullPointerException ();
		
		return getPupilByFormId(currentPupilId);
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return getName();
	}
}
