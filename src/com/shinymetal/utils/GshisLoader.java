package com.shinymetal.utils;

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
import java.util.ArrayList;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.util.Log;

import com.shinymetal.objects.Lesson;
import com.shinymetal.objects.Pupil;
import com.shinymetal.objects.Schedule;
import com.shinymetal.objects.User;
import com.shinymetal.objects.Week;


public class GshisLoader {
	
	protected final String siteUrl = "http://schoolinfo.educom.ru";
	protected final String loginstep1 = "/Login.aspx?ReturnUrl=%2fdefault.aspx";
	protected final String loginstep2 = "/default.aspx?action=login";
	protected final String lessonsUrl = "/Pupil/Lessons.aspx";
	protected final String diaryUrl = "/Pupil/Diary.aspx";
	protected final String gradesUrl = "/Pupil/Grades.aspx";
//	protected final String perfUrl = "/Pupil/Performance.aspx";

	protected User user;

	protected String cookieARRAffinity;
	protected String cookieASPXAUTH;
	protected String cookieASPNET_SessionId;

	protected String authVIEWSTATE;
	protected String lessonsVIEWSTATE;
	protected String diaryVIEWSTATE;
	protected String gradesVIEWSTATE;
	
	protected Date currWeekStart = Week.getWeekStart(new Date ());
	
	public Date getCurrWeekStart() {
		return currWeekStart;
	}

	public void setCurrWeekStart(Date currWeekStart) {
		this.currWeekStart = currWeekStart;
	}

	public void parseLessonsPage(String page) throws ParseException {

		Document doc = Jsoup.parse(page);

		GshisHTMLParser.fetchUserName(doc, user);
		GshisHTMLParser.fetchPupils(doc, user);
		GshisHTMLParser.fetchYears(doc, user);
		GshisHTMLParser.fetchWeeks(doc, user);
		GshisHTMLParser.fetchLessons(doc, user);
		
		lessonsVIEWSTATE = GshisHTMLParser.getVIEWSTATE(doc);
	}
	
	public void parseDiaryPage(String page) throws ParseException {

		Document doc = Jsoup.parse(page);

		GshisHTMLParser.fetchUserName(doc, user);
		GshisHTMLParser.fetchPupils(doc, user);
		GshisHTMLParser.fetchYears(doc, user);
		GshisHTMLParser.fetchLessonsDetails(doc, user);
		
		diaryVIEWSTATE = GshisHTMLParser.getVIEWSTATE(doc);
	}

	public void parseGradesPage(String page) throws ParseException {

		Document doc = Jsoup.parse(page);

		GshisHTMLParser.fetchUserName(doc, user);
		GshisHTMLParser.fetchPupils(doc, user);
		GshisHTMLParser.fetchYears(doc, user);
		GshisHTMLParser.fetchGradeSemesters(doc, user);
		GshisHTMLParser.fetchGrades(doc, user);
		
		gradesVIEWSTATE = GshisHTMLParser.getVIEWSTATE(doc);
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

		return (HttpURLConnection) new URL(url).openConnection();
	}
	
	protected String encodePOSTVar(String name, String value) throws UnsupportedEncodingException	{
		
		return URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8") + "&"; 
	}

	protected boolean loginGetCookiesStep1() {

		try {

			HttpURLConnection uc = getHttpURLConnection (siteUrl + loginstep1);
			uc.connect();

			String affinity = getCookieByName(uc, "ARRAffinity");
			if (affinity != null)
				cookieARRAffinity = affinity;

			String line = null;
			StringBuffer tmp = new StringBuffer();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					uc.getInputStream(), "UTF-8"));
			while ((line = in.readLine()) != null) {
				tmp.append(line + "\n");
			}

