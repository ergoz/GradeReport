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
						w.setStartStop(value, schedule.getSchoolYear());
						w.setFormValue(week.attr("value"));

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

				Elements subjects = lessonCellDetail
						.getElementsByAttributeValue("class", "lesson-subject");
				for (Element subject : subjects) {
					subjectName = subject.text();
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
				Lesson l;

				if ((l = schedule.getLesson(Lesson.getStart(date, time))) == null) {

					l = new Lesson(number, subjectName, teacherName);

					l.setTimeframe(date, time);
					schedule.addLesson(l);
				}
			}
		}

		// Lessons may not be found, this is okay
		// if (!found)
		// throw new ParseException("Lessons not found", 0);
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

	public static String fetchLongCellStringNoWhitespaces(Element e) {

		String s = fetchLongCellString(e).replaceAll("&nbsp;", " ");
		Matcher matcher = whitespaces_only.matcher(s);

		if (matcher.find())
			return null;

		return s;
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

							// System.out.println("Date: " + div.text());
							date = new SimpleDateFormat("dd.MM.yyyy",
									Locale.ENGLISH).parse(div.text());
						}

						tdCount = 1;

					} else if (td.hasAttr("class")
							&& td.attr("class").equals("diary-mark")) {

						String marks = fetchLongCellStringNoWhitespaces(td);
						// System.out.println("Mark: " + marks);
						if (l != null && marks != null) {
							l.setMarks(marks);
						}
						tdCount++;

					} else if (td.hasAttr("class")
							&& td.attr("class").equals("diary-comment")) {

						String comment = fetchLongCellStringNoWhitespaces(td);
						// System.out.println("Comment: " + comment);
						if (l != null && comment != null) {
							l.setComment(comment);							
						}
						
//						if (l != null)
//							System.out.println("Parsed l: " + l.toString() );
						
						tdCount++;

					} else if (tdCount == 2) {

						String theme = fetchLongCellStringNoWhitespaces(td);
						// System.out.println("Theme: " + theme);
						if (l != null && theme != null) {
							l.setTheme(theme);
						}
						tdCount++;

					} else if (tdCount == 3) {

						String homework = fetchLongCellStringNoWhitespaces(td);
						// System.out.println("Homework: " + homework);
						if (l != null && homework != null) {
							l.setHomework(homework);
						}
						tdCount++;
					// TODO: use subjects_name pattern
					} else if (td.text().matches("^[0-9]{1}\\." + whitespace_charclass + "{1}.*")) {


						System.out.println("Name: " + td.text() + ", d: "
								+ date);
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
							// e.getCause();
							e.printStackTrace();
						}						

					} else {

						// System.out.println("Text: " +
						// fetchLongCellString(td));
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