package com.shinymetal.gradereport.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.auth.InvalidCredentialsException;

import com.shinymetal.gradereport.objects.GradeSemester;
import com.shinymetal.gradereport.objects.Pupil;
import com.shinymetal.gradereport.objects.Schedule;
import com.shinymetal.gradereport.objects.Week;

public class BasicParser {

	protected final static String WHITESCPACE_CHARS = "" /*
														 * dummy empty string
														 * for homogeneity
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
	protected final static String WHITESPACE_CHARCLASS = "[" + WHITESCPACE_CHARS
			+ "]";
	/* A \S that actually works for Java’s native character set: Unicode */
	protected final static String NOT_WHITESPACE_CHARCLASS = "[^"
			+ WHITESCPACE_CHARS + "]";

	protected final static Pattern WHITESPACES_ONLY = Pattern.compile("^"
			+ WHITESPACE_CHARCLASS + "+$");

	protected volatile String mLogin;
	protected volatile String mPassword;
	
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

	public void setLogin(String login) {
		this.mLogin = login;
	}
	
	public String getLogin() {
		
		return mLogin;
	}

	public void setPassword(String password) {
		this.mPassword = password;
	}

	public boolean containsPrintableChars (String str) {

		if (str == null || str.length() <= 0)
			return false;

		Matcher matcher = WHITESPACES_ONLY.matcher(str.replaceAll("&nbsp;", " "));

		if (matcher.find())
			return false;
		
		return true;
	}
	
	public boolean loginSequence() throws MalformedURLException, IOException,
			InvalidCredentialsException {
		
		return false;
	}
	
	public void getLessons() throws ParseException, MalformedURLException, IOException {
		
	}
	
	public void getLessons(Pupil p, Schedule s, Week w)
			throws ParseException, MalformedURLException, IOException {
		
	}
	
	public void getLessonsDetails() throws ParseException, MalformedURLException, IOException {
		
	}
	
	public void getLessonsDetails(Pupil p, Schedule s, Week w)
			throws ParseException, MalformedURLException, IOException {
		
	}
	
	public void getGrades() throws ParseException, MalformedURLException, IOException {
		
	}
	
	public void getGrades(Pupil p, Schedule s, GradeSemester sem)
			throws ParseException, MalformedURLException, IOException {
		
	}
}
