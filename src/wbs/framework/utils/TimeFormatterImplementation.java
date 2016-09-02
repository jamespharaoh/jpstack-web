package wbs.framework.utils;

import static wbs.framework.utils.etc.NumberUtils.lessThanZero;
import static wbs.framework.utils.etc.StringUtils.pluralise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.TimeUtils.millisecondsToDuration;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Optional;

import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("timeFormatter")
public
class TimeFormatterImplementation
	implements TimeFormatter {

	// implementation

	@Override
	public
	String timestampString (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		return timestampFormat
			.withZone (timeZone)
			.print (instant);

	}

	@Override
	public
	String timestampTimezoneString (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		return timestampTimezoneFormat
			.withZone (timeZone)
			.print (instant);

	}

	@Override
	public
	String dateStringLong (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		return longDateFormat
			.withZone (timeZone)
			.print (instant);

	}

	@Override
	public
	String timeString (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		return timeFormat
			.withZone (timeZone)
			.print (instant);

	}

	@Override
	public
	String dateStringShort (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		return shortDateFormat
			.withZone (timeZone)
			.print (instant);

	}

	@Override
	public
	String httpTimestampString (
			@NonNull ReadableInstant instant) {

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
	String timestampHourStringIso (
			@NonNull ReadableInstant instant) {

		return timestampHourIsoFormat.print (
			instant);

	}

	@Override
	public
	String timestampMinuteStringIso (
			@NonNull ReadableInstant instant) {

		return timestampMinuteIsoFormat.print (
			instant);

	}

	@Override
	public
	String timestampSecondStringIso (
			@NonNull ReadableInstant instant) {

		return timestampSecondIsoFormat.print (
			instant);

	}

	@Override
	public
	Interval isoStringToInterval (
			@NonNull String isoString) {

		switch (isoString.length ()) {

		case 14:

			Instant startOfHour =
				timestampHourIsoFormat.parseDateTime (
					isoString)

				.toInstant ();

			Instant endOfHour =
				startOfHour.plus (
					Duration.standardHours (1));

			return new Interval (
				startOfHour,
				endOfHour);

		default:

			throw new RuntimeException (
				stringFormat (
					"Don't understand how to parse '%s' (length is %s)",
					isoString,
					isoString.length ()));

		}

	}

	@Override
	public
	String dateString (
			@NonNull LocalDate localDate) {

		return shortDateFormat.print (
			localDate);

	}

	@Override
	public
	String timestampTimezoneString (
			@NonNull DateTime dateTime) {

		return timestampTimezoneFormat.print (
			dateTime);

	}

	@Override
	public
	String timezoneString (
			@NonNull DateTime dateTime) {

		return timezoneFormat.print (
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

	// duration

	@Override
	public
	String prettyDuration (
			@NonNull ReadableInstant start,
			@NonNull ReadableInstant end) {

		return prettyDuration (
			new Duration (
				start,
				end));

	}

	@Override
	public
	String prettyDuration (
			@NonNull ReadableDuration duration) {

		long milliseconds =
			duration.getMillis ();

		if (
			lessThanZero (
				milliseconds)
		) {

			return stringFormat (
				"-%s",
				prettyDuration (
					millisecondsToDuration (
						- milliseconds)));
		}

		if (milliseconds < 2 * 1000L) {

			return pluralise (
				milliseconds,
				"millisecond");

		} else if (milliseconds < 2 * 60000L) {

			return pluralise (
				milliseconds / 1000L,
				"second");

		} else if (milliseconds < 2 * 3600000L) {

			return pluralise (
				milliseconds / 60000L,
				"minute");

		} else if (milliseconds < 2 * 86400000L) {

			return pluralise (
				milliseconds / 3600000L,
				"hour");

		} else if (milliseconds < 2 * 2678400000L) {

			return pluralise (
				milliseconds / 86400000L,
				"day");

		} else if (milliseconds < 2 * 31557600000L) {

			return pluralise (
				milliseconds / 2592000000L,
				"month");

		} else {

			return pluralise (
				milliseconds / 31556736000L,
				"year");

		}

	}

	// time zone

	@Override
	public
	DateTime timestampTimezoneToDateTime (
			@NonNull String string) {

		return timestampTimezoneFormat.parseDateTime (
			string);

	}

	/*
	@Override
	public
	DateTimeZone defaultTimezone () {

		return timezone (
			ifNull (
				wbsConfig.defaultTimezone (),
				DateTimeZone.getDefault ().getID ()));

	}
	*/

	@Override
	public
	DateTimeZone timezone (
			@NonNull String name) {

		return DateTimeZone.forID (
			name);

	}

	// data

	public final static
	DateTimeFormatter timestampFormat =

		DateTimeFormat.forPattern (
			"yyyy-MM-dd HH:mm:ss");

	public final static
	DateTimeFormatter timestampTimezoneFormat =

		DateTimeFormat.forPattern (
			"yyyy-MM-dd HH:mm:ss z");

	public final static
	DateTimeFormatter timeFormat =

		DateTimeFormat.forPattern (
			"HH:mm:ss");

	public final static
	DateTimeFormatter longDateFormat =

		DateTimeFormat.forPattern (
			"EEEE, d MMMM yyyy");

	public final static
	DateTimeFormatter shortDateFormat =

		DateTimeFormat.forPattern (
			"yyyy-MM-dd");

	public final static
	DateTimeFormatter timezoneFormat =

		DateTimeFormat.forPattern (
			"z");

	public final static
	DateTimeFormatter httpTimestampFormat =

		DateTimeFormat.forPattern (
			"EEE, dd MMM yyyyy HH:mm:ss z")

		.withLocale (
			Locale.US)

		.withZoneUTC ();

	public final static
	DateTimeFormatter timestampHourIsoFormat =
		DateTimeFormat.forPattern (
			"yyyy-MM-dd'T'HH'Z'")

		.withZoneUTC ();

	public final static
	DateTimeFormatter timestampMinuteIsoFormat =
		DateTimeFormat.forPattern (
			"yyyy-MM-dd'T'HH::mm'Z'")

		.withZoneUTC ();

	public final static
	DateTimeFormatter timestampSecondIsoFormat =
		DateTimeFormat.forPattern (
			"yyyy-MM-dd'T'HH:mm:ss'Z'")

		.withZoneUTC ();

}
