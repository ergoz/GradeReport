package com.shinymetal.gradereport.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import com.shinymetal.gradereport.objects.GradeRec;
import com.shinymetal.gradereport.objects.GradeSemester;
import com.shinymetal.gradereport.objects.Lesson;
import com.shinymetal.gradereport.objects.Pupil;
import com.shinymetal.gradereport.objects.Schedule;
import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.objects.Week;

public class GshisHTMLParser {

	public final static String whitespace_chars = "" /*
											 * dummy empty string for
											 * homogeneity
											 */
			+ "\\u0009" // CHARACTER TABULATION
			+ "\\u000A" // LINE FEED (LF)
			+ "\\u000B" // LINE TABULATION
			+ "\\u000C" // FORM FEED (FF)
			+ "\\u000D" // CARRIAGE RETURN (CR)
			+ "\\u0020" // SPACE
			+ "\\u0085" // NEXT LINE (NEL)
			+ "\\u00A0" // NO-BREAK SPACE
			+ "\\u1680" // OGHAM SPACE MARK
			+ "\\u180E" // MONGOLIAN VOWEL SEPARATOR
			+ "\\u2000" // EN QUAD
			+ "\\u2001" // EM QUAD
			+ "\\u2002" // EN SPACE
			+ "\\u2003" // EM SPACE
			+ "\\u2004" // THREE-PER-EM SPACE
			+ "\\u2005" // FOUR-PER-EM SPACE
			+ "\\u2006" // SIX-PER-EM SPACE
			+ "\\u2007" // FIGURE SPACE
			+ "\\u2008" // PUNCTUATION SPACE
			+ "\\u2009" // THIN SPACE
			+ "\\u200A" // HAIR SPACE
			+ "\\u2028" // LINE SEPARATOR
			+ "\\u2029" // PARAGRAPH SEPARATOR
			+ "\\u202F" // NARROW NO-BREAK SPACE
			+ "\\u205F" // MEDIUM MATHEMATICAL SPACE
			+ "\\u3000" // IDEOGRAPHIC SPACE
	;
	/* A \s that actually works for Java�s native character set: Unicode */
	public final static String whitespace_charclass = "[" + whitespace_chars + "]";
	/* A \S that actually works for Java�s native character set: Unicode */
	public final static String not_whitespace_charclass = "[^" + whitespace_chars
			+ "]";
	
	public final static Pattern whitespaces_only = Pattern.compile("^" + whitespace_charclass +"+$");
//	public final static Pattern subjects_name = Pattern.compile("^[0-9]{1}\\." + whitespace_charclass + "{1}.*");
	
	public static Pupil getSelectedPupil(Document doc) throws ParseException {

		boolean found = false;
		Pupil p, selectedP = null;

		Elements pupilSelectors = doc.getElementsByAttributeValue("id",
				"ctl00_topMenu_pupil_drdPupils");
		for (Element pupilSelector : pupilSelectors) {

			Elements pupils = pupilSelector.getAllElements();
			for (Element pupil : pupils) {
				if (pupil.tagName().equals("option")) {

					String value = pupil.attr("value");

					found = true;
					if ((p = Pupil.getByFormId(value)) == null) {

						p = new Pupil(pupil.text(), value);
						p.insert();
					}

					if (pupil.hasAttr("selected")
							&& pupil.attr("selected").equals("selected")) {

						selectedP = p;
					}
				}
			}
		}

		if (!found)
			throw new ParseException("Pupils not found", 0);
		
		return selectedP;
	}

	public static Schedule getSelectedSchedule(Document doc, Pupil selPupil) throws ParseException {

		boolean found = false;
		Schedule selectedS = null;

		Elements yearSelectors = doc.getElementsByAttributeValue("id",
				"ctl00_learnYear_drdLearnYears");
		for (Element yearSelector : yearSelectors) {

			Elements years = yearSelector.getAllElements();
			for (Element year : years) {
				if (year.tagName().equals("option")) {

					String value = year.attr("value");
					Schedule schedule;

					found = true;

					try {

						schedule = selPupil.getScheduleByFormId(value);
						
					} catch (NullPointerException e) {

						final SimpleDateFormat f = new SimpleDateFormat(
								"yyyy dd.MM", Locale.ENGLISH);
						schedule = new Schedule(value, year.text());

						Date start = f.parse(year.text().substring(0,
								year.text().indexOf("-") - 1)
								+ " 01.09");
						Date stop = f.parse(year.text().substring(
								year.text().indexOf("-") + 2,
								year.text().length())
								+ " 31.05");
						
						schedule.setStart(start);
						schedule.setStop(stop);
				    	
						selPupil.addSchedule(schedule);
					}

					if (year.hasAttr("selected")
							&& year.attr("selected").equals("selected")) {

						selectedS = schedule;
					}
				}
			}
		}

		if (!found)
			throw new ParseException("Years not found", 0);
		
		return selectedS;
	}

