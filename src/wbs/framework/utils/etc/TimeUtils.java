package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.sql.Timestamp;
import java.util.Date;

import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import lombok.NonNull;

public
class TimeUtils {

	// ---------- instant construction

	public static
	String isoDate (
			@NonNull Instant instant) {

		return instant.toString (
			isoDateFormat);

	}

	public static
	Instant toInstant (
			@NonNull ReadableInstant readableInstant) {

		return readableInstant.toInstant ();

	}

	public static
	Instant toInstantNullSafe (
			ReadableInstant readableInstant) {

		return readableInstant != null
			? readableInstant.toInstant ()
			: null;

	}

	public static
	Instant dateToInstantNullSafe (
			Date date) {

		if (date == null)
			return null;

		return new Instant (
			date);

	}

	public static
	Instant millisToInstant (
			long millis) {

		return new Instant (
			millis);

	}

	public static
	Instant secondsToInstant (
			long seconds) {

		return new Instant (
			seconds * 1000);

	}

	public static
	Instant toInstant (
			@NonNull Timestamp timestamp) {

		return millisToInstant (
			timestamp.getTime ());

	}

	// ---------- instant conversion

	public static
	Date instantToDateNullSafe (
			ReadableInstant instant) {

		if (instant == null)
			return null;

		return instant.toInstant ().toDate ();

	}

	// ---------- instant comparison

	public static
	boolean earlierThan (
			@NonNull ReadableInstant left,
			@NonNull ReadableInstant right) {

		return left.isBefore (
			right);

	}

	public static
	boolean notEarlierThan (
			@NonNull ReadableInstant left,
			@NonNull ReadableInstant right) {

		return ! left.isBefore (
			right);

	}

	public static
	boolean laterThan (
			@NonNull ReadableInstant left,
			@NonNull ReadableInstant right) {

		return left.isAfter (
			right);

	}

	public static
	boolean notLaterThan (
			@NonNull ReadableInstant left,
			@NonNull ReadableInstant right) {

		return ! left.isAfter (
			right);

	}

	// ---------- duration construction

	public static
	Duration millisecondsToDuration (
			@NonNull Long milliseconds) {

		return new Duration (
			milliseconds);

	}

	// ---------- duration comparison

	public static
	boolean longerThan (
			@NonNull Duration left,
			@NonNull Duration right) {

		return left.isLongerThan (
			right);

	}

	public static
	boolean notLongerThan (
			@NonNull Duration left,
			@NonNull Duration right) {

		return ! left.isLongerThan (
			right);

	}

	public static
	boolean shorterThan (
			@NonNull Duration left,
			@NonNull Duration right) {

		return left.isShorterThan (
			right);

	}

	public static
	boolean notShorterThan (
			@NonNull Duration left,
			@NonNull Duration right) {

		return ! left.isShorterThan (
			right);

	}

	// ---------- local date construction

	public static
	LocalDate localDate (
			@NonNull ReadableInstant instant,
			@NonNull DateTimeZone timezone) {

		return instant

			.toInstant ()

			.toDateTime (
				timezone)

			.toLocalDate ();

	}

	public static
	Instant earliest (
			Instant... instants) {

		Instant earliest =
			null;

		for (
			Instant instant
				: instants
		) {

			if (

				earliest == null

				|| instant.isBefore (
					earliest)

			) {

				earliest =
					instant;

			}

		}

		return earliest;

	}

	public static
	boolean localDateEqual (
			@NonNull LocalDate date0,
			@NonNull LocalDate date1) {

		return date0.equals (
			date1);

	}

	public static
	boolean localDateNotEqual (
			@NonNull LocalDate date0,
			@NonNull LocalDate date1) {

		return ! date0.equals (
			date1);

	}

	public static
	void sleepDuration (
			@NonNull ReadableDuration duration)
		throws InterruptedException {

		Thread.sleep (
			duration.getMillis ());

	}

	public static
	void sleepUntil (
			@NonNull ReadableInstant endTime)
		throws InterruptedException {

		long millisToSleep = (
			+ endTime.getMillis ()
			- System.currentTimeMillis ());

		if (millisToSleep > 0) {

			Thread.sleep (
				millisToSleep);

		}

	}

	public static
	LocalTime localTime (
			long hour,
			long minute,
			long second,
			long millisecond) {

		return new LocalTime (

			toJavaIntegerRequired (
				hour),

			toJavaIntegerRequired (
				minute),

			toJavaIntegerRequired (
				second),

			toJavaIntegerRequired (
				millisecond)

		);

	}

	public static
	LocalTime localTime (
			long hour,
			long minute,
			long second) {

		return new LocalTime (

			toJavaIntegerRequired (
				hour),

			toJavaIntegerRequired (
				minute),

			toJavaIntegerRequired (
				second),

			0

		);

	}

	public static
	LocalTime localTime (
			long hour,
			long minute) {

		return new LocalTime (

			toJavaIntegerRequired (
				hour),

			toJavaIntegerRequired (
				minute),

			0,
			0

		);

	}

	public static
	LocalTime localTime (
			long hour) {

		return new LocalTime (

			toJavaIntegerRequired (
				hour),

			0,
			0,
			0

		);

	}

	public static
	long calculateAgeInYears (
			@NonNull LocalDate birthDate,
			@NonNull Instant now,
			@NonNull DateTimeZone timezone) {

		Years years =
			Years.yearsBetween (
				birthDate.toDateTimeAtStartOfDay (
					timezone),
				now);

		return years.getYears ();

	}

	public static
	DateTimeFormatter isoDateFormat =
		DateTimeFormat.forPattern (
			"yyyy-MM-dd'T'HH:mm:ss'Z'")

		.withZoneUTC ();

}
