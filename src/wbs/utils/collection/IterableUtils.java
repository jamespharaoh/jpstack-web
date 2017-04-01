package wbs.utils.collection;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

public
class IterableUtils {

	public static
	long iterableCount (
			@NonNull Iterable <?> iterable) {

		long size = 0;

		for (
			@SuppressWarnings ("unused")
			Object _item
				: iterable
		) {
			size ++;
		}

		return size;

	}

	public static <InputType, OutputType>
	Iterable <OutputType> iterableMap (
			@NonNull Function <
				? super InputType,
				? extends OutputType
			> mapFunction,
			@NonNull Iterable <InputType> input) {

		List <OutputType> output =
			new ArrayList<> ();

		for (
			InputType inputItem
				: input
		) {

			output.add (
				mapFunction.apply (
					inputItem));

		}

		return output;

	}

	public static <InputType, OutputType>
	List <OutputType> iterableMapToList (
			@NonNull Function <
				? super InputType,
				? extends OutputType
			> mapFunction,
			@NonNull Iterable <InputType> input) {

		return ImmutableList.copyOf (
			iterableMap (
				mapFunction,
				input));

	}

	public static <InputType, OutputType>
	Set <OutputType> iterableMapToSet (
			@NonNull Function <
				? super InputType,
				? extends OutputType
			> mapFunction,
			@NonNull Iterable <InputType> input) {

		return ImmutableSet.copyOf (
			iterableMap (
				mapFunction,
				input));

	}

	public static <ItemType>
	Iterable <ItemType> iterableFilter (
			@NonNull Predicate <? super ItemType> predicate,
			@NonNull Iterable <ItemType> input) {

		return () ->
			iterableStream (input)

			.filter (
				predicate)

			.iterator ();

	}

	public static <ItemType>
	List <ItemType> iterableFilterToList (
			@NonNull Predicate <? super ItemType> predicate,
			@NonNull Iterable <ItemType> input) {

		return iterableStream (input)

			.collect (
				Collectors.toList ());

	}

	public static <ItemType>
	Stream <ItemType> iterableStream (
			@NonNull Iterable <ItemType> iterable) {

		return StreamSupport.stream (
			iterable.spliterator (),
			false);

	}

	public static <ItemType>
	Optional <ItemType> iterableFindFirst (
			@NonNull Predicate <ItemType> predicate,
			@NonNull Iterable <ItemType> iterable) {

		for (
			ItemType item
				: iterable
		) {

			if (
				predicate.test (
					item)
			) {

				return optionalOf (
					item);

			}

		}

		return optionalAbsent ();

	}

	public static <ItemType>
	ItemType iterableFindExactlyOneRequired (
			@NonNull Predicate <ItemType> predicate,
			@NonNull Iterable <ItemType> iterable) {

		Optional <ItemType> value =
			optionalAbsent ();

		for (
			ItemType item
				: iterable
		) {

			if (
				! predicate.test (
					item)
			) {
				continue;
			}

			if (
				optionalIsPresent (
					value)
			) {
				throw new IllegalArgumentException (
					"Multiple matching elements");
			}

			value =
				optionalOf (
					item);

		}

		if (
			optionalIsNotPresent (
				value)
		) {
			throw new IllegalArgumentException (
				"No matching element found");
		}

		return optionalGetRequired (
			value);

	}

}
