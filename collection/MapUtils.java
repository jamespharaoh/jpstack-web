package wbs.utils.collection;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

public
class MapUtils {

	// constructors

	public static <KeyType, ValueType>
	Map <KeyType, ValueType> immutableMap (
			@NonNull Iterator <
				? extends Map.Entry <
					? extends KeyType,
					? extends ValueType
				>
			> iterator) {

		ImmutableMap.Builder <KeyType, ValueType> builder =
			ImmutableMap.builder ();

		while (iterator.hasNext ()) {

			Map.Entry <
				? extends KeyType,
				? extends ValueType
			> entry =
				iterator.next ();

			builder.put (
				new AbstractMap.SimpleEntry <KeyType, ValueType> (
					entry.getKey (),
					entry.getValue ()));

		}

		return builder.build ();

	}

	// empty

	public static
	boolean mapIsEmpty (
			@NonNull Map <?, ?> map) {

		return map.isEmpty ();

	}

	public static
	boolean mapIsNotEmpty (
			@NonNull Map <?, ?> map) {

		return ! map.isEmpty ();

	}

	public static <KeyType, ValueType>
	Map <KeyType, ValueType> emptyMap () {

		return ImmutableMap.of ();

	}

	// key lookup

	public static <KeyType>
	boolean mapContainsKey (
			@NonNull Map <KeyType, ?> map,
			@NonNull KeyType key) {

		return map.containsKey (
			key);

	}

	// item lookup

	public static <KeyType, ValueType>
	Optional <ValueType> mapItemForKey (
			@NonNull Map <KeyType, ValueType> map,
			@NonNull KeyType key) {

		ValueType value =
			map.get (
				key);

		if (value != null) {

			return optionalOf (
				value);

		} else if (map.containsKey (key)) {

			throw new NullPointerException ();

		} else {

			return optionalAbsent ();

		}

	}

	public static <KeyType, ValueType>
	ValueType mapItemForKeyOrDefault (
			@NonNull Map <KeyType, ValueType> map,
			@NonNull KeyType key,
			@NonNull ValueType defaultValue) {

		ValueType value =
			map.get (
				key);

		if (value != null) {

			return value;

		} else if (map.containsKey (key)) {

			throw new NullPointerException ();

		} else {

			return defaultValue;

		}

	}

	public static <KeyType, ValueType>
	ValueType mapItemForKeyRequired (
			@NonNull Map <KeyType, ValueType> map,
			@NonNull KeyType key) {

		ValueType value =
			map.get (
				key);

		if (value != null) {

			return value;

		} else if (map.containsKey (key)) {

			throw new NullPointerException ();

		} else {

			throw new NoSuchElementException ();

		}

	}

	public static <Type>
	Type mapItemForKeyOrKey (
			@NonNull Map <Type, Type> map,
			@NonNull Type key) {

		Type value =
			map.get (
				key);

		if (value != null) {

			return value;

		} else if (map.containsKey (key)) {

			throw new NullPointerException ();

		} else {

			return key;

		}

	}

	public static <KeyType, ValueType>
	ValueType mapItemForKeyOrThrow (
			@NonNull Map <KeyType, ValueType> map,
			@NonNull KeyType key,
			@NonNull Supplier <RuntimeException> exceptionSupplier) {

		ValueType value =
			map.get (
				key);


		if (value != null) {

			return value;

		} else if (
			map.containsKey (
				key)
		) {

			throw new NullPointerException ();

		} else {

			throw exceptionSupplier.get ();

		}

	}

	public static <KeyType, ValueType>
	Map <KeyType, ValueType> mapWithDerivedKey (
			@NonNull Iterable <ValueType> values,
			@NonNull Function <
				? super ValueType,
				? extends KeyType
			> keyFunction) {

		ImmutableMap.Builder <KeyType, ValueType> builder =
			ImmutableMap.builder ();

		for (
			ValueType value
				: values
		) {

			builder.put (
				keyFunction.apply (
					value),
				value);

		}

		return builder.build ();

	}

	public static <KeyType, InValueType, OutValueType>
	Map <KeyType, OutValueType> mapWithDerivedKeyAndValue (
			@NonNull Iterable <? extends InValueType> values,
			@NonNull Function <
				? super InValueType,
				? extends KeyType
			> keyFunction,
			@NonNull Function <
				? super InValueType,
				? extends OutValueType
			> valueFunction) {

		ImmutableMap.Builder <KeyType, OutValueType> builder =
			ImmutableMap.builder ();

		for (
			InValueType value
				: values
		) {

			builder.put (
				keyFunction.apply (
					value),
				valueFunction.apply (
					value));

		}

		return builder.build ();

	}

	public static <KeyType, ValueType>
	void mapPutOrThrowIllegalStateException (
			@NonNull Map <KeyType, ValueType> map,
			@NonNull KeyType key,
			@NonNull ValueType value) {

		if (
			map.containsKey (
				key)
		) {
			throw new IllegalStateException ();
		}

		map.put (
			key,
			value);

	}

	// properties

	public static
	Properties mapToProperties (
			@NonNull Map <String, String> propertiesMap) {

		Properties properties =
			new Properties ();

		properties.putAll (
			propertiesMap);

		return properties;

	}

}