	public static void getWeeks(Document doc, Schedule s) throws ParseException {

		boolean found = false;

		Elements weekSelectors = doc.getElementsByAttributeValue("id",
				"ctl00_body_week_drdWeeks");
		for (Element weekSelector : weekSelectors) {

			Elements weeks = weekSelector.getAllElements();
			for (Element week : weeks) {
				if (week.tagName().equals("option")) {

					String value = week.text();
					Week w;
					found = true;

					try {

						w = s.getWeek(week.attr("value"));
					} catch (NullPointerException e) {

						w = new Week();
						
						String wBegin = value.substring(0, value.indexOf("-") - 1);
						String wMonth = wBegin.substring(wBegin.indexOf(".") + 1, wBegin.length());

						String year;
						if (Integer.parseInt(wMonth) > 7) {
							year = s.getFormText().substring(0, s.getFormText().indexOf("-") - 1);
						} else {
							year = s.getFormText().substring(s.getFormText().indexOf("-") + 2,
									s.getFormText().length());
						}

						w.setStart(new SimpleDateFormat("yyyy dd.MM", Locale.ENGLISH).parse(year
								+ " " + wBegin));

						w.setFormText(week.text());
						w.setFormId(week.attr("value"));

						s.addWeek(w);
					}

					if (week.hasAttr("selected")
							&& week.attr("selected").equals("selected")) {

						w.setLoaded().update();
					}
				}
			}
		}

		if (!found)
			throw new ParseException("Weeks not found", 0);
	}

	public static void getLessons(Document doc, Schedule s) throws ParseException {

		Elements lessonCells = doc.getElementsByAttribute("number");
		for (Element lessonCell : lessonCells) {

			String number = lessonCell.attr("number");
			String time = "";

			Elements timeDetails = lessonCell
					.getElementsByClass("cell-header2");
			for (Element timeDetail : timeDetails) {
				if (timeDetail.hasAttr("style"))
					time = timeDetail.text();
			}

			Elements lessonCellDetails = lessonCell
					.getElementsByAttribute("jsdate");
			for (Element lessonCellDetail : lessonCellDetails) {

				String date = lessonCellDetail.attr("jsdate");
				String subjectName = "";
				String teacherName = "";
				String formId = "";

				Elements subjects = lessonCellDetail
						.getElementsByAttributeValue("class", "lesson-subject");
				for (Element subject : subjects) {
					subjectName = subject.text();
					formId = subject.attr("id");
				}

				Elements teachers = lessonCellDetail
						.getElementsByAttributeValue("class", "lesson-teacher");
				for (Element teacher : teachers) {
					teacherName = teacher.text();
				}

				if (subjectName.length() <= 0) {
					// No lesson scheduled
					continue;
				}

				SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);

				Lesson l;
				
				String timeB = time.substring(0, time.indexOf("-") - 1);
				Date start = format.parse(date + " " + timeB);

				if ((l = s.getLesson(start)) == null) {

					l = new Lesson();
					
					String timeE = time.substring(time.indexOf("-") + 2, time.length());
					Date stop = format.parse(date + " " + timeE);

					l.setStart(start);
					l.setStop(stop);
					l.setFormId(formId);
					l.setFormText(subjectName);
					l.setTeacher(teacherName);
					l.setNumber(Integer.parseInt(number));

					s.addLesson(l);
				}
			}
		}

