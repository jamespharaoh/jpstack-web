package wbs.platform.php;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public interface PhpEntity {

	PhpType getType();

	boolean isBoolean();

	boolean isInteger();

	boolean isFloat();

	boolean isString();

	boolean isArray();

	boolean isObject();

	boolean isNormalArray();

	boolean isNull();

	boolean isNumber();

	boolean isScalar();

	/** Cast to a PHP boolean and return as a Boolean. */
	Boolean asBoolean();

	/** Cast to a PHP integer and return as an Integer. */
	Integer asInteger();

	/** Cast to a PHP integer and return as a Long. */
	Long asLong();

	/** Cast to a PHP float and return as a Double. */
	Double asDouble();

	/**
	 * Cast to a PHP string and return as a String (assumes iso-8859-1
	 * encoding).
	 */
	String asString();

	/** Cast to a PHP string and return as a String. */
	String asString (
			String encoding)
		throws UnsupportedEncodingException;

	/** Cast to a PHP string and return as a String (assumes utf-8 encoding). */
	String asStringUtf8();

	/** Cast to a PHP string and return as a byte[]. */
	byte[] asByteArray();

	/** Cast to a PHP array and return as a Map. */
	Map<Object, PhpEntity> asMap();

	/**
	 * Cast to a PHP array and return as an List. This fails unless the array
	 * contains consecutive integer indexes from 0 up.
	 */
	List<PhpEntity> asList();

	/**
	 * Cast to a PHP array and return as an Object[]. This fails unless the
	 * array contains consecutive integer indexes from 0 up.
	 */
	Object[] asArray();

	/** Cast to a PHP object and return as a Map. */
	Map<Object, PhpEntity> asObjectMap();

	/** Returns the most natural java representation for this object. */
	Object asObject();

	/** Returns an Integer, Long or Double for appropriate types. */
	Number asNumber();

	/** Cast to a PHP array and return the object denoted by key. */
	PhpEntity getAt(Object key);
}
