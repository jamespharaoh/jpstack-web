package wbs.framework.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;

import com.google.common.base.Optional;

public
interface TimeFormatter {

	// instant to string

	String timestampString (
			DateTimeZone timeZone,
			ReadableInstant instant);

	String timestampTimezoneString (
			DateTimeZone timeZone,
			ReadableInstant instant);

	String dateStringLong (
			DateTimeZone timeZone,
			ReadableInstant instant);

	String timeString (
			DateTimeZone timeZone,
			ReadableInstant instant);

	String dateStringShort (
			DateTimeZone timeZone,
			ReadableInstant instant);

	String httpTimestampString (
			ReadableInstant instant);

	// instant to iso string

	String timestampSecondStringIso (
			ReadableInstant instant);

	String timestampMinuteStringIso (
			ReadableInstant instant);

	String timestampHourStringIso (
			ReadableInstant instant);

	// string to instant

	Instant timestampStringToInstant (
			DateTimeZone timeZone,
			String string);

	// iso string to interval

	Interval isoStringToInterval (
			String isoString);

	// datetime to string

	String timestampTimezoneString (
			DateTime dateTime);

	String timezoneString (
			DateTime dateTime);

	// string to datetime

	DateTime timestampTimezoneToDateTime (
			String string);

	// local date to string

	String dateString (
			LocalDate localDate);

	// string to local date

	Optional<LocalDate> dateStringToLocalDate (
			String string);

	LocalDate dateStringToLocalDateRequired (
			String string);

	// duration to string

	String prettyDuration (
			ReadableInstant start,
			ReadableInstant end);

	String prettyDuration (
			ReadableDuration interval);

	// time zones

	DateTimeZone timezone (
			String name);

}