			Document doc = Jsoup.parse(String.valueOf(tmp));
			authVIEWSTATE = GshisHTMLParser.getVIEWSTATE(doc);
			if (authVIEWSTATE != null) 
				return true;

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	protected boolean loginGetCookiesStep2() {

		if (authVIEWSTATE.length() <= 0) {
			return false;
		}

		try {
			HttpURLConnection uc = getHttpURLConnection(siteUrl + loginstep1);
			uc.setInstanceFollowRedirects(false);

			String urlParameters = "";

			urlParameters += encodePOSTVar("__EVENTTARGET", "ctl00$btnLogin");
			urlParameters += encodePOSTVar("__VIEWSTATE", authVIEWSTATE);
			urlParameters += encodePOSTVar("ctl00$txtLogin", user.getLogin());
			urlParameters += encodePOSTVar("ctl00$txtPassword", user.getPassword());
			
			System.out.println("urlParameters: " + urlParameters);

			uc.setRequestMethod("POST");
			uc.setRequestProperty("Cookie", "ARRAffinity=" + cookieARRAffinity);
			uc.setRequestProperty("Origin", siteUrl);
			uc.setRequestProperty("Referer", siteUrl + loginstep1);
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

				cookieASPXAUTH = aspxauth;
				return true;
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	protected boolean loginGetCookiesStep3() {

		if (cookieASPXAUTH.length() <= 0) {
			return false;
		}

		try {
			HttpURLConnection uc = getHttpURLConnection(siteUrl + loginstep2);
			uc.setInstanceFollowRedirects(false);
			uc.setRequestProperty("Cookie", "ARRAffinity=" + cookieARRAffinity
					+ "; .ASPXAUTH=" + cookieASPXAUTH);
			uc.setRequestProperty("Origin", siteUrl);
			uc.setRequestProperty("Referer", siteUrl + loginstep1);

			uc.connect();

			String sessionId = getCookieByName(uc, "ASP.NET_SessionId");
			if (sessionId != null) {

				cookieASPNET_SessionId = sessionId;
				return true;
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	protected String getPageByURL(String pageUrl) {

		if (cookieASPXAUTH.length() <= 0
				|| cookieASPNET_SessionId.length() <= 0) {
			return null;
		}

		try {
			HttpURLConnection uc = getHttpURLConnection(siteUrl + pageUrl);
//			uc.setInstanceFollowRedirects(false);
			uc.setRequestProperty("Cookie", "ARRAffinity=" + cookieARRAffinity
					+ "; .ASPXAUTH=" + cookieASPXAUTH + "; ASP.NET_SessionId="
					+ cookieASPNET_SessionId);
			uc.setRequestProperty("Origin", siteUrl);
			uc.setRequestProperty("Referer", siteUrl + loginstep1);

			uc.connect();

			String line = null;
			StringBuffer tmp = new StringBuffer();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					uc.getInputStream(), "UTF-8"));
			while ((line = in.readLine()) != null) {
				tmp.append(line + "\n");
			}

			return tmp.toString();

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	protected String getLessons (Pupil p, Schedule s, Week w) {
		
		if (cookieASPXAUTH.length() <= 0
				|| cookieASPNET_SessionId.length() <= 0) {
			return null;
		}

		try {
			HttpURLConnection uc = getHttpURLConnection(siteUrl + lessonsUrl);

			String urlParameters = "";

			/*
			 * Do NOT add ctl00$sm, __EVENTTARGET, __EVENTARGUMENT, __LASTFOCUS,
			 * __ASYNCPOST, this will break everything for unknown reason!
			 */
			urlParameters += encodePOSTVar("__VIEWSTATE", lessonsVIEWSTATE);
			urlParameters += encodePOSTVar("ctl00$learnYear$drdLearnYears", s.getFormId());
			urlParameters += encodePOSTVar("ctl00$topMenu$pupil$drdPupils", p.getFormId());
			urlParameters += encodePOSTVar("ctl00$topMenu$tbUserId", p.getFormId());
			urlParameters += encodePOSTVar("ctl00$leftMenu$accordion_AccordionExtender_ClientState", "");
			urlParameters += encodePOSTVar("ctl00$body$week$drdWeeks", w.getFormId());
			
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Cookie", "ARRAffinity=" + cookieARRAffinity
					+ "; ASP.NET_SessionId=" + cookieASPNET_SessionId
					+ "; .ASPXAUTH=" + cookieASPXAUTH );
					
			uc.setRequestProperty("Origin", siteUrl);
			uc.setRequestProperty("Referer", siteUrl + lessonsUrl);
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

			System.out.println("Loaded week " + w.toString());
			return String.valueOf(tmp);
		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;		
	}

	protected String getLessonDetails (Pupil p, Schedule s, Week w) {
		
		if (cookieASPXAUTH.length() <= 0
				|| cookieASPNET_SessionId.length() <= 0) {
			return null;
		}

		try {
			HttpURLConnection uc = getHttpURLConnection(siteUrl + diaryUrl);

			String urlParameters = "";

			/*
			 * Do NOT add ctl00$sm, __EVENTTARGET, __EVENTARGUMENT, __LASTFOCUS,
			 * __ASYNCPOST, this will break everything for unknown reason!
			 */
			urlParameters += encodePOSTVar("__VIEWSTATE", diaryVIEWSTATE);
			urlParameters += encodePOSTVar("ctl00$learnYear$drdLearnYears", s.getFormId());
			urlParameters += encodePOSTVar("ctl00$topMenu$pupil$drdPupils", p.getFormId());
			urlParameters += encodePOSTVar("ctl00$topMenu$tbUserId", p.getFormId());
			urlParameters += encodePOSTVar("ctl00$leftMenu$accordion_AccordionExtender_ClientState", "");
			urlParameters += encodePOSTVar("ctl00$body$period$drdPeriodType", "1");
			urlParameters += encodePOSTVar("ctl00$body$period$week$drdWeeks", w.getFormId());
			
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Cookie", "ARRAffinity=" + cookieARRAffinity
					+ "; ASP.NET_SessionId=" + cookieASPNET_SessionId
					+ "; .ASPXAUTH=" + cookieASPXAUTH );
					
			uc.setRequestProperty("Origin", siteUrl);
			uc.setRequestProperty("Referer", siteUrl + diaryUrl);
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
		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;		
	}
	
	private static volatile GshisLoader instance;
	
	protected boolean isLoggedIn;

	protected GshisLoader() {

		user = new User();
		isLoggedIn = false;
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

	protected boolean login () {
		
		if (!loginGetCookiesStep1()) {
			Log.e("login", "loginGetCookiesStep1 () failed!");
			return false;
		}

		if (!loginGetCookiesStep2()) {
			Log.e("login", "loginGetCookiesStep2 () failed!");
			return false;
		}

		if (!loginGetCookiesStep3()) {
			Log.e("login", "loginGetCookiesStep3 () failed!");
			return false;
		}
		
		isLoggedIn = true;
		return true;
	}
	
	public void reset () {

		isLoggedIn = false;
		
		authVIEWSTATE = null;
		lessonsVIEWSTATE = null;
		diaryVIEWSTATE = null;
		gradesVIEWSTATE = null;
	}
	
	protected boolean parseLessonsByDate(Date day) {

		try {
			String page;
			
			if (lessonsVIEWSTATE == null || lessonsVIEWSTATE.length() <= 0
					|| user.getCurrentPupilId() == null) {
	
				if ((page = getPageByURL(lessonsUrl)) == null) {
					Log.e("getLessonsByDate()", "getPageByURL () [1] failed!");
					return false;
				}
	
				parseLessonsPage(page);
			}

			Pupil p = user.getCurrentPupil();
			Schedule s = p.getCurrentSchedule();
			boolean weekLoaded = s.getWeek(day).getLoaded();

			if (!weekLoaded) {

				page = getLessons(p, s, s.getWeek(day));
				if (page == null) {
					Log.e("getLessonsByDate()", "getLessons () [1] failed!");
					return false;
				}
				parseLessonsPage(page);
			}

			if (diaryVIEWSTATE == null || diaryVIEWSTATE.length() <= 0) {
				if ((page = getPageByURL(diaryUrl))== null) {
					Log.e("getLessonsByDate()", "getPageByURL () [2] failed!");
					return false;
				}
				
				parseDiaryPage(page);
			}
			
			if (!weekLoaded) {
				page = getLessonDetails(p, s, s.getWeek(day));
				if ( page == null) {
					Log.e("getLessonsByDate()", "getLessonDetails () [1] failed!");
					return false;
				}
				parseDiaryPage(page);
			}
			
			return s.getWeek(day).getLoaded();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public ArrayList<Lesson> getLessonsByDate(Date day) {
		
		ArrayList<Lesson> res = new ArrayList<Lesson> (); 
		Schedule s;
		boolean requestNeeded = false;
		
		if (!isLoggedIn) {
			
			for (int i=0; i<2; i++)
				if (login()) break;

			requestNeeded = true;
		}
		
		if (!isLoggedIn) return null;

		try {
			
			s = user.getCurrentPupil().getCurrentSchedule();
			
			if ( !s.getWeek(day).getLoaded() ) {
				requestNeeded = true;
			}
			
		} catch (NullPointerException e) {
			
			requestNeeded = true;
		}

		try {
			if (requestNeeded) {
				
				for (int i=0; i<2; i++)
					if (parseLessonsByDate(day)) break;
			}
	
			s = user.getCurrentPupil().getCurrentSchedule();
			int l = 1;
			
			while (true) {

				res.add(s.getLessonByNumber(day, l++));
			}
			
		} catch (NullPointerException e) {
			
		}
		
		return res;
	}	
}