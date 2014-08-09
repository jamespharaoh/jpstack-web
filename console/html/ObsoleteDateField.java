package wbs.platform.console.html;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO this should use JodaTime
public
class ObsoleteDateField {

	private final static
	Pattern datePattern =
		Pattern.compile (
			"([0-9]{4})-([0-9]{2})-([0-9]{2})");

	public final static
	SimpleDateFormat dateFormat =
		new SimpleDateFormat (
			"yyyy-MM-dd");

	public final
	Date date;

	public final
	String text;

	private
	ObsoleteDateField (
			Date newDate) {

		date = newDate;
		text = dateFormat.format (date);

	}

	private
	ObsoleteDateField (
			String newText) {

		date = null;
		text = newText;

	}

	public static
	ObsoleteDateField parse (
			String input) {

		Calendar calendar =
			Calendar.getInstance ();

		// if there is no input, use the present date

		if (input == null || input.equals ("")) {

			calendar.set (Calendar.HOUR_OF_DAY, 0);
			calendar.set (Calendar.MINUTE, 0);
			calendar.set (Calendar.SECOND, 0);
			calendar.set (Calendar.MILLISECOND, 0);

			return new ObsoleteDateField (
				calendar.getTime ());

		}

		// parse the date, if it fails just return the text but no date

		Matcher matcher =
			datePattern.matcher (input);

		if (! matcher.matches ())
			return new ObsoleteDateField (input);

		// ok, set the calendar with the given date and return that

		calendar.clear ();

		calendar.set (
			Calendar.YEAR,
			Integer.valueOf (matcher.group (1)));

		calendar.set (
			Calendar.MONTH,
			Integer.valueOf (matcher.group (2)) - 1);

		calendar.set (
			Calendar.DATE,
			Integer.valueOf (matcher.group (3)));

		return new ObsoleteDateField (
			calendar.getTime ());

	}

	public static
	String format (
			Date date) {

		return dateFormat.format (
			date);

	}

}
