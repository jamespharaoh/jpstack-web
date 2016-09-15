package wbs.framework.hibernate;

import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.string.StringUtils.camelToUnderscore;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import wbs.utils.collection.ReversableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class EnumUserType <DatabaseType, JavaType extends Enum <?>>
	implements UserType {

	@Getter @Setter
	int sqlType;

	@Getter @Setter
	Class <JavaType> enumClass;

	private
	ReversableMap <DatabaseType, JavaType> keyToEnumMap =
		ReversableMap.makeHashed ();

	public
	EnumUserType () {
	}

	public
	Set <DatabaseType> databaseValues () {

		return keyToEnumMap.keySet ();

	}

	public
	Set <JavaType> javaValues () {

		return keyToEnumMap.valueSet ();

	}

	public
	void add (
			DatabaseType databaseValue,
			JavaType javaValue) {

		keyToEnumMap.put (
			databaseValue,
			javaValue);

	}

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

		return left == right;

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

		Object key =
			resultSet.getObject (
				names [0]);

		if (resultSet.wasNull ())
			return null;

		// TODO this is ugly

		if (
			sqlType == Types.BIGINT
			&& key instanceof Integer
		) {

			key =
				fromJavaInteger (
					(Integer)
					key);

		}

		JavaType ret =
			keyToEnumMap.get (key);

		if (ret == null) {

			ret =
				keyToEnumMap.get (
					key.toString ());

			if (ret == null) {

				throw new RuntimeException (
					stringFormat (
						"Unknown column value '%s' (%s) for %s",
						key.toString (),
						key.getClass ().getName (),
						enumClass.getSimpleName ()));

			}

		}

		return ret;

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
				sqlType);

			return;

		}

		Object key =
			keyToEnumMap.getKey (
				enumClass.cast (
					value));

		if (key == null)
			throw new RuntimeException ();

		statement.setObject (
			index,
			key,
			sqlType);

	}

	@Override
	public
	Class<?> returnedClass () {

		return enumClass;

	}

	@Override
	public
	int[] sqlTypes () {

		return new int [] {
			sqlType
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
			@NonNull Object value) {

		return keyToEnumMap

			.getKey (
				enumClass.cast (
					value))

			.hashCode ();

	}

	public
	EnumUserType <DatabaseType, JavaType> auto (
			@NonNull Class <DatabaseType> databaseClass) {

		for (
			JavaType value
				: enumClass.getEnumConstants ()
		) {

			add (
				databaseClass.cast (
					camelToUnderscore (
						value.name ())),
				value);

		}

		return this;

	}

}
