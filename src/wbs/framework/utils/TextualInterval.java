package wbs.framework.utils;

import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.lowercase;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.split;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.ReadableInstant;
import org.joda.time.YearMonth;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@Accessors (fluent = true)
@Value
public
class TextualInterval {

	String textualValue;
	Interval value;

	public static
	boolean validPartial (
			@NonNull String string) {

		if (
			in (
				lowercase (string),
				"today",
				"yesterday",
				"this month",
				"last month")
		) {
			return true;
		}

		for (
			Pattern pattern
				: partialPatterns
		) {

			Matcher matcher =
				pattern.matcher (
					string);

			if (matcher.matches ())
				return true;

		}

		return false;

	}

	public static
	boolean valid (
			@NonNull String string) {

		List<String> parts =
			split (
				string,
				" to ");

		if (parts.size () == 1) {

			return validPartial (
				string.trim ());

		} else if (parts.size () == 2) {

			return (

				validPartial (
					parts.get (0).trim ())

				&& validPartial (
					parts.get (1).trim ())

			);

		} else {

			return false;

		}

	}

	public static
	Optional<Interval> parsePartial (
			@NonNull DateTimeZone timeZone,
			@NonNull String string,
			@NonNull Integer hourOffset) {

		DateTime now =
			DateTime.now (
				timeZone);

		LocalDate today =
			now.getHourOfDay () >= hourOffset
				? now.toLocalDate ()
				: now.toLocalDate ().minusDays (1);

		YearMonth thisMonth =
			now.getHourOfDay () >= hourOffset || now.getDayOfMonth () > 1
				? new YearMonth (now)
				: new YearMonth (now).minusMonths (1);

		switch (lowercase (string)) {

		case "today":

			LocalDate tomorrow =
				today.plusDays (1);

			return Optional.of (
				new Interval (
					today.toDateTime (
						new LocalTime (hourOffset, 0),
						timeZone),
					tomorrow.toDateTime (
						new LocalTime (hourOffset, 0),
						timeZone)));

		case "yesterday":

			LocalDate yesterday =
				today.minusDays (1);

			return Optional.of (
				new Interval (
					yesterday.toDateTime (
						new LocalTime (hourOffset, 0),
						timeZone),
					today.toDateTime (
						new LocalTime (hourOffset, 0),
						timeZone)));

		case "this month":

			YearMonth nextMonth =
				thisMonth.plusMonths (1);

			return Optional.of (
				new Interval (
					thisMonth.toLocalDate (1).toDateTime (
						new LocalTime (hourOffset, 0),
						timeZone),
					nextMonth.toLocalDate (1).toDateTime (
						new LocalTime (hourOffset, 0),
						timeZone)));

		case "last month":

			YearMonth lastMonth =
				thisMonth.minusMonths (1);

			return Optional.of (
				new Interval (
					lastMonth.toLocalDate (1).toDateTime (
						new LocalTime (hourOffset, 0),
						timeZone),
					thisMonth.toLocalDate (1).toDateTime (
						new LocalTime (hourOffset, 0),
						timeZone)));

		}

		int fromYear = 0;
		int fromMonth = 1;
		int fromDate = 1;
		int fromHour = hourOffset;
		int fromMinute = 0;
		int fromSecond = 0;

		for (
			Pattern pattern
				: partialPatterns
		) {

			Matcher matcher =
				pattern.matcher (
					string);

			if (! matcher.matches ())
				continue;

			int groupCount =
				matcher.groupCount ();

			// work out time from

			if (groupCount >= 1) {

				fromYear =
					Integer.parseInt (
						matcher.group (1));

			}

			if (groupCount >= 2) {

				fromMonth =
					Integer.parseInt (
						matcher.group (2));

			}

			if (groupCount >= 3) {

				fromDate =
					Integer.parseInt (
						matcher.group (3));

			}

			if (groupCount >= 4) {

				fromHour =
					Integer.parseInt (
						matcher.group (4));

			}

			if (groupCount >= 5) {

				fromMinute =
					Integer.parseInt (
						matcher.group (5));

			}

			if (groupCount >= 6) {

				fromSecond =
					Integer.parseInt (
						matcher.group (6));

			}

			DateTime fromDateTime =
				new DateTime (
					fromYear,
					fromMonth,
					fromDate,
					fromHour,
					fromMinute,
					fromSecond,
					timeZone);

			// work out time to

			DateTime toDateTime;

			if (groupCount == 0) {

				toDateTime =
					fromDateTime.plusYears (
						10000);

			} else if (groupCount == 1) {

				toDateTime =
					fromDateTime.plusYears (
						1);

			} else if (groupCount == 2) {

				toDateTime =
					fromDateTime.plusMonths (
						1);

			} else if (groupCount == 3) {

				toDateTime =
					fromDateTime.plusDays (
						1);

			} else if (groupCount == 4) {

				toDateTime =
					fromDateTime.plusHours (
						1);

			} else if (groupCount == 5) {

				toDateTime =
					fromDateTime.plusMinutes (
						1);

			} else if (groupCount == 6) {

				toDateTime =
					fromDateTime.plusSeconds (
						1);

			} else {

				return Optional.absent ();

			}

			return Optional.of (
				new Interval (
					fromDateTime,
					toDateTime));

		}

		return Optional.absent ();

	}

