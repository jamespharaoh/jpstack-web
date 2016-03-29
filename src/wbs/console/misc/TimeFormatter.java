package wbs.console.misc;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;

public
interface TimeFormatter {

	// instant to string

	String instantToTimestampString (
			DateTimeZone timeZone,
			Instant instant);

	String instantToTimestampTimezoneString (
			DateTimeZone timeZone,
			Instant instant);

	String instantToDateStringLong (
			DateTimeZone timeZone,
			Instant instant);

	String instantToTimeString (
			DateTimeZone timeZone,
			Instant instant);

	String instantToDateStringShort (
			DateTimeZone timeZone,
			Instant instant);

	String instantToHttpTimestampString (
			Instant instant);

	// string to instant

	Instant timestampStringToInstant (
			DateTimeZone timeZone,
			String string);

	// datetime to string

	String dateTimeToTimestampTimezoneString (
			DateTime dateTime);

	String dateTimeToTimezoneString (
			DateTime dateTime);

	// string to datetime

	DateTime timestampTimezoneToDateTime (
			String string);

	// local date to string

	String localDateToDateString (
			LocalDate localDate);

	// string to local date

	Optional<LocalDate> dateStringToLocalDate (
			String string);

	LocalDate dateStringToLocalDateRequired (
			String string);

	// time zones

	DateTimeZone defaultTimezone ();

	DateTimeZone timezone (
			String name);

}
