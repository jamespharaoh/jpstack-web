package wbs.framework.hibernate;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.sum;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalEqualOrNotPresentSafe;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.millisecondsToDuration;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.CustomType;
import org.hibernate.usertype.UserType;
import org.joda.time.Duration;
import org.joda.time.ReadableDuration;
import org.postgresql.util.PGInterval;

public
class DurationUserType
	implements UserType {

	@Override
	public
	Object deepCopy (
			Object value) {

		return value;

	}

	@Override
	public
	boolean equals (
			Object left,
			Object right) {

		return optionalEqualOrNotPresentSafe (
			optionalFromNullable (
				left),
			optionalFromNullable (
				right));

	}

	@Override
	public
	boolean isMutable () {

		return false;

	}

	@Override
	public
	Object nullSafeGet (
			ResultSet resultSet,
			String[] names,
			SessionImplementor session,
			Object owner)
		throws SQLException {

		String stringValue =
			resultSet.getString (
				names [0]);

		if (resultSet.wasNull ())
			return null;

		Matcher hoursMinutesSecondsMatcher =
			hoursMinutesSecondsRegex.matcher (
				stringValue);

		Matcher hoursMinutesSecondsDecisecondsMatcher =
			hoursMinutesSecondsDecisecondsRegex.matcher (
				stringValue);

		Matcher hoursMinutesSecondsCentisecondsMatcher =
			hoursMinutesSecondsCentisecondsRegex.matcher (
				stringValue);

		Matcher hoursMinutesSecondsMillisecondsMatcher =
			hoursMinutesSecondsMillisecondsRegex.matcher (
				stringValue);

		Matcher hoursMinutesSecondsMicrosecondsMatcher =
			hoursMinutesSecondsMillisecondsRegex.matcher (
				stringValue);

		if (hoursMinutesSecondsMatcher.matches ()) {

			return millisecondsToDuration (
				sum (
					3600000l * parseIntegerRequired (
						hoursMinutesSecondsMatcher.group (1)),
					60000l * parseIntegerRequired (
						hoursMinutesSecondsMatcher.group (2)),
					1000l * parseIntegerRequired (
						hoursMinutesSecondsMatcher.group (3))));

		} else if (hoursMinutesSecondsDecisecondsMatcher.matches ()) {

			return millisecondsToDuration (
				sum (
					3600000l * parseIntegerRequired (
						hoursMinutesSecondsDecisecondsMatcher.group (1)),
					60000l * parseIntegerRequired (
						hoursMinutesSecondsDecisecondsMatcher.group (2)),
					1000l * parseIntegerRequired (
						hoursMinutesSecondsDecisecondsMatcher.group (3)),
					100l * parseIntegerRequired (
						hoursMinutesSecondsDecisecondsMatcher.group (4))));

		} else if (hoursMinutesSecondsCentisecondsMatcher.matches ()) {

			return millisecondsToDuration (
				sum (
					3600000l * parseIntegerRequired (
						hoursMinutesSecondsCentisecondsMatcher.group (1)),
					60000l * parseIntegerRequired (
						hoursMinutesSecondsCentisecondsMatcher.group (2)),
					1000l * parseIntegerRequired (
						hoursMinutesSecondsCentisecondsMatcher.group (3)),
					10l * parseIntegerRequired (
						hoursMinutesSecondsCentisecondsMatcher.group (4))));

		} else if (hoursMinutesSecondsMillisecondsMatcher.matches ()) {

			return millisecondsToDuration (
				sum (
					3600000l * parseIntegerRequired (
						hoursMinutesSecondsMillisecondsMatcher.group (1)),
					60000l * parseIntegerRequired (
						hoursMinutesSecondsMillisecondsMatcher.group (2)),
					1000l * parseIntegerRequired (
						hoursMinutesSecondsMillisecondsMatcher.group (3)),
					parseIntegerRequired (
						hoursMinutesSecondsMillisecondsMatcher.group (4))));

		} else if (hoursMinutesSecondsMicrosecondsMatcher.matches ()) {

			return millisecondsToDuration (
				sum (
					3600000l * parseIntegerRequired (
						hoursMinutesSecondsMicrosecondsMatcher.group (1)),
					60000l * parseIntegerRequired (
						hoursMinutesSecondsMicrosecondsMatcher.group (2)),
					1000l * parseIntegerRequired (
						hoursMinutesSecondsMicrosecondsMatcher.group (3)),
					parseIntegerRequired (
						hoursMinutesSecondsMicrosecondsMatcher.group (4)),
					ifThenElse (
						moreThan (
							parseIntegerRequired (
								hoursMinutesSecondsMicrosecondsMatcher.group (5)),
							499l),
						() -> 1l,
						() -> 0l)));

		} else {

			throw new RuntimeException (
				stringFormat (
					"Unable to parse interval: %s",
					stringValue));

		}

	}

	@Override
	public
	void nullSafeSet (
			PreparedStatement statement,
			Object value,
			int index,
			SessionImplementor session)
		throws SQLException {

		if (value == null) {

			statement.setNull (
				index,
				Types.OTHER);

			return;

		}

		ReadableDuration durationValue =
			(ReadableDuration) value;

		PGInterval databaseValue =
			new PGInterval (
				0,
				0,
				0,
				0,
				0,
				durationValue.getMillis () / 1000d);

		statement.setObject (
			index,
			databaseValue,
			Types.OTHER);

	}

	@Override
	public
	Class <?> returnedClass () {

		return Duration.class;

	}

	@Override
	public
	int[] sqlTypes () {

		return new int [] {
			Types.OTHER,
		};

	}

	@Override
	public
	Object replace (
			Object original,
			Object target,
			Object owner) {

		return original;

	}

	@Override
	public
	Object assemble (
			Serializable cached,
			Object owner) {

		return cached;

	}

	@Override
	public
	Serializable disassemble (
			Object value) {

		return (Serializable) value;

	}

	@Override
	public
	int hashCode (
			Object value) {

		return value.hashCode ();

	}

	public final static
	CustomType INSTANCE =
		new CustomType (
			new DurationUserType ());

	private final static
	Pattern hoursMinutesSecondsRegex =
		Pattern.compile (
			"^(\\d{2}):(\\d{2}):(\\d{2})$");

	private final static
	Pattern hoursMinutesSecondsDecisecondsRegex =
		Pattern.compile (
			"^(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{1})$");

	private final static
	Pattern hoursMinutesSecondsCentisecondsRegex =
		Pattern.compile (
			"^(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{2})$");

	private final static
	Pattern hoursMinutesSecondsMillisecondsRegex =
		Pattern.compile (
			"^(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})$");

	private final static
	Pattern hoursMinutesSecondsMicrosecondsRegex =
		Pattern.compile (
			"^(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})(\\d{3})$");

}
