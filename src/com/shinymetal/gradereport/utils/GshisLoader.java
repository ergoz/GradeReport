package com.shinymetal.gradereport.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.auth.InvalidCredentialsException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.util.Log;

import com.shinymetal.gradereport.objects.GradeSemester;
import com.shinymetal.gradereport.objects.Lesson;
import com.shinymetal.gradereport.objects.Pupil;
import com.shinymetal.gradereport.objects.Schedule;
import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.objects.Week;

public class GshisLoader {
	
	protected final static String SITE_NAME = "http://schoolinfo.educom.ru";
	protected final static String LOGIN_STEP_1 = "/Login.aspx?ReturnUrl=%2fdefault.aspx";
	protected final static String LOGIN_STEP_2 = "/default.aspx?action=login";
	protected final static String LESSONS_PAGE = "/Pupil/Lessons.aspx";
	protected final static String DIARY_PAGE = "/Pupil/Diary.aspx";
	protected final static String GRADES_PAGE = "/Pupil/Grades.aspx";
	
	protected final static String ERROR_CANNOT_LOAD_DATA = "���������� �������� ������ � �������";
	protected final static String ERROR_INV_CREDENTIALS = "�������� ����� ��� ������";

	protected String mCookieARRAffinity;
	protected String mCookieASPXAUTH;
	protected String mCookieASPNET_SessionId;

	protected String mAuthVIEWSTATE;
	protected String mLessonsVIEWSTATE;
	protected String mDiaryVIEWSTATE;
	protected String mGradesVIEWSTATE;
	
	protected String mLogin;
	protected String mPassword;
	
	private static volatile GshisLoader instance;
	
	protected boolean mIsLoggedIn;
	protected boolean mIsLastNetworkCallFailed = false;
	protected String mLastNetworkFailureReason;
	
	protected Date mCurrWeekStart = Week.getWeekStart(new Date ());
	
	protected String currentPupilName;
	
	public Date getCurrWeekStart() {
		return mCurrWeekStart;
	}

	public void setCurrWeekStart(Date currWeekStart) {
		this.mCurrWeekStart = currWeekStart;
	}


	public void setLogin(String login) {
		this.mLogin = login;
	}
	
	public String getLogin() {
		
		return mLogin;
	}

	public void setPassword(String password) {
		this.mPassword = password;
	}

	public void parseLessonsPage(String page) throws ParseException {

		Document doc = Jsoup.parse(page);

		Pupil p = GshisHTMLParser.getSelectedPupil(doc);
		Schedule s = GshisHTMLParser.getSelectedSchedule(doc, p);
		GshisHTMLParser.getWeeks(doc, s);
		GshisHTMLParser.getLessons(doc, s);
		
		mLessonsVIEWSTATE = GshisHTMLParser.getVIEWSTATE(doc);
	}
	
	public void parseDiaryPage(String page) throws ParseException {

		Document doc = Jsoup.parse(page);

		Pupil p = GshisHTMLParser.getSelectedPupil(doc);
		Schedule s = GshisHTMLParser.getSelectedSchedule(doc, p);
		GshisHTMLParser.getLessonsDetails(doc, s);
		
		mDiaryVIEWSTATE = GshisHTMLParser.getVIEWSTATE(doc);
	}

	public void parseGradesPage(String page) throws ParseException {

		Document doc = Jsoup.parse(page);

		Pupil p = GshisHTMLParser.getSelectedPupil(doc);
		Schedule s = GshisHTMLParser.getSelectedSchedule(doc, p);
		GradeSemester g = GshisHTMLParser.getActiveGradeSemester(doc, s);
		GshisHTMLParser.getGrades(doc, s ,g);
		
		mGradesVIEWSTATE = GshisHTMLParser.getVIEWSTATE(doc);
	}