		// Lessons may not be found, this is okay
	}

	public static String fetchLongCellString(Element e) {

		Elements links = e.getElementsByTag("a");
		for (Element link : links) {

			if (link.hasAttr("txttitle")) {
				return link.attr("txttitle");
			}
		}
		return e.text();
	}

	public static boolean containsPrintableChars (String str) {

		if (str == null || str.length() <= 0)
			return false;

		String s = str.replaceAll("&nbsp;", " ");
		Matcher matcher = whitespaces_only.matcher(s);

		if (matcher.find())
			return false;
		
		return true;
	}
	
	public static String fetchLongCellStringNoWhitespaces(Element e) {

		String s = fetchLongCellString(e).replaceAll("&nbsp;", " ");
		Matcher matcher = whitespaces_only.matcher(s);

		if (matcher.find())
			return null;

		return s;
	}

	public static GradeSemester getActiveGradeSemester(Document doc, Schedule sch)
			throws ParseException {
		
		boolean found = false;
		GradeSemester selG = null;
		
		Elements semesterSelectors = doc.getElementsByAttributeValue("id",
				"ctl00_body_drdTerms");
		for (Element semesterSelector : semesterSelectors) {

			Elements semesters = semesterSelector.getAllElements();
			for (Element semester : semesters) {
				if (semester.tagName().equals("option")) {

					String value = semester.text();
					GradeSemester sem;
					found = true;

					try {

						sem = sch.getSemester(semester.attr("value"));
					} catch (NullPointerException e) {
						
						String sBegin = value.substring(12, value.indexOf("-") - 1);
						String sEnd = value.substring(value.indexOf("-") + 2, value.length() - 2);
						SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
						sem = new GradeSemester ();

						sem.setStart(fmt.parse(sBegin));
						sem.setStart(fmt.parse(sEnd));
						sem.setFormText(semester.text());
						sem.setFormId(semester.attr("value"));

						sch.addSemester(sem);
					}

					if (semester.hasAttr("selected")
							&& semester.attr("selected").equals("selected")) {

						sem.setLoaded();			
						selG = sem;
					}
				}
			}
		}

		if (!found)
			throw new ParseException("Semesters not found", 0);
		
		return selG;
	}

	public static void getGrades(Document doc, Schedule sch, GradeSemester s)
			throws ParseException {
		
		Elements tableCells = doc.getElementsByAttributeValue("class",
				"table rating");
		for (Element tableCell : tableCells) {

			Elements trs = tableCell.getElementsByTag("tr");
			for (Element tr : trs) {

				if (tr.hasAttr("class")
						&& tr.attr("class").equals("table-header"))
					continue;
				
				GradeRec rec = new GradeRec();
				int thCount = 0;

				Elements ths = tr.getElementsByTag("th");
				for (Element th : ths) {

					if (th.hasAttr("class")
							&& th.attr("class").equals("table-header3")) {
						
						rec.setFormText(th.text());
						thCount = 2;

					} else if (th.hasAttr("class")
							&& th.attr("class").equals("cell-header2")) {
						
						switch (thCount) {
						case 2:
							if (containsPrintableChars(th.text()))
								rec.setAbsent(Integer.parseInt(th.text()));
							break;
						case 3:
							if (containsPrintableChars(th.text()))
								rec.setReleased(Integer.parseInt(th.text()));
							break;
						case 4:
							if (containsPrintableChars(th.text()))
								rec.setSick(Integer.parseInt(th.text()));
							break;
						case 5:
							if (containsPrintableChars(th.text()))
								rec.setAverage(Float.parseFloat(th.text().replace(',', '.')));
							break;
						case 6:
							
							Elements tds = th.getElementsByTag("td");
							for (Element td : tds) {
								if (containsPrintableChars(td.text()))
									rec.setTotal(Integer.parseInt(td.text()));
							}							
							break;
						}

						thCount++;
					}
				}
				
				Elements tds = tr.getElementsByTag("td");
				for (Element td : tds) {
					
					if (td.hasAttr("class")
							&& td.attr("class").equals(
									"grade-with-type")) {

						Elements spans = td.getElementsByTag("span");
						for (Element span : spans) {
							
							if (containsPrintableChars(span.text()) && containsPrintableChars(span.attr("title")))							
								rec.addMarcRec(rec.new MarkRec (span.text(), span.attr("title")));
						}
					}
				}
				
				rec.setStart(s.getStart());
				rec.setStop(s.getStop());
				
				if (containsPrintableChars(rec.getFormText()))
					sch.addGradeRec(rec);
			}
		}
	}
	
	protected static String fixDuplicateString(String newS, String prevS, int idx) {

		if (idx == 0)
			return newS;
		
		if (newS == null || newS.length() == 0)
			return prevS;
		
		if (prevS == null || prevS.length() == 0)
			return newS;
		
		if(idx == 1) {
			return "1) " + prevS + "; 2) " + newS;
		}
		
		return prevS + "; " + Integer.toString(idx) + ") " + newS; 
	}

	public static void getLessonsDetails(Document doc, Schedule s)
			throws ParseException {

		Elements tableCells = doc.getElementsByAttributeValue("class",
				"table diary");
		for (Element tableCell : tableCells) {

			int tdCount = 0;
			Date date = null;
			Lesson l, lPrev = null; // lPrev to handle duplicate lesson bug
			int sameLesson = 0;      // Also to handle duplicate lesson bug

			Elements trs = tableCell.getElementsByTag("tr");
			for (Element tr : trs) {

				if (tr.hasAttr("class")
						&& tr.attr("class").equals("table-header"))
					continue;

				l = null;
				sameLesson = 0; // assume no bug here
				Elements tds = tr.getElementsByTag("td");

				for (Element td : tds) {

					if (td.hasAttr("class") && td.attr("class").equals("date")) {

						Elements divs = td.getElementsByTag("div");
						for (Element div : divs) {

							date = new SimpleDateFormat("dd.MM.yyyy",
									Locale.ENGLISH).parse(div.text());
						}

						tdCount = 1;

					} else if (td.hasAttr("class")
							&& td.attr("class").equals("diary-mark")) {

						String marks = fetchLongCellStringNoWhitespaces(td);
						if (l != null && marks != null) {
							
							if (sameLesson > 0 && lPrev != null) {
								
								l.setMarks(fixDuplicateString(marks, lPrev.getMarks(), sameLesson));
							} else
								l.setMarks(marks);
						}
						tdCount++;

					} else if (td.hasAttr("class")
							&& td.attr("class").equals("diary-comment")) {

						String comment = fetchLongCellStringNoWhitespaces(td);
						if (l != null && comment != null) {
							
							if (sameLesson > 0 && lPrev != null) {
								
								l.setComment(fixDuplicateString(comment, lPrev.getComment(), sameLesson));
							} else
								l.setComment(comment);							
						}
						
						tdCount++;

					} else if (tdCount == 2) {

						String theme = fetchLongCellStringNoWhitespaces(td);
						if (l != null && theme != null) {
							
							if (sameLesson > 0 && lPrev != null) {
								
								l.setTheme(fixDuplicateString(theme, lPrev.getTheme(), sameLesson));
							} else
								l.setTheme(theme);
						}
						tdCount++;

					} else if (tdCount == 3) {

						String homework = fetchLongCellStringNoWhitespaces(td);
						if (l != null && homework != null) {
							
							if (sameLesson > 0 && lPrev != null) {
								
								l.setHomework(fixDuplicateString(homework, lPrev.getHomework(), sameLesson));
							} else
								l.setHomework(homework);
						}
						tdCount++;
					// TODO: use subjects_name pattern
					} else if (td.text().matches("^[0-9]{1}\\." + whitespace_charclass + "{1}.*")) {

						tdCount = 2;

						try {
							l = s.getLessonByNumber(
											date,
											Integer.parseInt(td.text()
													.substring(0, 1)));
							
							if (lPrev != null
									&& l.getStart().equals(lPrev.getStart())
									&& l.getNumber() == lPrev.getNumber()
									&& l.getFormId().equals(lPrev.getFormId())) {
								
								// We hit the same lesson bug
								sameLesson++;
							}
						} catch (NullPointerException e) {

							e.printStackTrace();
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}						

					} else {
						tdCount++;
					}
				}
				
				if (l != null) {
					lPrev = l;
					l.update();
				}
			}
		}
	}

	public static String getVIEWSTATE(Document doc) {

		Element viewstate = doc.getElementById("__VIEWSTATE");
		if (viewstate == null)
			return null;

		return viewstate.val();
	}
}