	public static
	Optional<TextualInterval> parse (
			@NonNull DateTimeZone timeZone,
			@NonNull String string,
			@NonNull Integer hourOffset) {

		List<String> parts =
			split (
				string,
				" to ");

		if (parts.size () == 1) {

			Optional<Interval> optionalInterval =
				parsePartial (
					timeZone,
					string.trim (),
					hourOffset);

			if (
				isNotPresent (
					optionalInterval)
			) {
				return Optional.absent ();
			}

			return Optional.of (
				new TextualInterval (
					string.trim (),
					optionalInterval.get ()));

		} else if (parts.size () == 2) {

			Optional<Interval> optionalFirstInterval =
				parsePartial (
					timeZone,
					parts.get (0).trim (),
					hourOffset);

			if (
				isNotPresent (
					optionalFirstInterval)
			) {
				return Optional.absent ();
			}

			Optional<Interval> optionalSecondInterval =
				parsePartial (
					timeZone,
					parts.get (1).trim (),
					hourOffset);

			if (
				isNotPresent (
					optionalSecondInterval)
			) {
				return Optional.absent ();
			}

			return Optional.of (
				new TextualInterval (
					string.trim (),
					new Interval (
						optionalFirstInterval.get ().getStart (),
						optionalSecondInterval.get ().getEnd ())));

		} else {

			return Optional.absent ();

		}

	}

	public static
	TextualInterval parseRequired (
			@NonNull DateTimeZone timeZone,
			@NonNull String string,
			@NonNull Integer hourOffset) {

		return optionalRequired (
			parse (
				timeZone,
				string,
				hourOffset));

	}

	public static
	TextualInterval forInterval (
			@NonNull DateTimeZone timeZone,
			@NonNull Interval interval) {

		// TODO make this cleverer

		return new TextualInterval (
			stringFormat (
				"%s to %s",
				formatInstant (
					timeZone,
					interval.getStart ()),
				formatInstant (
					timeZone,
					interval.getEnd ())),
			interval);

	}

	public static
	String formatInstant (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		DateTime dateTime =
			new DateTime (
				instant,
				timeZone);

		return dateTime.toString (
			timestampFormat);

	}

	private final static
	DateTimeFormatter timestampFormat =
		DateTimeFormat

		.forPattern (
			"yyyy-MM-dd HH:mm:ss")

		.withZone (
			DateTimeZone.getDefault ());

	private final static
	List<Pattern> partialPatterns =
		ImmutableList.<Pattern>of (

		Pattern.compile (
			"([0-9]{4})-(0?[1-9]|1[0-2])-(0?[1-9]|[12][0-9]|3[01]) " +
			"([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])"),

		Pattern.compile (
			"([0-9]{4})-(0?[1-9]|1[0-2])-(0?[1-9]|[12][0-9]|3[01]) " +
			"([01][0-9]|2[0-3]):([0-5][0-9])"),

		Pattern.compile (
			"([0-9]{4})-(0?[1-9]|1[0-2])-(0?[1-9]|[12][0-9]|3[01]) " +
			"([01][0-9]|2[0-3])"),

		Pattern.compile (
			"([0-9]{4})-(0?[1-9]|1[0-2])-(0?[1-9]|[12][0-9]|3[01])"),

		Pattern.compile (
			"([0-9]{4})-(0?[1-9]|1[0-2])"),

		Pattern.compile (
			"([0-9]{4})"),

		Pattern.compile (
			"")

	);

}