	protected String getCookieByName(HttpURLConnection uc, String cookieName) {

		String headerName = null;

		for (int i = 1; (headerName = uc.getHeaderFieldKey(i)) != null; i++) {

			if (headerName.equals("Set-Cookie")) {
				String cookie = uc.getHeaderField(i);

				cookie = cookie.substring(0, cookie.indexOf(";"));
				String name = cookie.substring(0, cookie.indexOf("="));
				String value = cookie.substring(cookie.indexOf("=") + 1,
						cookie.length());

				if (name.equals(cookieName))
					return value;
			}
		}

		return null;
	}
	
	protected HttpURLConnection getHttpURLConnection(String url)
			throws MalformedURLException, IOException {

		return (HttpURLConnection) new URL(url).openConnection(/* new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
				"192.168.112.14", 8080))*/);
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
		mAuthVIEWSTATE = GshisHTMLParser.getVIEWSTATE(doc);
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

		if (mCookieASPXAUTH.length() <= 0
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
	
	protected String getLessons(Pupil p, Schedule s, Week w)
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

	protected String getLessonDetails(Pupil p, Schedule s, Week w)
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

		System.out.println("Loaded week details " + w.toString());
		return String.valueOf(tmp);
	}
	
	protected GshisLoader() {

		mIsLoggedIn = false;
	}
	
	public static GshisLoader getInstance() {

		GshisLoader localInstance = instance;

		if (localInstance == null) {

			synchronized (GshisLoader.class) {

				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new GshisLoader();
				}
			}
		}

		return localInstance;
	}

	protected boolean loginSequence() throws MalformedURLException,
			IOException, InvalidCredentialsException {
		
		if (!loginGetAuthVIEWSTATE()) {
			
			throw new IllegalStateException (ERROR_CANNOT_LOAD_DATA);
		}

		if (!loginGetASPXAUTH()) {
			
			throw new InvalidCredentialsException ();
		}

		if (!loginGetSessionId()) {
			
			throw new IllegalStateException (ERROR_CANNOT_LOAD_DATA);
		}
		
		mIsLoggedIn = true;
		return true;
	}
	
	public void reset () {

		mIsLoggedIn = false;
		
		mAuthVIEWSTATE = null;
		mLessonsVIEWSTATE = null;
		mDiaryVIEWSTATE = null;
		mGradesVIEWSTATE = null;
	}
	
	protected boolean parseLessonsByDate(Date day, String pupilName)
			throws MalformedURLException, IOException, ParseException {

		String page;

		if (mLessonsVIEWSTATE == null || mLessonsVIEWSTATE.length() <= 0) {

			if ((page = getPageByURL(LESSONS_PAGE)) == null) {
				Log.e("getLessonsByDate()", "getPageByURL () [1] failed!");
				return false;
			}

			parseLessonsPage(page);
		}
		
		Log.e("getLessonsByDate()", "AAAAAAAAAAAAAAAAAAAA!");
		
				
		if (pupilName == null)
			pupilName = currentPupilName;
		
		Log.e("getLessonsByDate()", "BBBBBBBBBBBBBBBBBBBB!");

		Pupil p = Pupil.getByFormName(pupilName);
		
		Log.e("getLessonsByDate()", "CCCCCCCCCCCCCCCCCCCC!");
		Schedule s = p.getScheduleByDate(day);
		
		Log.e("getLessonsByDate()", "DDDDDDDDDDDDDDDDDDDD!");
		boolean weekLoaded = s.getWeek(day).getLoaded();

		if (!weekLoaded) {

			page = getLessons(p, s, s.getWeek(day));
			if (page == null) {
				Log.e("getLessonsByDate()", "getLessons () [1] failed!");
				return false;
			}
			parseLessonsPage(page);
		}

		if (mDiaryVIEWSTATE == null || mDiaryVIEWSTATE.length() <= 0) {
			if ((page = getPageByURL(DIARY_PAGE)) == null) {
				Log.e("getLessonsByDate()", "getPageByURL () [2] failed!");
				return false;
			}

			parseDiaryPage(page);
		}

		if (!weekLoaded) {
			page = getLessonDetails(p, s, s.getWeek(day));
			if (page == null) {
				Log.e("getLessonsByDate()", "getLessonDetails () [1] failed!");
				return false;
			}
			parseDiaryPage(page);
		}

		return s.getWeek(day).getLoaded();

	}

