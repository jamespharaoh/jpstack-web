package wbs.utils.collection;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

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
				OutputType
			> mapFunction,
			@NonNull Iterable <? extends InputType> iterable) {

		return () ->
			Streams.stream (
				iterable)

			.map (
				item ->
					mapFunction.apply (
						item))

			.iterator ();

	}

	public static <InLeftType, InRightType, OutType>
	Iterable <OutType> iterableMap (
			@NonNull BiFunction <
				? super InLeftType,
				? super InRightType,
				OutType
			> mapFunction,
			@NonNull Iterable <Pair <InLeftType, InRightType>> iterable) {

		return () ->
			Streams.stream (
				iterable)

			.map (
				pair ->
					mapFunction.apply (
						pair.getLeft (),
						pair.getRight ()))

			.iterator ();

	}

	public static <InputType, OutputType>
	List <OutputType> iterableMapToList (
			@NonNull Iterable <InputType> input,
			@NonNull Function <
				? super InputType,
				? extends OutputType
			> mapFunction) {

		return ImmutableList.copyOf (
			iterableMap (
				mapFunction,
				input));

	}

	public static <InputLeftType, InputRightType, OutputType>
	List <OutputType> iterableMapToList (
			@NonNull Iterable <Pair <InputLeftType, InputRightType>> input,
			@NonNull BiFunction <
				? super InputLeftType,
				? super InputRightType,
				? extends OutputType
			> mapFunction) {

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
			iterableStream (
				input)

			.filter (
				predicate)

			.iterator ();

	}

	public static <LeftType, RightType>
	Iterable <Pair <LeftType, RightType>> iterableFilter (
			@NonNull BiPredicate <
				? super LeftType,
				? super RightType
			> predicate,
			@NonNull Iterable <Pair <LeftType, RightType>> input) {

		return () ->
			iterableStream (
				input)

			.filter (
				pair ->
					predicate.test (
						pair.getLeft (),
						pair.getRight ()))

			.iterator ();

	}

	public static <ItemType>
	List <ItemType> iterableFilterToList (
			@NonNull Predicate <? super ItemType> predicate,
			@NonNull Iterable <ItemType> input) {

		return iterableStream (input)

			.filter (
				predicate)

			.collect (
				Collectors.toList ());

	}

	public static <InputType, OutputType>
	Iterable <OutputType> iterableFilterMap (
			@NonNull Iterable <? extends InputType> iterable,
			@NonNull Predicate <? super InputType> predicate,
			@NonNull Function <? super InputType, OutputType> mapping) {

		return () ->
			Streams.stream (
				iterable)

			.filter (
				predicate)

			.map (
				mapping::apply)

			.iterator ();

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

	public static <ItemType>
	ItemType iterableOnlyItemRequired (
			@NonNull Iterable <ItemType> iterable) {

		Iterator <ItemType> iterator =
			iterable.iterator ();

		if (! iterator.hasNext ()) {
			throw new IllegalArgumentException ();
		}

		ItemType item =
			iterator.next ();

		if (iterator.hasNext ()) {
			throw new IllegalArgumentException ();
		}

		return item;

	}

}
