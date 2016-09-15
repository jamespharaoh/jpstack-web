package wbs.framework.hibernate;

import static wbs.utils.string.StringUtils.camelToUnderscore;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import java.util.Set;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import wbs.utils.collection.ReversableMap;

public
class HibernateEnumType<EnumType extends Enum<?>>
	implements
		ParameterizedType,
		UserType {

	private
	Class<EnumType> enumClass;

	private
	ReversableMap<String,EnumType> keyToEnumMap =
		ReversableMap.<String,EnumType>makeHashed ();

	public
	HibernateEnumType () {
	}

	public
	Set<String> databaseValues () {
		return keyToEnumMap.keySet ();
	}

	public
	Set<EnumType> javaValues () {
		return keyToEnumMap.valueSet ();
	}

	public
	void add (
			String databaseValue,
			EnumType enumValue) {

		keyToEnumMap.put (
			databaseValue,
			enumValue);

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

		EnumType ret =
			keyToEnumMap.get (key);

		if (ret == null) {

			ret =
				keyToEnumMap.get (
					key.toString ());

			if (ret == null) {

				throw new RuntimeException (
					stringFormat (
						"Unknown column value '%s' (%s)",
						key.toString (),
						key.getClass ().getName ()));

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
				Types.OTHER);

			return;

		}

		Object key =
			keyToEnumMap.getKey (
				enumClass.cast (value));

		if (key == null)
			throw new RuntimeException ();

		statement.setObject (
			index,
			key,
			Types.OTHER);

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
			1111
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

		return keyToEnumMap

			.getKey (
				enumClass.cast (
					value))

			.hashCode ();

	}

	@Override
	public
	void setParameterValues (
			Properties parameters) {

		String enumClassName =
			parameters.getProperty (
				"enumClass");

		try {

			@SuppressWarnings ("unchecked")
			Class<EnumType> enumClassTemp =
				(Class<EnumType>)
				Class.forName (
					enumClassName);

			enumClass =
				enumClassTemp;

		} catch (ClassNotFoundException exception) {

			throw new RuntimeException (
				stringFormat (
					"Enum class not found: %s",
					enumClassName));

		}

		for (
			EnumType enumValue
				: enumClass.getEnumConstants ()
		) {

			add (
				camelToUnderscore (
					enumValue.name ()),
				enumValue);

		}

	}

}
