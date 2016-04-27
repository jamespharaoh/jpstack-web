package wbs.framework.utils;

import lombok.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;

import com.google.common.base.Optional;

public 
interface TimezoneTimeFormatter {

	// details

	TimeFormatter timeFormatter ();
	DateTimeZone timezone ();

	// instant to string

	default
	String timestampWithoutTimezoneString (
			@NonNull ReadableInstant instant) {

		return timeFormatter ().timestampString (
			timezone (),
			instant);

	}

	default
	String timestampWithTimezoneString (
			@NonNull ReadableInstant instant) {

		return timeFormatter ().timestampTimezoneString (
			timezone (),
			instant);

	}

	default
	String dateStringLong (
			@NonNull ReadableInstant instant) {

		return timeFormatter ().dateStringLong (
			timezone (),
			instant);

	}

	default
	String timeString (
			@NonNull ReadableInstant instant) {

		return timeFormatter ().timeString (
			timezone (),
			instant);

	}

	default
	String dateStringShort (
			@NonNull ReadableInstant instant) {

		return timeFormatter ().dateStringShort (
			timezone (),
			instant);

	}

	default
	String httpTimestampString (
			@NonNull ReadableInstant instant) {

		return timeFormatter ().httpTimestampString (
			instant);

	}

	// string to instant

	default
	Instant timestampStringToInstant (
			@NonNull String string) {

		return timeFormatter ().timestampStringToInstant (
			timezone (),
			string);

	}

	// datetime to string

	default
	String timestampTimezoneString (
			@NonNull DateTime dateTime) {
	
		return timeFormatter ().timestampTimezoneString (
			dateTime);
	
	}

	default
	String timezoneString (
			@NonNull DateTime dateTime) {

		return timeFormatter ().timezoneString (
			dateTime);

	}

	// string to datetime

	default
	DateTime timestampTimezoneToDateTime (
			@NonNull String string) {

		return timeFormatter ().timestampTimezoneToDateTime (
			string);

	}

	// local date to string

	default
	String dateString (
			@NonNull LocalDate localDate) {

		return timeFormatter ().dateString (
			localDate);

	}

	// string to local date

	default
	Optional<LocalDate> dateStringToLocalDate (
			@NonNull String string) {

		return timeFormatter ().dateStringToLocalDate (
			string);

	}

	default
	LocalDate dateStringToLocalDateRequired (
			@NonNull String string) {

		return timeFormatter ().dateStringToLocalDateRequired (
			string);

	}

	// duration to string

	default
	String prettyDuration (
			@NonNull ReadableInstant start,
			@NonNull ReadableInstant end) {

		return timeFormatter ().prettyDuration (
			start,
			end);

	}

	default
	String prettyDuration (
			@NonNull ReadableDuration interval) {

		return timeFormatter ().prettyDuration (
			interval);

	}

}
