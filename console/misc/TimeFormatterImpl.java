package wbs.platform.console.misc;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("timeFormatter")
public
class TimeFormatterImpl
	implements TimeFormatter {

	public final static
	DateTimeFormatter timestampFormat =
		DateTimeFormat
			.forPattern ("yyyy-MM-dd HH:mm:ss")
			.withZone (DateTimeZone.getDefault ());

	public final static
	DateTimeFormatter timestampTimezoneFormat =
		DateTimeFormat
			.forPattern ("yyyy-MM-dd HH:mm:ss ZZZ")
			.withZone (DateTimeZone.getDefault ());

	public final static
	DateTimeFormatter timeFormat =
		DateTimeFormat
			.forPattern ("HH:mm:ss")
			.withZone (DateTimeZone.getDefault ());

	public final static
	DateTimeFormatter longDateFormat =
		DateTimeFormat
			.forPattern ("EEEE, d MMMM yyyy")
			.withZone (DateTimeZone.getDefault ());

	public final static
	DateTimeFormatter shortDateFormat =
		DateTimeFormat
			.forPattern ("yyyy-MM-dd")
			.withZone (DateTimeZone.getDefault ());

	public final static
	DateTimeFormatter httpTimestampFormat =
		DateTimeFormat
			.forPattern ("EEE, dd MMM yyyyy HH:mm:ss z")
			.withLocale (Locale.US)
			.withZoneUTC ();

	@Override
	public
	String instantToTimestampString (
			Instant instant) {

		if (instant == null)
			return null;

		return timestampFormat
			.print (instant);

	}

	@Override
	public
	String instantToDateStringLong (
			Instant instant) {

		if (instant == null)
			return null;

		return longDateFormat
			.print (instant);

	}

	@Override
	public
	String instantToTimeString (
			Instant instant) {

		if (instant == null)
			return null;

		return timeFormat
			.print (instant);

	}

	@Override
	public
	String instantToDateStringShort (
			Instant instant) {

		if (instant == null)
			return null;

		return shortDateFormat
			.print (instant);

	}

	@Override
	public
	String instantToHttpTimestampString (
			Instant instant) {

		if (instant == null)
			return null;

		return httpTimestampFormat
			.print (instant);

	}

	@Override
	public
	Instant timestampStringToInstant (
			String string) {

		return timestampFormat
			.parseDateTime (string)
			.toInstant ();

	}

	@Override
	public
	String localDateToDateString (
			LocalDate localDate) {

		if (localDate == null)
			return null;

		return shortDateFormat.print (
			localDate);

	}

	@Override
	public
	String dateTimeToTimestampTimezoneString (
			DateTime dateTime) {

		if (dateTime == null)
			return null;

		return timestampTimezoneFormat.print (
			dateTime);

	}

	@Override
	public
	LocalDate dateStringToLocalDate (
			String string) {

		return shortDateFormat.parseLocalDate (
			string);

	}

	@Override
	public
	DateTime timestampTimezoneToDateTime (
			String string) {

		return timestampTimezoneFormat.parseDateTime (
			string);

	}

}
