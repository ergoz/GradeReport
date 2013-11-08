package com.shinymetal.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.shinymetal.objects.GradeRec;
import com.shinymetal.objects.GradeSemester;
import com.shinymetal.objects.Lesson;
import com.shinymetal.objects.Pupil;
import com.shinymetal.objects.Schedule;
import com.shinymetal.objects.User;
import com.shinymetal.objects.Week;

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
	/* A \s that actually works for Java’s native character set: Unicode */
	public final static String whitespace_charclass = "[" + whitespace_chars + "]";
	/* A \S that actually works for Java’s native character set: Unicode */
	public final static String not_whitespace_charclass = "[^" + whitespace_chars
			+ "]";
	
	public final static Pattern whitespaces_only = Pattern.compile("^" + whitespace_charclass +"+$");
//	public final static Pattern subjects_name = Pattern.compile("^[0-9]{1}\\." + whitespace_charclass + "{1}.*");

	public static void fetchUserName(Document doc, User u)
			throws ParseException {

		Elements userNames = doc.getElementsByClass("user-name");
		for (Element userName : userNames) {

			u.setName(userName.text());
			return;
		}

		throw new ParseException("Username not found", 0);
	}

	public static void fetchPupils(Document doc, User u) throws ParseException {

		boolean found = false;

		Elements pupilSelectors = doc.getElementsByAttributeValue("id",
				"ctl00_topMenu_pupil_drdPupils");
		for (Element pupilSelector : pupilSelectors) {

			Elements pupils = pupilSelector.getAllElements();
			for (Element pupil : pupils) {
				if (pupil.tagName().equals("option")) {

					String value = pupil.attr("value");
					Pupil p;

					found = true;
					try {
						p = u.getPupilByFormId(value);

					} catch (NullPointerException e) {

						p = new Pupil(pupil.text(), value);
						u.addPupil(p);
					}

					if (pupil.hasAttr("selected")
							&& pupil.attr("selected").equals("selected")) {

						u.setCurrentPupilId(value);
					}
				}
			}
		}

		if (!found)
			throw new ParseException("Pupils not found", 0);
	}

	public static void fetchYears(Document doc, User u) throws ParseException {

		boolean found = false;

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

						schedule = u.getCurrentPupil().getScheduleByFormId(
								value);
					} catch (NullPointerException e) {

						schedule = new Schedule(value, year.text());

						Pupil p = u.getCurrentPupil();
						p.addSchedule(schedule);
					}

					if (year.hasAttr("selected")
							&& year.attr("selected").equals("selected")) {

						u.getCurrentPupil().setCurrentScheduleId(value);
					}
				}
			}
		}

		if (!found)
			throw new ParseException("Years not found", 0);
	}

	public static void fetchWeeks(Document doc, User u) throws ParseException {

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

					Schedule schedule = u.getCurrentPupil()
							.getCurrentSchedule();

					try {

						w = schedule.getWeek(week.attr("value"));
					} catch (NullPointerException e) {

						w = new Week();
						
						String wBegin = value.substring(0, value.indexOf("-") - 1);
						String wMonth = wBegin.substring(wBegin.indexOf(".") + 1, wBegin.length());

						String year;
						if (Integer.parseInt(wMonth) > 7) {
							year = schedule.getFormText().substring(0, schedule.getFormText().indexOf("-") - 1);
						} else {
							year = schedule.getFormText().substring(schedule.getFormText().indexOf("-") + 2,
									schedule.getFormText().length());
						}

						w.setStart(new SimpleDateFormat("yyyy dd.MM", Locale.ENGLISH).parse(year
								+ " " + wBegin));

						w.setFormText(week.text());
						w.setFormId(week.attr("value"));

						schedule.addWeek(w);
					}

					if (week.hasAttr("selected")
							&& week.attr("selected").equals("selected")) {

						schedule.getWeek(week.attr("value")).setLoaded();
					}
				}
			}
		}

		if (!found)
			throw new ParseException("Weeks not found", 0);
	}

	public static void fetchLessons(Document doc, User u) throws ParseException {

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

				Schedule schedule = u.getCurrentPupil().getCurrentSchedule();
				SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);

				Lesson l;
				
				String timeB = time.substring(0, time.indexOf("-") - 1);
				Date start = format.parse(date + " " + timeB);

				if ((l = schedule.getLesson(start)) == null) {

					l = new Lesson();
					
					String timeE = time.substring(time.indexOf("-") + 2, time.length());
					Date stop = format.parse(date + " " + timeE);

					l.setStart(start);
					l.setStop(stop);
					l.setFormId(formId);
					l.setFormText(subjectName);
					l.setTeacher(teacherName);
					l.setNumber(Integer.parseInt(number));

					schedule.addLesson(l);
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

	public static void fetchGradeSemesters(Document doc, User u)
			throws ParseException {
		
		boolean found = false;
		
		Elements semesterSelectors = doc.getElementsByAttributeValue("id",
				"ctl00_body_drdTerms");
		for (Element semesterSelector : semesterSelectors) {

			Elements semesters = semesterSelector.getAllElements();
			for (Element semester : semesters) {
				if (semester.tagName().equals("option")) {

					String value = semester.text();
					GradeSemester sem;
					found = true;

					Schedule schedule = u.getCurrentPupil()
							.getCurrentSchedule();

					try {

						sem = schedule.getSemester(semester.attr("value"));
					} catch (NullPointerException e) {
						
						String sBegin = value.substring(12, value.indexOf("-") - 1);
						String sEnd = value.substring(value.indexOf("-") + 2, value.length() - 2);
						SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
						sem = new GradeSemester ();

						sem.setStart(fmt.parse(sBegin));
						sem.setStart(fmt.parse(sEnd));
						sem.setFormText(semester.text());
						sem.setFormId(semester.attr("value"));

						schedule.addSemester(sem);
					}

					if (semester.hasAttr("selected")
							&& semester.attr("selected").equals("selected")) {

						schedule.getSemester(semester.attr("value")).setLoaded();
						schedule.setCurrentSemesterId(semester.attr("value"));
					}
				}
			}
		}

		if (!found)
			throw new ParseException("Semesters not found", 0);
	}

	public static void fetchGrades(Document doc, User u)
			throws ParseException {
		
		Schedule sch = u.getCurrentPupil().getCurrentSchedule();
		GradeSemester s = sch.getCurrentSemester();

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

	public static void fetchLessonsDetails(Document doc, User u)
			throws ParseException {

		Elements tableCells = doc.getElementsByAttributeValue("class",
				"table diary");
		for (Element tableCell : tableCells) {

			int tdCount = 0;
			Date date = null;
			Lesson l;

			Elements trs = tableCell.getElementsByTag("tr");
			for (Element tr : trs) {

				if (tr.hasAttr("class")
						&& tr.attr("class").equals("table-header"))
					continue;

				l = null;
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
							l.setMarks(marks);
						}
						tdCount++;

					} else if (td.hasAttr("class")
							&& td.attr("class").equals("diary-comment")) {

						String comment = fetchLongCellStringNoWhitespaces(td);
						if (l != null && comment != null) {
							l.setComment(comment);							
						}
						
						tdCount++;

					} else if (tdCount == 2) {

						String theme = fetchLongCellStringNoWhitespaces(td);
						if (l != null && theme != null) {
							l.setTheme(theme);
						}
						tdCount++;

					} else if (tdCount == 3) {

						String homework = fetchLongCellStringNoWhitespaces(td);
						if (l != null && homework != null) {
							l.setHomework(homework);
						}
						tdCount++;
					// TODO: use subjects_name pattern
					} else if (td.text().matches("^[0-9]{1}\\." + whitespace_charclass + "{1}.*")) {

						tdCount = 2;

						try {
							l = u.getCurrentPupil()
									.getCurrentSchedule()
									.getLessonByNumber(
											date,
											Integer.parseInt(td.text()
													.substring(0, 1)));
						} catch (NullPointerException e) {

							e.printStackTrace();
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}						

					} else {
						tdCount++;
					}
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