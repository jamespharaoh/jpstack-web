package wbs.console.misc;

import java.util.Locale;

import lombok.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("timeFormatter")
public
class TimeFormatterImplementation
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
			@NonNull DateTimeZone timeZone,
			@NonNull Instant instant) {

		return timestampFormat
			.withZone (timeZone)
			.print (instant);

	}

	@Override
	public
	String instantToDateStringLong (
			@NonNull DateTimeZone timeZone,
			@NonNull Instant instant) {

		return longDateFormat
			.withZone (timeZone)
			.print (instant);

	}

	@Override
	public
	String instantToTimeString (
			@NonNull DateTimeZone timeZone,
			@NonNull Instant instant) {

		return timeFormat
			.withZone (timeZone)
			.print (instant);

	}

	@Override
	public
	String instantToDateStringShort (
			@NonNull DateTimeZone timeZone,
			@NonNull Instant instant) {

		return shortDateFormat
			.withZone (timeZone)
			.print (instant);

	}

	@Override
	public
	String instantToHttpTimestampString (
			@NonNull Instant instant) {

		return httpTimestampFormat
			.print (instant);

	}

	@Override
	public
	Instant timestampStringToInstant (
			@NonNull DateTimeZone timeZone,
			@NonNull String string) {

		return timestampFormat
			.withZone (timeZone)
			.parseDateTime (string)
			.toInstant ();

	}

	@Override
	public
	String localDateToDateString (
			@NonNull LocalDate localDate) {

		return shortDateFormat.print (
			localDate);

	}

	@Override
	public
	String dateTimeToTimestampTimezoneString (
			@NonNull DateTime dateTime) {

		return timestampTimezoneFormat.print (
			dateTime);

	}

	@Override
	public
	Optional<LocalDate> dateStringToLocalDate (
			@NonNull String string) {

		try {

			return Optional.of (
				shortDateFormat.parseLocalDate (
					string));

		} catch (IllegalArgumentException exception) {

			return Optional.absent ();

		}

	}

	@Override
	public
	LocalDate dateStringToLocalDateRequired (
			@NonNull String string) {

		return shortDateFormat.parseLocalDate (
			string);

	}

	@Override
	public
	DateTime timestampTimezoneToDateTime (
			@NonNull String string) {

		return timestampTimezoneFormat.parseDateTime (
			string);

	}

	@Override
	public
	DateTimeZone defaultTimezone () {

		return DateTimeZone.getDefault ();

	}

	@Override
	public
	DateTimeZone timezone (
			@NonNull String name) {

		return DateTimeZone.forID (
			name);

	}

}
