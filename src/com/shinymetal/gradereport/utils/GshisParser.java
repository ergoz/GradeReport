package com.shinymetal.gradereport.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.auth.InvalidCredentialsException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import com.shinymetal.gradereport.BuildConfig;
import com.shinymetal.gradereport.objects.GradeRec;
import com.shinymetal.gradereport.objects.GradeSemester;
import com.shinymetal.gradereport.objects.Lesson;
import com.shinymetal.gradereport.objects.MarkRec;
import com.shinymetal.gradereport.objects.Pupil;
import com.shinymetal.gradereport.objects.Schedule;
import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.objects.Week;

public class GshisParser extends BasicParser {

	protected final static String SITE_NAME = "http://schoolinfo.educom.ru";
	protected final static String LOGIN_STEP_1 = "/Login.aspx?ReturnUrl=%2fdefault.aspx";
	protected final static String LOGIN_STEP_2 = "/default.aspx?action=login";
	protected final static String LESSONS_PAGE = "/Pupil/Lessons.aspx";
	protected final static String DIARY_PAGE = "/Pupil/Diary.aspx";
	protected final static String GRADES_PAGE = "/Pupil/Grades.aspx";
	
	protected final static Pattern SUBJECT_NAME = Pattern.compile("^[0-9]{1}\\."
			+ WHITESPACE_CHARCLASS + "{1}.*");
	
	protected String mCookieARRAffinity;
	protected String mCookieASPXAUTH;
	protected String mCookieASPNET_SessionId;

	protected String mAuthVIEWSTATE;
	protected String mLessonsVIEWSTATE;
	protected String mDiaryVIEWSTATE;
	protected String mGradesVIEWSTATE;
	
	protected String mCurrentPupilName;
	protected boolean mIsLoggedIn;

//	private static ArrayList<MarkRec> mNewMarks = new ArrayList<MarkRec> ();
//	
//	public final static ArrayList<MarkRec> getNewMarks() {
//		
//		return mNewMarks;
//	}
	
	public GshisParser() {
		
		mIsLoggedIn = false;
	}
	
