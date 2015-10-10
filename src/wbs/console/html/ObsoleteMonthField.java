package wbs.console.html;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public
class ObsoleteMonthField {

	private final static
	Pattern datePattern =
		Pattern.compile ("([0-9]{4})-([0-9]{2})");

	public final static
	DateTimeFormatter dateFormatter =
		DateTimeFormat.forPattern ("yyyy-MM");

	public final
	LocalDate date;

	public final
	String text;

	private
	ObsoleteMonthField (
			LocalDate newDate) {

		date =
			newDate;

		text =
			dateFormatter.print (date);

	}

	private
	ObsoleteMonthField (
			String newText) {

		date = null;
		text = newText;

	}

	public static
	ObsoleteMonthField parse (
			String input) {

		Calendar calendar =
			Calendar.getInstance ();

		// if there is no input, use the present date

		if (input == null || input.equals ("")) {

			return new ObsoleteMonthField (
				LocalDate.now ());

		}

		// parse the date, if it fails just return the text but no date

		Matcher matcher =
			datePattern.matcher (input);

		if (! matcher.matches ())
			return new ObsoleteMonthField (input);

		// ok, set the calendar with the given date and return that

		calendar.clear ();

		calendar.set (
			Calendar.YEAR,
			Integer.valueOf (matcher.group (1)));

		calendar.set (
			Calendar.MONTH,
			Integer.valueOf (matcher.group (2)) - 1);

		return new ObsoleteMonthField (
			new LocalDate (
				Integer.valueOf (
					matcher.group (1)),
				Integer.valueOf (
					matcher.group (2)),
				1));

	}

}