	public ArrayList<Lesson> getLessonsByDate(Date day, String pupilName, boolean canLoad) {
		
		ArrayList<Lesson> res = new ArrayList<Lesson> ();
		boolean requestNeeded = false;
		
		String uName = currentPupilName;
		if (pupilName != null)
			uName = pupilName;

		Pupil p;
		Schedule s;
		
		if (!mIsLoggedIn) {
			
			if (!canLoad)
				return null;
			
			// Clear network errors
			mIsLastNetworkCallFailed = false;
			mLastNetworkFailureReason = "";
			
			for (int i=0; i<2; i++) {
				
				try {
					if (loginSequence()) break;
					
				} catch (Exception e) {
					
					mIsLastNetworkCallFailed = true;
					if ((mLastNetworkFailureReason = e.getMessage()) == null)
						mLastNetworkFailureReason = e.toString();
					
					if (e instanceof InvalidCredentialsException) {
						
						mLastNetworkFailureReason = ERROR_INV_CREDENTIALS;
						return null; // else try one more time
					}
				}
			}

			requestNeeded = true;
		}
		
		if (!mIsLoggedIn) return null;
		
		if (uName == null) {
			
			requestNeeded = true;
		}
		else try {
			
			p = Pupil.getByFormName(uName);
			s = p.getScheduleByDate(day);
			
			if ( !s.getWeek(day).getLoaded() ) {
				requestNeeded = true;
			}
			
		} catch (Exception e) { // either NullPointerException or IllegalArgumentException
			
			Log.i(this.toString(), TS.get() + this.toString()
					+ " getLessonsByDate() : " + e.toString());
			
			requestNeeded = true;
		}
		
		if (requestNeeded && !canLoad)
			return null;

		int l = 1;
		
		mIsLastNetworkCallFailed = false;
		mLastNetworkFailureReason = "";

		try {
			if (requestNeeded) {
				

				Log.i(this.toString(), TS.get() + this.toString()
						+ " getLessonsByDate() : ZZZZZ" );
				
				for (int i=0; i<2; i++)
					if (parseLessonsByDate(day, uName)) break;
			}
	
			// TODO: fix this
			if (uName == null)
				uName = currentPupilName;

			p = Pupil.getByFormName(uName);
			s = p.getScheduleByDate(day);
			
			while (true) {

				res.add(s.getLessonByNumber(day, l));
				l++;
			}
		
		} catch (NullPointerException e) {
			
		} catch (Exception e) {

			mIsLastNetworkCallFailed = true;
			if ((mLastNetworkFailureReason = e.getMessage()) == null)
				mLastNetworkFailureReason = e.toString();
		}
		
		Log.i(this.toString(), TS.get() + "getLessonsByDate (), added " + l
				+ " lessons for date " + day.toString());	
		return res;
	}
	
	public boolean isLastNetworkCallFailed() {
		return mIsLastNetworkCallFailed;
	}

	public String getLastNetworkFailureReason() {
		return mLastNetworkFailureReason;
	}

	public String getPupilIdByName(String name) {
		
		for (Pupil p : Pupil.getSet()) {
			
			if (p.getFormText().equals(name)) {
				return p.getFormId();
			}			
		}
		return null;
	}
	
	public ArrayList<String> getPupilNames () {
		
		ArrayList<String> res = new ArrayList<String> (); 

		for (Pupil p : Pupil.getSet()) {
			res.add(p.getFormText());
		}
			
		return res;
	}
	
	public void selectPupilByName (String name) {

		currentPupilName = name;
	}
}