	public void reset() {

		mIsLoggedIn = false; 

		mAuthVIEWSTATE = null;
		mLessonsVIEWSTATE = null;
		mDiaryVIEWSTATE = null;
		mGradesVIEWSTATE = null;
	}

	
	public Pupil getSelectedPupil(String login, Document doc) throws ParseException {

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
					if ((p = Pupil.getByFormId(login, value)) == null) {

						p = new Pupil(pupil.text(), value);
						long rowId = p.insert(login);
						
						if (BuildConfig.DEBUG)
							Log.d("GshisParser", TS.get()
									+ " Pupil.insert() = " + rowId);
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

	public Schedule getSelectedSchedule(Document doc, Pupil selPupil) throws ParseException {

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
					
					if ((schedule = selPupil.getScheduleByFormId(value)) == null) {

						final SimpleDateFormat f = new SimpleDateFormat(
								"yyyy dd.MM", Locale.ENGLISH);
						f.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
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

	public Week getSelectedWeek(Document doc, Schedule s) throws ParseException {

		boolean found = false;
		Week selectedW = null;
		
		SimpleDateFormat f = new SimpleDateFormat("yyyy dd.MM", Locale.ENGLISH);
		f.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
		
		Elements weekSelectors = doc.getElementsByAttributeValue("id",
				"ctl00_body_week_drdWeeks");
		for (Element weekSelector : weekSelectors) {

			Elements weeks = weekSelector.getAllElements();
			for (Element week : weeks) {
				if (week.tagName().equals("option")) {

					String value = week.text();
					Week w;
					found = true;
					
					if ((w = s.getWeek(week.attr("value"))) == null ) {

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

						w.setStart(f.parse(year	+ " " + wBegin));
						w.setFormText(week.text());
						w.setFormId(week.attr("value"));

						s.addWeek(w);
					}

					if (week.hasAttr("selected")
							&& week.attr("selected").equals("selected")) {
						
						selectedW = w;
						long u = w.setLoaded().update();
						
						if (BuildConfig.DEBUG)
							Log.d("GshisParser", TS.get()
									+ " Week.update() = " + u);
					}
				}
			}
		}

		if (!found)
			throw new ParseException("Weeks not found", 0);
		
		return selectedW;
	}

	protected void getLessons(Document doc, Schedule s) throws ParseException {

		final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);
		format.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
		
		Elements lessonCells = doc.getElementsByAttribute("number");
		
		for (Element lessonCell : lessonCells) {

			Lesson l, lPrev = null;  // lPrev to handle duplicate lesson
			int sameLesson = 0;      // Also to handle duplicate lesson
			
			int number = Integer.parseInt(lessonCell.attr("number"));
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
				int index = 0;
				sameLesson = 0;
				
				for (Element subject : lessonCellDetail
						.getElementsByAttributeValue("class", "lesson-subject")) {

					if (subject == null || subject.text() == null
							|| subject.text().length() <= 0) {
						// No lesson scheduled
						continue;
					}

					Date start = format.parse(date + " " + time.substring(0, time.indexOf("-") - 1));
					if ((l = s.getLessonByNumber(start, number)) == null) {
						
						if (BuildConfig.DEBUG)
							Log.d("GshisParser", TS.get()
									+ " getLessons() not found in db, will insert");

						l = new Lesson();
						sameLesson = 0;

						l.setStart(start);
						l.setStop(format.parse(date
								+ " "
								+ time.substring(time.indexOf("-") + 2,
										time.length())));
						l.setFormId(subject.attr("id"));
						l.setFormText(subject.text());
						l.setTeacher(lessonCellDetail
								.getElementsByAttributeValue("class",
										"lesson-teacher").get(sameLesson).text());
						l.setNumber(number);

						s.addLesson(l);

					} else {
						
						if (BuildConfig.DEBUG)
							Log.d("GshisParser", TS.get()
									+ " getLessons() found in db, will update");
						
						l.setFormId(subject.attr("id"));
						
						if (lPrev != null
								&& lPrev.getStart().equals(start)
								&& lPrev.getNumber() == number) {
							
							if (BuildConfig.DEBUG)
								Log.d("GshisParser", TS.get()
										+ " getLessons() dup = " + subject.text() + " index = " + index + " sameLesson = " + sameLesson);

							
							sameLesson++;
							
							if (!lPrev.getFormText().equals(subject.text()))								
								l.setFormText(fixDuplicateString(
										subject.text(), lPrev.getFormText(), sameLesson));
							
							String teacher = lessonCellDetail
									.getElementsByAttributeValue("class",
											"lesson-teacher").get(index).text();
							
							if (!lPrev.getTeacher().equals(teacher))								
								l.setTeacher(fixDuplicateString(
										teacher, lPrev.getTeacher(), sameLesson));

						} else {

							l.setNumber(number);
							l.setFormText(subject.text());
							l.setTeacher(lessonCellDetail
									.getElementsByAttributeValue("class",
											"lesson-teacher").get(index).text());
						}
						
						l.update();
					}
					
					lPrev = l;
					index++;
				}
			}
		}
	}

	public void getLessonsDetails(Document doc, Schedule s)
			throws ParseException {

		final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy",
				Locale.ENGLISH);
		fmt.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
		
		Elements tableCells = doc.getElementsByAttributeValue("class",
				"table diary");
		for (Element tableCell : tableCells) {

			int tdCount = 0;
			Date date = null;
			Lesson l, lPrev = null; // lPrev to handle duplicate lesson
			int sameLesson = 0;      // Also to handle duplicate lesson

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

						date = fmt.parse(td.getElementsByTag("div").first().text());
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

					} else if (SUBJECT_NAME.matcher(td.text()).find()) {

						tdCount = 2;
						int number = Integer.parseInt(td.text().substring(0, 1)); 
						l = s.getLessonByNumber(date, number);

						if (lPrev != null && l != null
								&& l.getStart().equals(lPrev.getStart())
								&& l.getNumber() == lPrev.getNumber()) {

							// We hit the same lesson bug
							sameLesson++;
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

	public String fetchLongCellString(Element e) {

		for (Element link : e.getElementsByTag("a")) {

			if (link.hasAttr("txttitle")) {
				return link.attr("txttitle");
			}
		}
		return e.text();
	}
	
	public String fetchLongCellStringNoWhitespaces(Element e) {

		String s = fetchLongCellString(e).replaceAll("&nbsp;", " ");
		Matcher matcher = WHITESPACES_ONLY.matcher(s);

		if (matcher.find())
			return null;

		return s;
	}

	public GradeSemester getActiveGradeSemester(Document doc, Schedule sch)
			throws ParseException {
		
		boolean found = false;
		GradeSemester selG = null;
		
		SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
		fmt.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
		
		Elements semesterSelectors = doc.getElementsByAttributeValue("id",
				"ctl00_body_drdTerms");
		for (Element semesterSelector : semesterSelectors) {

			Elements semesters = semesterSelector.getAllElements();
			for (Element semester : semesters) {
				if (semester.tagName().equals("option")) {

					String value = semester.text();
					GradeSemester sem;
					found = true;

					if ((sem = sch.getSemester(semester.attr("value"))) == null ) {
						
						sem = new GradeSemester ();

						sem.setStart(fmt.parse(value.substring(12, value.indexOf("-") - 1)));
						sem.setStop(fmt.parse(value.substring(value.indexOf("-") + 2, value.length() - 2)));
						sem.setFormText(semester.text());
						sem.setFormId(semester.attr("value"));

						sch.addSemester(sem);
					}

					if (semester.hasAttr("selected")
							&& semester.attr("selected").equals("selected")) {

						long u = sem.setLoaded().update();			
						selG = sem;
						
						if (BuildConfig.DEBUG)
							Log.d("GshisParser", TS.get()
									+ " Semester.update() = " + u);
					}
				}
			}
		}

		if (!found)
			throw new ParseException("Semesters not found", 0);
		
		return selG;
	}

	protected void getGrades(Document doc, Schedule sch, GradeSemester s)
			throws ParseException {
		
//		mNewMarks = new ArrayList<MarkRec> ();
		
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
						}

						thCount++;
					}
				}

				Element total = tr.getElementsByTag("td").last();
				if (containsPrintableChars(total.text()) && total.text().matches("[-+]?\\d*\\.?\\d+")) {

					rec.setTotal(Integer.parseInt(total.text()));
				}

				rec.setStart(s.getStart());
				rec.setStop(s.getStop());
				
				if (containsPrintableChars(rec.getFormText())) {

					GradeRec exR = sch.getGradeRecByDateText (rec.getStart(), rec.getFormText());
					if (exR != null) {
						
//						if (BuildConfig.DEBUG)
//							Log.d("GshisParser",
//									TS.get()
//											+ " before update GradeRec, start = "
//											+ exR.getStart() + " stop = "
//											+ exR.getStop() + " text = "
//											+ exR.getFormText());
						
						exR.setAbsent(rec.getAbsent());
						exR.setAverage(rec.getAverage());
						exR.setReleased(rec.getReleased());
						exR.setSick(rec.getSick());
						exR.setTotal(rec.getTotal());
						
						// make sure we have only fresh marks
						exR.deleteMarks();
						
						@SuppressWarnings("unused")
						long u = exR.update();						
						rec = exR;
						
//						if (BuildConfig.DEBUG)
//							Log.d("GshisParser", TS.get()
//									+ " GradeRec.update() = " + u);
					}
					else
					{
//						if (BuildConfig.DEBUG)
//							Log.d("GshisParser", TS.get()
//									+ " insert GradeRec = " + rec);
						
						sch.addGradeRec(rec);
					}

					for (Element td : tr.getElementsByTag("td")) {

						if (td.hasAttr("class")
								&& td.attr("class").equals("grade-with-type")) {


							Element span = td.getElementsByTag("span").first();

							if (containsPrintableChars(span.text())
									&& containsPrintableChars(span
											.attr("title"))) {

								MarkRec mr = rec.getMarkRecByComment(span.attr("title"));
								if (mr != null) {

									mr.setMarks(span.text());
									
									@SuppressWarnings("unused")
									long u = mr.update();

//									if (BuildConfig.DEBUG)
//										Log.d("GshisParser", TS.get() + " MarkRec.update() = " + u
//												+ " rec = " + rec);
								} else {
									

									mr = new MarkRec(span.text(), span.attr("title"));
									
//									mNewMarks.add(mr);
									rec.addMarcRec(mr);

//									if (BuildConfig.DEBUG)
//										Log.d("GshisParser", TS.get()
//												+ " insert MarkRec Comment = " + mr.getComment() + " Marks = "
//												+ mr.getMarks());
								}
							}
						}
					}
				}
			}
		}
	}
	
	protected String fixDuplicateString(String newS, String prevS, int idx) {

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

	public String getVIEWSTATE(Document doc) {

		Element viewstate = doc.getElementById("__VIEWSTATE");
		if (viewstate == null)
			return null;

		return viewstate.val();
	}
	
	protected HttpURLConnection getHttpURLConnection(String url)
			throws MalformedURLException, IOException {

		return (HttpURLConnection) new URL(url).openConnection();
	}
	
	protected String encodePOSTVar(String name, String value) throws UnsupportedEncodingException	{
		
		return URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8") + "&"; 
	}

	protected boolean loginGetAuthVIEWSTATE() throws MalformedURLException, IOException {

		HttpURLConnection uc = getHttpURLConnection(SITE_NAME + LOGIN_STEP_1);
		uc.connect();

		String affinity = getCookieByName(uc, "ARRAffinity");
		if (affinity != null)
			mCookieARRAffinity = affinity;

		String line = null;
		StringBuffer tmp = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				uc.getInputStream(), "UTF-8"));
		while ((line = in.readLine()) != null) {
			tmp.append(line + "\n");
		}

		Document doc = Jsoup.parse(String.valueOf(tmp));
		mAuthVIEWSTATE = getVIEWSTATE(doc);
		return mAuthVIEWSTATE != null;
	}

	protected boolean loginGetASPXAUTH() throws IOException {

		HttpURLConnection uc = getHttpURLConnection(SITE_NAME + LOGIN_STEP_1);
		uc.setInstanceFollowRedirects(false);

		String urlParameters = "";

		urlParameters += encodePOSTVar("__EVENTTARGET", "ctl00$btnLogin");
		urlParameters += encodePOSTVar("__VIEWSTATE", mAuthVIEWSTATE);
		urlParameters += encodePOSTVar("ctl00$txtLogin", mLogin);
		urlParameters += encodePOSTVar("ctl00$txtPassword", mPassword);

		uc.setRequestMethod("POST");
		uc.setRequestProperty("Cookie", "ARRAffinity=" + mCookieARRAffinity);
		uc.setRequestProperty("Origin", SITE_NAME);
		uc.setRequestProperty("Referer", SITE_NAME + LOGIN_STEP_1);
		uc.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded; charset=utf-8");

		uc.setRequestProperty("Content-Length",
				"" + Integer.toString(urlParameters.getBytes().length));

		uc.setUseCaches(false);
		uc.setDoInput(true);
		uc.setDoOutput(true);

		// Send request
		DataOutputStream wr = new DataOutputStream(uc.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		String aspxauth = getCookieByName(uc, ".ASPXAUTH");
		if (aspxauth != null) {

			mCookieASPXAUTH = aspxauth;
			return true;
		}

		return false;
	}

	protected boolean loginGetSessionId() throws MalformedURLException, IOException {

		HttpURLConnection uc = getHttpURLConnection(SITE_NAME + LOGIN_STEP_2);
		uc.setInstanceFollowRedirects(false);
		uc.setRequestProperty("Cookie", "ARRAffinity=" + mCookieARRAffinity
				+ "; .ASPXAUTH=" + mCookieASPXAUTH);
		uc.setRequestProperty("Origin", SITE_NAME);
		uc.setRequestProperty("Referer", SITE_NAME + LOGIN_STEP_1);

		uc.connect();

		String sessionId = getCookieByName(uc, "ASP.NET_SessionId");
		if (sessionId != null) {

			mCookieASPNET_SessionId = sessionId;
			return true;
		}

		return false;
	}

	protected String getPageByURL(String pageUrl) throws MalformedURLException, IOException {

		if (mCookieASPXAUTH == null || mCookieASPXAUTH.length() <= 0
				|| mCookieASPNET_SessionId.length() <= 0) {
			return null;
		}

		HttpURLConnection uc = getHttpURLConnection(SITE_NAME + pageUrl);

		uc.setRequestProperty("Cookie", "ARRAffinity=" + mCookieARRAffinity
				+ "; .ASPXAUTH=" + mCookieASPXAUTH + "; ASP.NET_SessionId="
				+ mCookieASPNET_SessionId);
		uc.setRequestProperty("Origin", SITE_NAME);
		uc.setRequestProperty("Referer", SITE_NAME + LOGIN_STEP_1);

		uc.connect();

		String line = null;
		StringBuffer tmp = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				uc.getInputStream(), "UTF-8"));
		while ((line = in.readLine()) != null) {
			tmp.append(line + "\n");
		}

		return tmp.toString();
	}
	
	protected String getLessonsPage(Pupil p, Schedule s, Week w)
			throws MalformedURLException, IOException {
		
		if (mCookieASPXAUTH.length() <= 0
				|| mCookieASPNET_SessionId.length() <= 0) {
			return null;
		}

		HttpURLConnection uc = getHttpURLConnection(SITE_NAME + LESSONS_PAGE);

		String urlParameters = "";

		/*
		 * Do NOT add ctl00$sm, __EVENTTARGET, __EVENTARGUMENT, __LASTFOCUS,
		 * __ASYNCPOST, this will break everything for unknown reason!
		 */
		urlParameters += encodePOSTVar("__VIEWSTATE", mLessonsVIEWSTATE);
		urlParameters += encodePOSTVar("ctl00$learnYear$drdLearnYears",
				s.getFormId());
		urlParameters += encodePOSTVar("ctl00$topMenu$pupil$drdPupils",
				p.getFormId());
		urlParameters += encodePOSTVar("ctl00$topMenu$tbUserId", p.getFormId());
		urlParameters += encodePOSTVar(
				"ctl00$leftMenu$accordion_AccordionExtender_ClientState", "");
		urlParameters += encodePOSTVar("ctl00$body$week$drdWeeks",
				w.getFormId());

		uc.setRequestMethod("POST");
		uc.setRequestProperty("Cookie", "ARRAffinity=" + mCookieARRAffinity
				+ "; ASP.NET_SessionId=" + mCookieASPNET_SessionId
				+ "; .ASPXAUTH=" + mCookieASPXAUTH);

		uc.setRequestProperty("Origin", SITE_NAME);
		uc.setRequestProperty("Referer", SITE_NAME + LESSONS_PAGE);
		uc.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded; charset=utf-8");

		uc.setRequestProperty("Content-Length",
				"" + Integer.toString(urlParameters.getBytes().length));

		uc.setUseCaches(false);
		uc.setDoInput(true);
		uc.setDoOutput(true);

		// Send request
		DataOutputStream wr = new DataOutputStream(uc.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		String line = null;
		StringBuffer tmp = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				uc.getInputStream(), "UTF-8"));
		while ((line = in.readLine()) != null) {
			tmp.append(line + "\n");
		}

		return String.valueOf(tmp);
	}

	protected String getLessonsDetailsPage(Pupil p, Schedule s, Week w)
			throws MalformedURLException, IOException {
		
		if (mCookieASPXAUTH.length() <= 0
				|| mCookieASPNET_SessionId.length() <= 0) {
			return null;
		}

		HttpURLConnection uc = getHttpURLConnection(SITE_NAME + DIARY_PAGE);

		String urlParameters = "";

		/*
		 * Do NOT add ctl00$sm, __EVENTTARGET, __EVENTARGUMENT, __LASTFOCUS,
		 * __ASYNCPOST, this will break everything for unknown reason!
		 */
		urlParameters += encodePOSTVar("__VIEWSTATE", mDiaryVIEWSTATE);
		urlParameters += encodePOSTVar("ctl00$learnYear$drdLearnYears",
				s.getFormId());
		urlParameters += encodePOSTVar("ctl00$topMenu$pupil$drdPupils",
				p.getFormId());
		urlParameters += encodePOSTVar("ctl00$topMenu$tbUserId", p.getFormId());
		urlParameters += encodePOSTVar(
				"ctl00$leftMenu$accordion_AccordionExtender_ClientState", "");
		urlParameters += encodePOSTVar("ctl00$body$period$drdPeriodType", "1");
		urlParameters += encodePOSTVar("ctl00$body$period$week$drdWeeks",
				w.getFormId());

		uc.setRequestMethod("POST");
		uc.setRequestProperty("Cookie", "ARRAffinity=" + mCookieARRAffinity
				+ "; ASP.NET_SessionId=" + mCookieASPNET_SessionId
				+ "; .ASPXAUTH=" + mCookieASPXAUTH);

		uc.setRequestProperty("Origin", SITE_NAME);
		uc.setRequestProperty("Referer", SITE_NAME + DIARY_PAGE);
		uc.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded; charset=utf-8");

		uc.setRequestProperty("Content-Length",
				"" + Integer.toString(urlParameters.getBytes().length));

		uc.setUseCaches(false);
		uc.setDoInput(true);
		uc.setDoOutput(true);

		// Send request
		DataOutputStream wr = new DataOutputStream(uc.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		String line = null;
		StringBuffer tmp = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				uc.getInputStream(), "UTF-8"));
		while ((line = in.readLine()) != null) {
			tmp.append(line + "\n");
		}

		return String.valueOf(tmp);
	}

	protected String getGradesPage(Pupil p, Schedule s, GradeSemester sem)
			throws MalformedURLException, IOException {
		
		if (mCookieASPXAUTH.length() <= 0
				|| mCookieASPNET_SessionId.length() <= 0) {
			return null;
		}

		HttpURLConnection uc = getHttpURLConnection(SITE_NAME + GRADES_PAGE);

		String urlParameters = "";

		/*
		 * Do NOT add ctl00$sm, __EVENTTARGET, __EVENTARGUMENT, __LASTFOCUS,
		 * __ASYNCPOST, this will break everything for unknown reason!
		 * 
		 */
		urlParameters += encodePOSTVar("__VIEWSTATE", mGradesVIEWSTATE);
		urlParameters += encodePOSTVar("ctl00$learnYear$drdLearnYears",
				s.getFormId());
		urlParameters += encodePOSTVar("ctl00$topMenu$pupil$drdPupils",
				p.getFormId());
		urlParameters += encodePOSTVar("ctl00$topMenu$tbUserId", p.getFormId());
		urlParameters += encodePOSTVar(
				"ctl00$leftMenu$accordion_AccordionExtender_ClientState", "");
		urlParameters += encodePOSTVar("ctl00$body$drdTerms",
				sem.getFormId());

		uc.setRequestMethod("POST");
		uc.setRequestProperty("Cookie", "ARRAffinity=" + mCookieARRAffinity
				+ "; ASP.NET_SessionId=" + mCookieASPNET_SessionId
				+ "; .ASPXAUTH=" + mCookieASPXAUTH);

		uc.setRequestProperty("Origin", SITE_NAME);
		uc.setRequestProperty("Referer", SITE_NAME + DIARY_PAGE);
		uc.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded; charset=utf-8");

		uc.setRequestProperty("Content-Length",
				"" + Integer.toString(urlParameters.getBytes().length));

		uc.setUseCaches(false);
		uc.setDoInput(true);
		uc.setDoOutput(true);

		// Send request
		DataOutputStream wr = new DataOutputStream(uc.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		String line = null;
		StringBuffer tmp = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				uc.getInputStream(), "UTF-8"));
		while ((line = in.readLine()) != null) {
			tmp.append(line + "\n");
		}

		return String.valueOf(tmp);
	}
	
	public Week parseLessonsPage(String page) throws ParseException {

		Document doc = Jsoup.parse(page);

		Pupil p = getSelectedPupil(mLogin, doc);
		mCurrentPupilName = p.getFormText();
		
		Schedule s = getSelectedSchedule(doc, p);		
		Week w = getSelectedWeek(doc, s);
		getLessons(doc, s);
		
		mLessonsVIEWSTATE = getVIEWSTATE(doc);		
		if (mLessonsVIEWSTATE == null || mLessonsVIEWSTATE.length() <= 0)
			throw new IllegalStateException("LessonsVIEWSTATE is NULL");

		return w;
	}
	
	public void getLessons() throws ParseException, MalformedURLException, IOException {

		String page;
		
		if ((page = getPageByURL(LESSONS_PAGE)) == null)
			return;
		
		parseLessonsPage(page);
	}
	
	public void getLessons(Pupil p, Schedule s, Week w)
			throws ParseException, MalformedURLException, IOException {
	
		String page = getLessonsPage(p, s, w);
		
		if (page == null)
			return;

		parseLessonsPage(page);
	}
	
	public void parseLessonsDetailsPage(String page) throws ParseException {

		Document doc = Jsoup.parse(page);

		Pupil p = getSelectedPupil(mLogin, doc);
		mCurrentPupilName = p.getFormText();
		
		Schedule s = getSelectedSchedule(doc, p);
		getLessonsDetails(doc, s);
		
		mDiaryVIEWSTATE = getVIEWSTATE(doc);
		if (mDiaryVIEWSTATE == null || mDiaryVIEWSTATE.length() <= 0)
			throw new IllegalStateException("DiaryVIEWSTATE is NULL");
	}
	
	public void getLessonsDetails() throws ParseException, MalformedURLException, IOException {
		
		String page;
		
		if ((page = getPageByURL(DIARY_PAGE)) == null)
			return;

		parseLessonsDetailsPage(page);
	}
	
	public void getLessonsDetails(Pupil p, Schedule s, Week w)
			throws ParseException, MalformedURLException, IOException {
		
		String page = getLessonsDetailsPage(p, s, w);		
		if (page == null)
			return;
		
		parseLessonsDetailsPage(page);
	}

	public void parseGradesPage(String page) throws ParseException {

		Document doc = Jsoup.parse(page);

		Pupil p = getSelectedPupil(mLogin, doc);
		mCurrentPupilName = p.getFormText();
		
		Schedule s = getSelectedSchedule(doc, p);
		GradeSemester g = getActiveGradeSemester(doc, s);
		getGrades(doc, s ,g);
		
		mGradesVIEWSTATE = getVIEWSTATE(doc);
		if (mGradesVIEWSTATE == null || mGradesVIEWSTATE.length() <= 0)
			throw new IllegalStateException("GradesVIEWSTATE is NULL");
	}
	
	public void getGrades() throws ParseException, MalformedURLException, IOException {

		String page;
		
		if ((page = getPageByURL(GRADES_PAGE)) == null)
			return;

		parseGradesPage(page);
	}
	
	public void getGrades(Pupil p, Schedule s, GradeSemester sem)
			throws ParseException, MalformedURLException, IOException {

		String page = getGradesPage(p, s, sem);
		if (page == null)
			return;
		
		parseGradesPage(page);
	}

	public boolean loginSequence() throws MalformedURLException,
			IOException, InvalidCredentialsException {

		if (!loginGetAuthVIEWSTATE())
			throw new IllegalStateException();

		if (!loginGetASPXAUTH())
			throw new InvalidCredentialsException();

		if (!loginGetSessionId())
			throw new IllegalStateException();

		mIsLoggedIn = true;
		return true;
	}
}