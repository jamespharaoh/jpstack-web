package wbs.console.html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public
class ObsoleteDateField {

	private final static
	Pattern datePattern =
		Pattern.compile (
			"([0-9]{4})-([0-9]{2})-([0-9]{2})");

	public final static
	DateTimeFormatter dateFormatter =
		DateTimeFormat.forPattern (
			"yyyy-MM-dd");

	public final
	LocalDate date;

	public final
	String text;

	private
	ObsoleteDateField (
			LocalDate newDate) {

		date =
			newDate;

		text =
			dateFormatter.print (
				date);

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

		// if there is no input, use the present date

		if (
			input == null
			|| input.equals ("")
		) {

			return new ObsoleteDateField (
				LocalDate.now ());

		}

		// parse the date, if it fails just return the text but no date

		Matcher matcher =
			datePattern.matcher (input);

		if (! matcher.matches ())
			return new ObsoleteDateField (input);

		// ok, set the calendar with the given date and return that

		return new ObsoleteDateField (
			new LocalDate (
				Integer.valueOf (
					matcher.group (1)),
				Integer.valueOf (
					matcher.group (2)),
				Integer.valueOf (
					matcher.group (3))));

	}

}
