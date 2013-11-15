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
import java.util.Set;

import org.apache.http.auth.InvalidCredentialsException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.database.Cursor;
import android.util.Log;

import com.shinymetal.gradereport.objects.GradeSemester;
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
	
	protected final static String ERROR_CANNOT_LOAD_DATA = "Невозможно получить данные с сервера";
	protected final static String ERROR_INV_CREDENTIALS = "Неверный логин или пароль";

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

	public Week parseLessonsPage(String page) throws ParseException {

		Document doc = Jsoup.parse(page);

		Pupil p = GshisHTMLParser.getSelectedPupil(doc);
		currentPupilName = p.getFormText();
		
		Schedule s = GshisHTMLParser.getSelectedSchedule(doc, p);		
		Week w = GshisHTMLParser.getSelectedWeek(doc, s);
		GshisHTMLParser.getLessons(doc, s);
		
		mLessonsVIEWSTATE = GshisHTMLParser.getVIEWSTATE(doc);
		return w;
	}
	
	public void parseLessonsDetailsPage(String page) throws ParseException {

		Document doc = Jsoup.parse(page);

		Pupil p = GshisHTMLParser.getSelectedPupil(doc);
		currentPupilName = p.getFormText();
		
		Schedule s = GshisHTMLParser.getSelectedSchedule(doc, p);
		GshisHTMLParser.getLessonsDetails(doc, s);
		
		mDiaryVIEWSTATE = GshisHTMLParser.getVIEWSTATE(doc);
	}

	public void parseGradesPage(String page) throws ParseException {

		Document doc = Jsoup.parse(page);

		Pupil p = GshisHTMLParser.getSelectedPupil(doc);
		currentPupilName = p.getFormText();
		
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

		return (HttpURLConnection) new URL(url).openConnection(/*new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
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
		Week week = null;
		
		Log.i (this.toString(), TS.get() + "parseLessonsByDate () : started");

		if (mLessonsVIEWSTATE == null || mLessonsVIEWSTATE.length() <= 0) {
			
			Log.i (this.toString(), TS.get() + "parseLessonsByDate () mLessonsVIEWSTATE is null, fetching...");

			if ((page = getPageByURL(LESSONS_PAGE)) == null) {
				
				Log.e("getLessonsByDate()", "getPageByURL () [1] failed!");
				return false;
			}

			Log.i (this.toString(), TS.get() + "parseLessonsByDate () call parseLessonsPage () ...");
			week = parseLessonsPage(page);
			Log.i (this.toString(), TS.get() + "parseLessonsByDate () call parseLessonsPage () done");
		}
		
		if (pupilName == null)
			pupilName = currentPupilName;
		
		Pupil p = Pupil.getByFormName(pupilName);
		Schedule s = p.getScheduleByDate(day);
		
		Log.i (this.toString(), TS.get() + "parseLessonsByDate () check if week is already loaded ...");
		
		boolean weekLoaded = false;
		if (week != null && week.getLoaded() && week.getStart().equals(Week.getWeekStart(day)))
		{
			weekLoaded = true;
		}
		else
			week = s.getWeek(day);
		
		Log.i (this.toString(), TS.get() + "parseLessonsByDate () check done");

		if (!weekLoaded) {
			
			Log.i (this.toString(), TS.get() + "parseLessonsByDate () call getLessons ()");

			page = getLessons(p, s, week);
			
			Log.i (this.toString(), TS.get() + "parseLessonsByDate () call getLessons () done ");
			
			if (page == null) {
				Log.e("getLessonsByDate()", "getLessons () [1] failed!");
				return false;
			}

			Log.i (this.toString(), TS.get() + "parseLessonsByDate () call parseLessonsPage () ...");
			parseLessonsPage(page);
			Log.i (this.toString(), TS.get() + "parseLessonsByDate () call parseLessonsPage () done");
		}

		if (mDiaryVIEWSTATE == null || mDiaryVIEWSTATE.length() <= 0) {
			
			Log.i (this.toString(), TS.get() + "parseLessonsByDate () mDiaryVIEWSTATE is null, fetching...");
			
			if ((page = getPageByURL(DIARY_PAGE)) == null) {
				Log.e("getLessonsByDate()", "getPageByURL () [2] failed!");
				return false;
			}

			Log.i (this.toString(), TS.get() + "parseLessonsByDate () call parseLessonsDetailsPage () ...");
			parseLessonsDetailsPage(page);
			Log.i (this.toString(), TS.get() + "parseLessonsByDate () call parseLessonsDetailsPage () done");
		}

		if (!weekLoaded) {
			
			Log.i (this.toString(), TS.get() + "parseLessonsByDate () call getLessonsDetails ()");
			
			page = getLessonDetails(p, s, week);
			
			Log.i (this.toString(), TS.get() + "parseLessonsByDate () call getLessonsDetails () done");
			
			if (page == null) {
				Log.e("getLessonsByDate()", "getLessonDetails () [1] failed!");
				return false;
			}
			
			Log.i (this.toString(), TS.get() + "parseLessonsByDate () call parseLessonsDetailsPage () ...");
			parseLessonsDetailsPage(page);
			Log.i (this.toString(), TS.get() + "parseLessonsByDate () call parseLessonsDetailsPage () done");
		}
		
		Log.i (this.toString(), TS.get() + "parseLessonsByDate () : finished");

		return week.getLoaded();
	}

	public Cursor getCursorLessonsByDate(Date day) {
		
		String uName;
		
		if (currentPupilName == null) {
				
			ArrayList<String> names = getPupilNames();
			if (names.size() > 0) currentPupilName = names.get(0);
		}
			
		if ((uName = currentPupilName) == null)
			return null;

		try {
			return Pupil.getByFormName(uName).getScheduleByDate(day)
					.getCursorLessonsByDate(day);

		} catch (Exception e) { // either NullPointerException or IllegalArgumentException
			
			Log.e(this.toString(), TS.get() + this.toString()
					+ " getCachedLessonsByDate() : Exception: " + e.toString());
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void getNonCachedLessonsByDate(Date day, String pupilName) {
		
		Log.i (this.toString(), TS.get() + "getNonCachedLessonsByDate () : started");
		
		String uName = pupilName;
		if (uName == null) {
			
			if (currentPupilName == null) {
				
				ArrayList<String> names = getPupilNames();
				if (names.size() > 0) currentPupilName = names.get(0);
			}
			
			uName = currentPupilName;
		}
		
		Log.i (this.toString(), TS.get() + "getNonCachedLessonsByDate () : trying to log in...");
		
		if (!mIsLoggedIn) {

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
						return; // else try one more time
					}
				}
			}
		}
		Log.i (this.toString(), TS.get() + "getNonCachedLessonsByDate () : logged in: " + mIsLoggedIn );
		
		if (!mIsLoggedIn) return;
		
		mIsLastNetworkCallFailed = false;
		mLastNetworkFailureReason = "";

		try {

			Log.i (this.toString(), TS.get() + "getNonCachedLessonsByDate () : calling parseLessonsByDate");
			
			for (int i=0; i<2; i++)
				if (parseLessonsByDate(day, uName)) break;
			
		} catch (Exception e) {
			
			Log.e(this.toString(),
					TS.get() + this.toString()
							+ " getNonCachedLessonsByDate() : Exception: "
							+ e.toString());
			
			e.printStackTrace();

			mIsLastNetworkCallFailed = true;
			if ((mLastNetworkFailureReason = e.getMessage()) == null)
				mLastNetworkFailureReason = e.toString();
		}
		
		Log.i (this.toString(), TS.get() + "getNonCachedLessonsByDate () : finished");
	}
	
	public void getAllPupilsLessons () {
		
		Log.i (this.toString(), TS.get() + "getAllPupilsLessons () : started");
		
		for (int i=0; i<2; i++) {
			
			try {
				if (loginSequence()) break;
				
			} catch (Exception e) {
				
				mIsLastNetworkCallFailed = true;
				if ((mLastNetworkFailureReason = e.getMessage()) == null)
					mLastNetworkFailureReason = e.toString();
				
				if (e instanceof InvalidCredentialsException) {
					
					mLastNetworkFailureReason = ERROR_INV_CREDENTIALS;
					return; // else try one more time
				}
			}
		}

		Set<Pupil> set = Pupil.getSet();
		if ( set != null && set.size() > 0) {
			for (Pupil p : set) {
	
				// TODO: implement the rest 
			}
		}
		else {
			
			// TODO: implement the rest, use null as pupil name
		}
		
		Log.i (this.toString(), TS.get() + "getAllPupilsLessons () : finished");
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
		
		Log.i (this.toString(), TS.get() + "getPupilNames () : started");
		
		ArrayList<String> res = new ArrayList<String> (); 

		for (Pupil p : Pupil.getSet()) {
			res.add(p.getFormText());
		}
		
		Log.i (this.toString(), TS.get() + "getPupilNames () : finished");
			
		return res;
	}
	
	public void selectPupilByName (String name) {

		currentPupilName = name;
	}
}