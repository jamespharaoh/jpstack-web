package wbs.framework.hibernate;

import static wbs.utils.etc.OptionalUtils.optionalEqualOrNotPresentSafe;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.CustomType;
import org.hibernate.usertype.UserType;
import org.joda.time.LocalDate;

public
class DateUserType
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
			SharedSessionContractImplementor session,
			Object owner)
		throws SQLException {

		String stringValue =
			resultSet.getString (
				names [0]);

		if (resultSet.wasNull ())
			return null;

		return LocalDate.parse (
			stringValue);

	}

	@Override
	public
	void nullSafeSet (
			PreparedStatement statement,
			Object value,
			int index,
			SharedSessionContractImplementor session)
		throws SQLException {

		if (value == null) {

			statement.setNull (
				index,
				Types.VARCHAR);

			return;

		}

		LocalDate localDateValue =
			(LocalDate) value;

		statement.setString (
			index,
			localDateValue.toString ());

	}

	@Override
	public
	Class <?> returnedClass () {

		return LocalDate.class;

	}

	@Override
	public
	int[] sqlTypes () {

		return new int [] {
			Types.VARCHAR,
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
			new DateUserType ());

}
