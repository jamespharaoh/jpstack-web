package wbs.framework.utils.etc;

import java.sql.Timestamp;
import java.util.Date;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public
class TimeUtils {

	public static
	String isoDate (
			@NonNull Instant instant) {

		return instant.toString (
			isoDateFormat);

	}

	public static
	boolean earlierThan (
			@NonNull Instant left,
			@NonNull Instant right) {

		return left.isBefore (
			right);

	}

	public static
	boolean laterThan (
			@NonNull Instant left,
			@NonNull Instant right) {

		return left.isAfter (
			right);

	}

	public static
	boolean notShorterThan (
			@NonNull Duration left,
			@NonNull Duration right) {

		return left.isLongerThan (
			right);

	}

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
	Date instantToDateNullSafe (
			ReadableInstant instant) {

		if (instant == null)
			return null;

		return instant.toInstant ().toDate ();

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
	Timestamp toSqlTimestamp (
			@NonNull ReadableInstant instant) {

		return new Timestamp (
			instant.getMillis ());

	}

	public static
	Instant toInstant (
			@NonNull Timestamp timestamp) {

		return millisToInstant (
			timestamp.getTime ());

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
	DateTimeFormatter isoDateFormat =
		DateTimeFormat.forPattern (
			"yyyy-MM-dd'T'HH:mm:ss'Z'")

		.withZoneUTC ();

}
