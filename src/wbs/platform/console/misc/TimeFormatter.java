package wbs.platform.console.misc;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

public
interface TimeFormatter {

	String instantToTimestampString (
			Instant instant);

	String instantToDateStringLong (
			Instant instant);

	String instantToTimeString (
			Instant instant);

	String instantToDateStringShort (
			Instant instant);

	String instantToHttpTimestampString (
			Instant instant);

	Instant timestampStringToInstant (
			String string);

	String dateTimeToTimestampTimezoneString (
			DateTime dateTime);

	String localDateToDateString (
			LocalDate localDate);

	LocalDate dateStringToLocalDate (
			String string);

	DateTime timestampTimezoneToDateTime (
			String string);

}
