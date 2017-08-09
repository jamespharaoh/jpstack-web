package wbs.console.misc;

import com.google.common.base.Optional;

import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;

import wbs.framework.database.Transaction;

public
interface ConsoleUserHelper {

	Optional <Long> loggedInUserId ();
	Long loggedInUserIdRequired ();

	Long hourOffset (
			Transaction parentTransaction);

	DateTimeZone timezone (
			Transaction parentTransaction);

	String dateStringLong (
			Transaction parentTransaction,
			ReadableInstant timestamp);

	String dateStringShort (
			Transaction parentTransaction,
			ReadableInstant timestamp);

	String timeString (
			Transaction parentTransaction,
			ReadableInstant timestamp);

	String timestampWithTimezoneString (
			Transaction parentTransaction,
			ReadableInstant timestamp);

	String timestampWithoutTimezoneString (
			Transaction parentTransaction,
			ReadableInstant timestamp);

	String prettyDuration (
			Transaction parentTransaction,
			ReadableDuration duration);

	default
	String prettyDuration (
			Transaction parentTransaction,
			ReadableInstant startTime,
			ReadableInstant endTime) {

		return prettyDuration (
			parentTransaction,
			new Duration (
				startTime,
				endTime));

	}

	String timezoneString (
			Transaction parentTransaction,
			ReadableInstant timestamp);

}
