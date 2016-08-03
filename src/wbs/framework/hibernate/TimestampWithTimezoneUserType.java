package wbs.framework.hibernate;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.TimeUtils.toInstant;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.CustomType;
import org.hibernate.usertype.UserType;
import org.joda.time.Instant;
import org.joda.time.ReadableInstant;

public
class TimestampWithTimezoneUserType
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

		return equal (
			left,
			right);

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

		Timestamp timestamp =
			resultSet.getTimestamp (
				names [0]);

		if (resultSet.wasNull ())
			return null;

		Instant instant =
			toInstant (
				timestamp);

		return instant;

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
				Types.TIMESTAMP);

			return;

		}

		ReadableInstant instantValue =
			(ReadableInstant) value;

		statement.setObject (
			index,
			new Timestamp (
				instantValue.getMillis ()),
			Types.TIMESTAMP);

	}

	@Override
	public
	Class<?> returnedClass () {

		return Instant.class;

	}

	@Override
	public
	int[] sqlTypes () {

		return new int [] {
			Types.TIMESTAMP,
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
			new TimestampWithTimezoneUserType ());

}
