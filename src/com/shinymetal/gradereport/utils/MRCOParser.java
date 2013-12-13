package com.shinymetal.gradereport.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.http.auth.InvalidCredentialsException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.util.Log;

import com.shinymetal.gradereport.BuildConfig;
import com.shinymetal.gradereport.objects.GradeSemester;
import com.shinymetal.gradereport.objects.Lesson;
import com.shinymetal.gradereport.objects.Pupil;
import com.shinymetal.gradereport.objects.Schedule;
import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.objects.Week;

public class MRCOParser extends BasicParser {
	
	private final static String SITE_NAME = "https://new.mcko.ru";
	private final static String LOGIN_STEP_1 = "";
	private final static String LESSONS_PAGE = "/new_mcko/index.php?c=dnevnik&d=rasp";
	private final static String DIARY_PAGE = "/new_mcko/index.php?c=dnevnik&d=dnev";
	private final static String GRADES_PAGE = "/new_mcko/index.php?c=dnevnik&d=usp";
	
	private String cookieSESSION_NAME = null;
	private String cookieSessionINT = null;
	private String mLoginField;
	private String mPassField;
	
	private Long mTod;	
	private String mCurrentPupilName;
	
	protected boolean loginGetSESSION_NAME() throws MalformedURLException,
			IOException {

		HttpURLConnection uc = getHttpURLConnection(SITE_NAME + LOGIN_STEP_1);
		uc.connect();

		String sessionName = getCookieByName(uc, "SESSION_NAME");
		if (sessionName != null)
			cookieSESSION_NAME = sessionName;

		String sessionINT = getCookieByName(uc, "sessionINT");
		if (sessionINT != null)
			cookieSessionINT = sessionINT;

		String line = null;
		StringBuffer tmp = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				uc.getInputStream(), "UTF-8"));
		while ((line = in.readLine()) != null) {
			tmp.append(line + "\n");
		}

		Document doc = Jsoup.parse(String.valueOf(tmp));
		for (Element input : doc.getElementsByClass("input_n")) {

			for (Element field : input.getElementsByTag("input")) {

				if (field.hasAttr("name")
						&& field.attr("name").matches("login.*")) {

					mLoginField = field.attr("name");

				} else if (field.hasAttr("name")
						&& field.attr("name").matches("pass.*")) {

					mPassField = field.attr("name");
				}
			}
		}

		return cookieSESSION_NAME != null && cookieSESSION_NAME.length() > 0;
	}

	private boolean loginGetSessionINT() throws UnsupportedEncodingException, IOException {

		if (cookieSESSION_NAME.length() <= 0) {
			return false;
		}

		HttpURLConnection uc = getHttpURLConnection(SITE_NAME + LOGIN_STEP_1);
		uc.setInstanceFollowRedirects(false);

		String urlParameters = "";

		urlParameters += encodePOSTVar(mLoginField, getLogin());
		urlParameters += encodePOSTVar(mPassField, getPassword());

		uc.setRequestMethod("POST");

		String cookie = "SESSION_NAME="
				+ cookieSESSION_NAME
				+ (cookieSessionINT != null && cookieSessionINT.length() > 0 ? "; sessionINT="
						+ cookieSessionINT
						: "");
		uc.setRequestProperty("Cookie", cookie);
		System.out.println("Cookie: " + cookie);
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

		String line = null;
		StringBuffer tmp = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				uc.getInputStream(), "windows-1251"));
		while ((line = in.readLine()) != null) {
			tmp.append(line + "\n");
		}

		String sessionName = getCookieByName(uc, "SESSION_NAME");
		if (sessionName != null)
			cookieSESSION_NAME = sessionName;

		String sessionINT = getCookieByName(uc, "sessionINT");
		if (sessionINT != null)
			cookieSessionINT = sessionINT;

		return cookieSessionINT != null && cookieSessionINT.length() > 0;
	}


	public boolean loginSequence() throws MalformedURLException, IOException,
			InvalidCredentialsException {
		
		if (!loginGetSESSION_NAME())
			throw new IllegalStateException();

		if (!loginGetSessionINT())
			throw new InvalidCredentialsException();

		return false;
	}
	
	public String getPageByURL(String pageUrl) throws UnsupportedEncodingException, IOException {

		if (cookieSESSION_NAME == null || cookieSESSION_NAME.length() <= 0
				|| cookieSessionINT == null || cookieSessionINT.length() <= 0) {
			return null;
		}

		HttpURLConnection uc = getHttpURLConnection(SITE_NAME + pageUrl);

		String cookie = "SESSION_NAME="
				+ cookieSESSION_NAME
				+ (cookieSessionINT != null && cookieSessionINT.length() > 0 ? "; sessionINT="
						+ cookieSessionINT
						: "");
		uc.setRequestProperty("Cookie", cookie);
		uc.setRequestProperty("Origin", SITE_NAME);
		uc.setRequestProperty("Referer", SITE_NAME + LOGIN_STEP_1);

		uc.connect();

		String line = null;
		StringBuffer tmp = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				uc.getInputStream(), "windows-1251"));
		while ((line = in.readLine()) != null) {
			tmp.append(line + "\n");
		}

		return tmp.toString();
	}

	protected String getLessonsPage(Pupil p, Schedule s, Week w, String page)
			throws MalformedURLException, IOException {
		
		if (cookieSESSION_NAME == null || cookieSESSION_NAME.length() <= 0
				|| cookieSessionINT == null || cookieSessionINT.length() <= 0) {
			return null;
		}

		HttpURLConnection uc = getHttpURLConnection(SITE_NAME + page);

		String urlParameters = "";

		urlParameters += encodePOSTVar("tod", "" + mTod);
		urlParameters += encodePOSTVar("date", "" + w.getStart().getTime() * 1000);

		uc.setRequestMethod("POST");
		String cookie = "SESSION_NAME="
				+ cookieSESSION_NAME
				+ (cookieSessionINT != null && cookieSessionINT.length() > 0 ? "; sessionINT="
						+ cookieSessionINT
						: "");
		uc.setRequestProperty("Cookie", cookie);
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
				uc.getInputStream(), "windows-1251"));
		while ((line = in.readLine()) != null) {
			tmp.append(line + "\n");
		}

		return String.valueOf(tmp);
	}

	
	public Pupil getSelectedPupil(String login, Document doc) {
		
		String pupil = doc.getElementsByAttributeValue("id", "name_uch").first().text();
		Pupil p; 
		
		if ((p = Pupil.getByFormId(login, pupil)) == null) {
			
			p =  new Pupil(pupil, pupil);
			long rowId = p.insert(login);
			
			if (BuildConfig.DEBUG)
				Log.d("GshisParser", TS.get()
						+ " Pupil.insert() = " + rowId);
		}
		
		return p;
	}
	
	public Schedule getSelectedSchedule(Document doc, Pupil selPupil) throws ParseException {
		
		mTod = Long.parseLong(doc.getElementsByAttributeValue("id", "tod").first().attr("value"));
		Date today = new Date(mTod*1000), start, stop;
		long yStart, yStop;
		boolean firstHalf = true;
		Calendar cal = Calendar.getInstance();
		Schedule schedule;

		cal.setTime(today);
		
		if (cal.get(Calendar.MONTH) < Calendar.AUGUST)
			firstHalf = false;
		
		cal.add(Calendar.YEAR, firstHalf ? 0 : -1);
		yStart = cal.get(Calendar.YEAR);
		
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MONTH, Calendar.SEPTEMBER);

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		start = cal.getTime();

		cal.add(Calendar.YEAR, 1);
		yStop = cal.get(Calendar.YEAR);
		
		cal.set(Calendar.DAY_OF_MONTH, 31);
		cal.set(Calendar.MONTH, Calendar.MAY);
		
		stop = cal.getTime();
		
		if ((schedule = selPupil.getScheduleByFormId("" + yStart)) == null) {

			schedule = new Schedule("" + yStart, "" + yStart + " - " + yStop);
			schedule.setStart(start);
			schedule.setStop(stop);
			selPupil.addSchedule(schedule);
		}
		return schedule;
	}
	
	public Week getSelectedWeek(Document doc, Schedule s) throws ParseException {
		
		Date today = new Date(mTod*1000);
		Week w, wCurr = null;
		SimpleDateFormat f = new SimpleDateFormat("dd.MM", Locale.ENGLISH);
		Calendar cal = Calendar.getInstance();
		
		today = Week.getWeekStart(today);		
		while (s.getStart().getTime() <= today.getTime()) {

			cal.setTime(today);
			
			if ((w = s.getWeek("" + cal.get(Calendar.WEEK_OF_YEAR))) == null ) {
				
				w = new Week();
				w.setFormId("" + cal.get(Calendar.WEEK_OF_YEAR));
				w.setStart(Week.getWeekStart(today));
				w.setStop(Week.getWeekStop(today));
				w.setFormText(f.format(w.getStart()) + " - " + f.format(w.getStop()));
				s.addWeek(w);
			}
			
			if (wCurr == null) {
				
				wCurr = w;
				wCurr.setLoaded().update();
			}
			
			cal.add(Calendar.DATE, -7);
			today = cal.getTime();
		}
		
		return wCurr;
	}
	
	protected void getLessons(Document doc, Schedule s) throws ParseException {

		Date today = new Date(mTod*1000);
		Calendar cal = Calendar.getInstance();
		Lesson l;

		cal.setTime(today);

		cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

		Date date = new Date();

		for (Element day : doc.getElementsByClass("day")) {

			String caption = day.getElementsByTag("caption").first().text();
			String stoday = caption.substring(caption.length() - 5,
					caption.length());

			int mday = Integer.parseInt(stoday.substring(0, stoday.indexOf(".")));
			int month = Integer.parseInt(stoday.substring(stoday.indexOf(".") + 1, stoday.length()));

			cal.set(Calendar.DAY_OF_MONTH, mday);
			cal.set(Calendar.MONTH, month - 1);

			for (Element tr : day.getElementsByTag("tr")) {

				int index = 0;
				int number = 0;
				String name = "";

				Element firstTd = tr.getElementsByTag("td").first();
				if (firstTd == null || firstTd.text() == null
						|| firstTd.text().length() <= 0
				/* || !firstTd.text().matches("[-+]?\\d*\\.?\\d+") */)
					continue; // header/footer

				for (Element td : tr.getElementsByTag("td")) {

					switch (index) {
					case 0: // number
						number = Integer.parseInt(td.text().substring(0,
								td.text().length() - 1));
						break;
					case 1: // time
						// System.out.println("Time: " + td.text());

						if (td.text().indexOf(":") != -1) {
							int hours = Integer.parseInt(td.text().substring(0,
									td.text().indexOf(":")));
							int minutes = Integer.parseInt(td.text().substring(
									td.text().indexOf(":") + 1,
									td.text().length()));

							cal.set(Calendar.HOUR_OF_DAY, hours);
							cal.set(Calendar.MINUTE, minutes);

							date = cal.getTime();
						}
						break;
					case 2: // lesson
						name = td.text();
						break;
					}

					index++;
				}

				if (name.length() > 0) {
					
					if ((l = s.getLessonByNumber(date, number)) == null) {
						
						l = new Lesson();

						l.setStart(date);
						// TODO: fix stop date
						l.setStop(date);
						l.setFormId(name);
						l.setFormText(name);
						l.setTeacher("");
						l.setNumber(number);

						s.addLesson(l);						
					}
				}
			}
		}	
	}
	
	protected String fetchLongCellString(Element e) {

		for (Element div : e.getElementsByTag("div")) {

			if (div.hasAttr("class") && div.attr("class").equals("com")) {
				return div.text();
			}
		}
		return e.text();
	}

	protected void getLessonsDetails(Document doc, Schedule s) {

		Date today = new Date(mTod*1000);
		Calendar cal = Calendar.getInstance();
		Lesson l;

		cal.setTime(today);

		cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

		for (Element day : doc.getElementsByClass("day")) {

			String caption = day.getElementsByTag("caption").first().text();
			String stoday = caption.substring(caption.length() - 5,
					caption.length());

			int mday = Integer.parseInt(stoday.substring(0, stoday.indexOf(".")));
			int month = Integer.parseInt(stoday.substring(stoday.indexOf(".") + 1, stoday.length()));

			cal.set(Calendar.DAY_OF_MONTH, mday);
			cal.set(Calendar.MONTH, month - 1);

			System.out.println(caption + "  " + cal.getTime());

			for (Element tr : day.getElementsByTag("tr")) {

				int index = 0;
				int number = 0;
				
				String name = "";
				String homework = "";
				String mark = "";
				String comment = "";

				Element firstTd = tr.getElementsByTag("td").first();
				if (firstTd == null || firstTd.text() == null
						|| firstTd.text().length() <= 0
				/* || !firstTd.text().matches("[-+]?\\d*\\.?\\d+") */)
					continue; // header/footer

				for (Element td : tr.getElementsByTag("td")) {

					switch (index) {
					case 0: // number
						number = Integer.parseInt(td.text().substring(0,
								td.text().length() - 1));
						break;
					case 1: // lesson
						name = td.text();
						break;
					case 2:
						homework = fetchLongCellString(td);
						break;
					case 3:
						comment = td.getElementsByTag("div").first().text();
						if (comment.indexOf("-") != -1)
							mark = comment.substring(0, comment.indexOf("-") - 1);
						break;
					}


					index++;
				}
				
				if (name.length() > 0) {
					
					if ((l = s.getLessonByNumber(today, number)) != null) {
						
						l.setHomework(homework);
						l.setMarks(mark);
						l.setComment(comment);

						l.update();
					}
				}
			}
		}	
	}
	
	protected void fetchGrades(Document doc) {
		
	}

	public Week parseLessonsPage(String page) throws ParseException {

		Document doc = Jsoup.parse(page);

		Pupil p = getSelectedPupil(mLogin, doc);
		mCurrentPupilName = p.getFormText();
		
		Schedule s = getSelectedSchedule(doc, p);		
		Week w = getSelectedWeek(doc, s);
		getLessons(doc, s);

		return w;
	}
	
	public void parseLessonsDetailsPage(String page) throws ParseException {

		Document doc = Jsoup.parse(page);

		Pupil p = getSelectedPupil(mLogin, doc);
		mCurrentPupilName = p.getFormText();
		
		Schedule s = getSelectedSchedule(doc, p);
		getLessonsDetails(doc, s);		
	}

	public void getLessons() throws ParseException, MalformedURLException,
			IOException {

		String page;
		
		if ((page = getPageByURL(LESSONS_PAGE)) == null)
			return;
		
		parseLessonsPage(page);
	}

	public void getLessons(Pupil p, Schedule s, Week w) throws ParseException,
			MalformedURLException, IOException {
		
		String page = getLessonsPage(p, s, w, LESSONS_PAGE);
		
		if (page == null)
			return;

		parseLessonsPage(page);
	}

	public void getLessonsDetails() throws ParseException,
			MalformedURLException, IOException {
		
		String page;
		
		if ((page = getPageByURL(DIARY_PAGE)) == null)
			return;

		parseLessonsDetailsPage(page);
	}

	public void getLessonsDetails(Pupil p, Schedule s, Week w)
			throws ParseException, MalformedURLException, IOException {
		
		String page = getLessonsPage(p, s, w, DIARY_PAGE);		
		if (page == null)
			return;
		
		parseLessonsDetailsPage(page);
	}

	public void getGrades() throws ParseException, MalformedURLException,
			IOException {

	}

	public void getGrades(Pupil p, Schedule s, GradeSemester sem)
			throws ParseException, MalformedURLException, IOException {

	}
}
