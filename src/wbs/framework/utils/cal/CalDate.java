package wbs.framework.utils.cal;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;

// TODO use LocalDate
public
class CalDate {

	private static
	TimeZone gmt =
		TimeZone.getTimeZone ("GMT");

	private final
	int year, month, day;

	public
	CalDate (
			int year,
			int month,
			int day) {

		this.year = year;
		this.month = month;
		this.day = day;

		// check it's valid

		Calendar cal =
			new GregorianCalendar (gmt);

		cal.setLenient (
			false);

		cal.set (
			Calendar.YEAR,
			year);

		cal.set (
			Calendar.MONTH,
			month - 1);

		cal.set (
			Calendar.DAY_OF_MONTH,
			day);

		cal.getTime ().getTime ();

	}

	public
	int getDay () {
		return day;
	}

	public
	int getMonth () {
		return month;
	}

	public
	int getYear () {
		return year;
	}

	@Override
	public
	String toString () {

		return String.format (
			"%04d-%02d-%02d",
			year,
			month,
			day);

	}

	private static
	Pattern ymdPattern =
		Pattern.compile (
			"(\\d{4})-(\\d{2})-(\\d{2})");

	public static
	CalDate parseYmd (
			String ymd) {

		Matcher matcher =
			ymdPattern.matcher (
				ymd);

		if (! matcher.matches ()) {

			throw new RuntimeException (
				"Invalid date");

		}

		return new CalDate (
			Integer.parseInt (
				matcher.group (1)),
			Integer.parseInt (
				matcher.group (2)),
			Integer.parseInt (
				matcher.group (3)));

	}

	public
	long localStart () {

		GregorianCalendar calendar =
			new GregorianCalendar (
				year,
				month - 1,
				day);

		return calendar.getTime ().getTime ();

	}

	public
	Date localStartJava () {

		return new Date (
			localStart ());

	}

	public
	long localEnd () {

		GregorianCalendar calendar =
			new GregorianCalendar (
				year,
				month - 1,
				day);

		calendar.add (
			Calendar.DAY_OF_MONTH,
			1);

		return calendar.getTime ().getTime ();

	}

	public
	Date localEndJava () {

		return new Date (
			localEnd ());

	}

	public static
	CalDate fromJava (
			Date date) {

		if (date == null)
			return null;

		GregorianCalendar calendar =
			new GregorianCalendar ();

		calendar.setTime (
			date);

		return new CalDate (
			calendar.get (Calendar.YEAR),
			calendar.get (Calendar.MONTH) + 1,
			calendar.get (Calendar.DAY_OF_MONTH));

	}

	public static
	Integer age (
			TimeZone timezone,
			CalDate birth,
			CalDate when) {

		if (birth == null)
			return null;

		if (when == null)
			return null;

		int age =
			+ when.year
			- birth.year;

		if (when.month < birth.month)
			age --;

		if (when.month == birth.month
				&& when.day < birth.day)
			age--;

		return age;

	}

	public static
	CalDate forLocalDate (
			LocalDate dob) {

		if (dob == null)
			return null;

		return new CalDate (
			dob.getYear (),
			dob.getMonthOfYear (),
			dob.getDayOfMonth ());

	}

	public
	LocalDate toLocalDate () {

		return new LocalDate (
			year,
			month,
			day);

	}

}
