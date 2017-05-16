package wbs.utils.etc;

import static wbs.utils.collection.ArrayUtils.arrayStream;
import static wbs.utils.collection.IterableUtils.iterableStream;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

public
class NullUtils {

	@SafeVarargs
	public static <Type>
	Type ifNull (
			@NonNull Type... values) {

		for (Type value : values) {

			if (value != null)
				return value;

		}

		return null;

	}

	@SafeVarargs
	public static <Type>
	Type ifNull (
			@NonNull Supplier <Type> ... valueSuppliers) {

		for (
			Supplier <Type> valueSupplier
				: valueSuppliers
		) {

			Type value =
				valueSupplier.get ();

			if (value != null)
				return value;

		}

		return null;

	}

	public static <Type>
	Type ifNull (
			Type input,
			Type ifNull) {

		return input == null
			? ifNull
			: input;

	}

	@SafeVarargs
	public static <Type>
	Type ifNullThenRequired (
			@NonNull Supplier <? extends Type> ... valueSuppliers) {

		for (
			Supplier <? extends Type> valueSupplier
				: valueSuppliers
		) {

			Type value =
				valueSupplier.get ();

			if (value != null) {
				return value;
			}

		}

		throw new NullPointerException ();

	}

	public static
	<Type> Type nullIf (
			Type input,
			Type nullIf) {

		if (input == null)
			return null;

		if (nullIf != null && input.equals (nullIf))
			return null;

		return input;

	}

	public static
	void errorIfNull (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String name,
			Object value) {

		if (
			isNull (
				value)
		) {

			parentTaskLogger.errorFormat (
				"Parameter %s is null",
				name);

		}

	}

	public static <Type>
	Iterable <Type> filterNotNull (
			@NonNull Iterable <Type> iterable) {

		return () ->
			iterableStream (
				iterable)

			.filter (
				item ->
					item != null)

			.iterator ();

	}

	@SafeVarargs
	public static <OutType, InType extends OutType>
	Iterable <OutType> filterNotNull (
			@NonNull InType ... array) {

		return () ->
			arrayStream (
				array)

			.filter (
				item ->
					item != null)

			.map (
				item ->
					(OutType)
					item)

			.iterator ();

	}

	public static <Type>
	List <Type> filterNotNullToList (
			@NonNull Iterable <? extends Type> iterable) {

		return ImmutableList.<Type> copyOf (
			iterableStream (
				iterable)

			.filter (
				item ->
					item != null)

			.iterator ()

		);

	}

	@SuppressWarnings ("unchecked")
	public static <OutType, InType extends OutType>
	List <OutType> filterNotNullToList (
			@NonNull InType ... array) {

		return ImmutableList.<OutType> copyOf (
			arrayStream (
				array)

			.filter (
				item ->
					item != null)

			.iterator ()

		);

	}

	public static
	boolean isNull (
			Object object) {

		return object == null;

	}

	public static
	boolean isNotNull (
			Object object) {

		return object != null;

	}
	public static
	boolean anyIsNotNull (
			Object ... objects) {

		for (
			Object object
				: objects
		) {

			if (object != null) {
				return true;
			}

		}

		return false;

	}

}
