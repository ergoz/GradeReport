package com.shinymetal.gradereport.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.auth.InvalidCredentialsException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.shinymetal.gradereport.objects.GradeSemester;
import com.shinymetal.gradereport.objects.Pupil;
import com.shinymetal.gradereport.objects.Schedule;
import com.shinymetal.gradereport.objects.Week;

public class MRCOParser extends BasicParser {
	
	private final static String siteUrl = "https://new.mcko.ru";
	private final static String loginstep1 = "";
	private final static String lessonsUrl = "/new_mcko/index.php?c=dnevnik&d=rasp";
	private final static String diaryUrl = "/new_mcko/index.php?c=dnevnik&d=dnev";
	private final static String gradesUrl = "/new_mcko/index.php?c=dnevnik&d=usp";
	
	private String cookieSESSION_NAME = null;
	private String cookieSessionINT = null;
	private String mLoginField;
	private String mPassField;
	
	protected boolean loginGetSESSION_NAME() throws MalformedURLException,
			IOException {

		HttpURLConnection uc = getHttpURLConnection(siteUrl + loginstep1);
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

		HttpURLConnection uc = getHttpURLConnection(siteUrl + loginstep1);
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

		HttpURLConnection uc = getHttpURLConnection(siteUrl + pageUrl);

		String cookie = "SESSION_NAME="
				+ cookieSESSION_NAME
				+ (cookieSessionINT != null && cookieSessionINT.length() > 0 ? "; sessionINT="
						+ cookieSessionINT
						: "");
		uc.setRequestProperty("Cookie", cookie);
		System.out.println("Cookie: " + cookie);
		uc.setRequestProperty("Origin", siteUrl);
		uc.setRequestProperty("Referer", siteUrl + loginstep1);

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

	
	public static void fetchPupils(Document doc) {
		
		String pupil = doc.getElementsByAttributeValue("id", "name_uch").first().text();
	}
	
	public static void fetchLessons(Document doc) {

		Long tod = Long.parseLong(doc.getElementsByAttributeValue("id", "tod").first().attr("value"));
		Date today = new Date(tod*1000);
		Calendar cal = Calendar.getInstance();

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

				if (name.length() > 0)
					System.out.println("" + number + ". " + name + " " + date);
			}
		}	
	}
	
	public static String fetchLongCellString(Element e) {

		for (Element div : e.getElementsByTag("div")) {

			if (div.hasAttr("class") && div.attr("class").equals("com")) {
				return div.text();
			}
		}
		return e.text();
	}

	public static void fetchLessonsDetails(Document doc) {

		Long tod = Long.parseLong(doc.getElementsByAttributeValue("id", "tod").first().attr("value"));
		Date today = new Date(tod*1000);
		Calendar cal = Calendar.getInstance();

		cal.setTime(today);

		cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

		System.out.println("Date: " + today);

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

				if (name.length() > 0)
					System.out.println("" + number + ". " + cal.getTime() + " " + name + " " + homework + " " + mark + " " + comment);
			}
		}	
	}
	
	public static void fetchGrades(Document doc) {
		
	}


	public void getLessons() throws ParseException, MalformedURLException,
			IOException {

	}

	public void getLessons(Pupil p, Schedule s, Week w) throws ParseException,
			MalformedURLException, IOException {

	}

	public void getLessonsDetails() throws ParseException,
			MalformedURLException, IOException {

	}

	public void getLessonsDetails(Pupil p, Schedule s, Week w)
			throws ParseException, MalformedURLException, IOException {

	}

	public void getGrades() throws ParseException, MalformedURLException,
			IOException {

	}

	public void getGrades(Pupil p, Schedule s, GradeSemester sem)
			throws ParseException, MalformedURLException, IOException {

	}
}
