package wbs.utils.collection;

import static wbs.utils.collection.ArrayUtils.arrayStream;
import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.dynamicCast;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.google.common.collect.ImmutableMap;
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
			@NonNull Iterable <? extends InputType> iterable,
			@NonNull Function <
				? super InputType,
				OutputType
			> mapFunction) {

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
				input,
				mapFunction));

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

	public static <InLeft, InRight, OutKey, OutValue>
	Map <OutKey, OutValue> iterableMapToMap (
			@NonNull Iterable <Pair <InLeft, InRight>> input,
			@NonNull BiFunction <
				? super InLeft,
				? super InRight,
				? extends OutKey
			> keyFunction,
			@NonNull BiFunction <
				? super InLeft,
				? super InRight,
				? extends OutValue
			> valueFunction) {

		ImmutableMap.Builder <OutKey, OutValue> builder =
			ImmutableMap.builder ();

		for (
			Pair <InLeft, InRight> item
				: input
		) {

			builder.put (
				keyFunction.apply (
					item.getLeft (),
					item.getRight ()),
				valueFunction.apply (
					item.getLeft (),
					item.getRight ()));

		}

		return builder.build ();

	}

	public static <InputType, OutputType>
	Set <OutputType> iterableMapToSet (
			@NonNull Iterable <InputType> input,
			@NonNull Function <
				? super InputType,
				? extends OutputType
			> mapFunction) {

		return ImmutableSet.copyOf (
			iterableMap (
				input,
				mapFunction));

	}

	public static <Type>
	Set <Type> iterableToSet (
			@NonNull Iterable <Type> input) {

		return ImmutableSet.copyOf (
			input);

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
			@NonNull Iterable <ItemType> input,
			@NonNull Predicate <? super ItemType> predicate) {

		return iterableStream (input)

			.filter (
				predicate)

			.collect (
				Collectors.toList ());

	}

	public static <ItemType>
	Set <ItemType> iterableFilterToSet (
			@NonNull Iterable <ItemType> input,
			@NonNull Predicate <? super ItemType> predicate) {

		return iterableStream (input)

			.filter (
				predicate)

			.collect (
				Collectors.toSet ());

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

			.iterator ()

		;

	}

	public static <InputType, OutputType>
	List <OutputType> iterableFilterMapToList (
			@NonNull Iterable <? extends InputType> iterable,
			@NonNull Predicate <? super InputType> predicate,
			@NonNull Function <? super InputType, OutputType> mapping) {

		return Streams.stream (
			iterable)

			.filter (
				predicate)

			.map (
				mapping::apply)

			.collect (
				Collectors.toList ())

		;

	}

	public static <InputType, OutputType>
	Set <OutputType> iterableFilterMapToSet (
			@NonNull Iterable <? extends InputType> iterable,
			@NonNull Predicate <? super InputType> predicate,
			@NonNull Function <? super InputType, OutputType> mapping) {

		return Streams.stream (
			iterable)

			.filter (
				predicate)

			.map (
				mapping::apply)

			.collect (
				Collectors.toSet ())

		;

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
			@NonNull Iterable <ItemType> iterable,
			@NonNull Predicate <ItemType> predicate) {

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
	Optional <ItemType> iterableOnlyItem (
			@NonNull Iterable <ItemType> iterable) {

		Iterator <ItemType> iterator =
			iterable.iterator ();

		if (! iterator.hasNext ()) {
			return optionalAbsent ();
		}

		ItemType item =
			iterator.next ();

		if (iterator.hasNext ()) {
			throw new IllegalArgumentException ();
		}

		return optionalOf (
			item);

	}

	public static <InType, OutType extends InType>
	Optional <OutType> iterableOnlyItemByClass (
			@NonNull Iterable <InType> iterable,
			@NonNull Class <OutType> targetClass) {

		OutType foundItem =
			null;

		for (
			InType inItem
				: iterable
		) {

			Optional <OutType> outItemOptional =
				dynamicCast (
					targetClass,
					inItem);

			if (
				optionalIsNotPresent (
					outItemOptional)
			) {
				continue;
			}

			if (foundItem != null) {

				throw new IllegalArgumentException (
					"Multiple items matched");

			}

			foundItem =
				optionalGetRequired (
					outItemOptional);

		}

		return optionalFromNullable (
			foundItem);

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

	public static <InType, OutType>
	Iterable <OutType> iterableFilterByClass (
			@NonNull Iterable <InType> iterable,
			@NonNull Class <OutType> targetClass) {

		return () ->
			Streams.stream (
				iterable)

			.map (
				item ->
					dynamicCast (
						targetClass,
						item))

			.filter (
				optionalItem ->
					optionalIsPresent (
						optionalItem))

			.map (
				optionalItem ->
					optionalGetRequired (
						optionalItem))

			.iterator ()

		;

	}

	public static <Type>
	Iterable <Type> iterableChainArguments () {

		return emptyList ();

	}

	public static <Type>
	Iterable <Type> iterableChainArguments (
			@NonNull Iterable <Type> iterable0) {

		return iterable0;

	}

	@SafeVarargs
	public static <Type>
	Iterable <Type> iterableChainArguments (
			@NonNull Iterable <? extends Type> ... iterables) {

		return () ->
			arrayStream (
				iterables)

			.flatMap (
				iterable ->
					iterableStream (
						iterable))

			.map (
				item ->
					(Type) item)

			.iterator ();

	}

	public static <Type>
	Iterable <Type> iterableChain (
			@NonNull Iterable <Iterable <Type>> iterables) {

		return () ->
			iterableStream (
				iterables)

			.flatMap (
				iterable ->
					iterableStream (
						iterable))

			.iterator ();

	}

	public static <Type>
	List <Type> iterableChainToList (
			@NonNull Iterable <Iterable <Type>> iterables) {

		return iterableStream (
			iterables)

			.flatMap (
				iterable ->
					iterableStream (
						iterable))

			.collect (
				Collectors.toList ())

		;

	}

}
