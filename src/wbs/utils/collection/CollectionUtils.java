package wbs.utils.collection;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

public
class CollectionUtils {

	public static <ValueType>
	ImmutableList <ValueType> singletonList (
			@NonNull ValueType item) {

		return ImmutableList.<ValueType> of (
			item);

	}

	public static <ValueType>
	ImmutableList <ValueType> immutableList (
			@NonNull Iterator <ValueType> iterator) {

		ImmutableList.Builder <ValueType> builder =
			ImmutableList.<ValueType> builder ();

		while (iterator.hasNext ()) {

			builder.add (
				iterator.next ());

		}

		return builder.build ();

	}

	public static <ValueType>
	ImmutableList <ValueType> listFromNullable (
			ValueType value) {

		if (value != null) {

			return ImmutableList.of (
				value);

		} else {

			return ImmutableList.of ();

		} 

	}

	public static
	boolean collectionIsEmpty (
			@NonNull Collection<?> collection) {

		return collection.isEmpty ();

	}

	public static
	boolean collectionIsNotEmpty (
			Collection <?> collection) {

		return ! collection.isEmpty ();

	}

	public static <Type>
	Type collectionAdd (
			@NonNull Collection <? super Type> list,
			@NonNull Type item) {

		list.add (
			item);

		return item;

	}

	public static
	long collectionSize (
			@NonNull Collection <?> collection) {

		return collection.size ();

	}

	public static
	boolean collectionSizeMoreThan (
			@NonNull Collection <?> collection,
			@NonNull Long size) {

		return collection.size () > size;

	}

	public static
	boolean collectionSizeNotMoreThan (
			@NonNull Collection <?> collection,
			@NonNull Long size) {

		return collection.size () <= size;

	}

	public static
	boolean collectionSizeLessThan (
			@NonNull Collection <?> collection,
			@NonNull Long size) {

		return collection.size () < size;

	}

	public static
	boolean collectionSizeNotLessThan (
			@NonNull Collection <?> collection,
			@NonNull Long size) {

		return collection.size () >= size;

	}

	public static
	long arrayLength (
			Object[] array) {

		return (long)
			array.length;

	}

	public static
	long arrayLength (
			byte[] array) {

		return (long)
			array.length;

	}

	public static <Type>
	long listIndexOfRequired (
			@NonNull List <Type> list,
			@NonNull Type value) {

		long index =
			list.indexOf (
				value);

		if (index < 0) {

			throw new IllegalArgumentException ();

		} else {

			return index;

		}

	}

	public static <Type>
	Type listItemAtIndexRequired (
			@NonNull List <Type> list,
			@NonNull Long index) {

		Type value =
			list.get (
				toJavaIntegerRequired (
					index));

		if (
			isNull (
				value)
		) {
			throw new NullPointerException ();
		}

		return value;

	}

	public static <Type>
	Type listFirstElementRequired (
			@NonNull List <Type> list) {

		return list.get (0);

	}

	public static <Type>
	Type listSecondElementRequired (
			@NonNull List <Type> list) {

		return list.get (1);

	}

	public static <Type>
	Type listThirdElementRequired (
			@NonNull List <Type> list) {

		return list.get (2);

	}

	public static <Type>
	Type listFourthElementRequired (
			@NonNull List <Type> list) {

		return list.get (4);

	}

	public static <Type>
	Type listLastItemRequired (
			@NonNull List <Type> list) {

		return list.get (
			list.size () - 1);

	}

	public static <Type>
	Type listSecondLastElementRequired (
			@NonNull List <Type> list) {

		return list.get (
			list.size () - 2);

	}

	public static <Type>
	Type listThirdLastElementRequired (
			@NonNull List <Type> list) {

		return list.get (
			list.size () - 3);

	}

	public static <Type>
	Type listFourthLastElementRequired (
			@NonNull List <Type> list) {

		return list.get (
			list.size () - 4);

	}

	// one element

	public static
	boolean collectionHasOneElement (
			@NonNull Collection <?> collection) {

		return collection.size () == 1;

	}

	public static
	boolean collectionDoesNotHaveOneElement (
			@NonNull Collection <?> collection) {

		return collection.size () != 1;

	}

	public static
	boolean collectionHasMoreThanOneElement (
			@NonNull Collection <?> collection) {

		return collection.size () > 1;

	}

	public static
	boolean collectionDoesNotHaveMoreThanOneElement (
			@NonNull Collection <?> collection) {

		return collection.size () <= 1;

	}

	public static
	boolean collectionHasLessThanOneElement (
			@NonNull Collection <?> collection) {

		return collection.size () < 1;

	}

	public static
	boolean collectionDoesNotHaveLessThanOneElement (
			@NonNull Collection <?> collection) {

		return collection.size () >= 1;

	}

	// two elements

	public static
	boolean collectionHasTwoElements (
			@NonNull Collection <?> collection) {

		return collection.size () == 2;

	}

	public static
	boolean collectionDoesNotHaveTwoElements (
			@NonNull Collection <?> collection) {

		return collection.size () != 2;

	}

	// three elements

	public static
	boolean collectionHasThreeElements (
			@NonNull Collection <?> collection) {

		return collection.size () == 3;

	}

	public static
	boolean collectionDoesNotHaveThreeElements (
			@NonNull Collection <?> collection) {

		return collection.size () != 3;

	}

	// four elements

	public static
	boolean collectionHasFourElements (
			@NonNull Collection <?> collection) {

		return collection.size () == 4;

	}

	// lists

	public static <Type>
	List <Type> listSlice (
			@NonNull List <Type> list,
			long startIndex,
			long endIndex) {

		return list.subList (
			toJavaIntegerRequired (
				startIndex),
			toJavaIntegerRequired (
				endIndex));

	}

	public static <Type>
	List <Type> listSliceAllButFirstItemRequired (
			@NonNull List <Type> list) {

		return list.subList (
			1,
			list.size ());

	}
	public static <Type>
	List <Type> listSliceAllButFirstTwoItemsRequired (
			@NonNull List <Type> list) {

		return list.subList (
			2,
			list.size ());

	}

	public static <Type>
	List <Type> listSliceAllButLastItemRequired (
			@NonNull List <Type> list) {

		return list.subList (
			0,
			list.size () - 1);

	}
	public static <Type>
	List <Type> listSliceAllButLastTwoItemsRequired (
			@NonNull List <Type> list) {

		return list.subList (
			0,
			list.size () - 2);

	}

	public static <Type>
	Type iterableFirstElementRequired (
			@NonNull Iterable <Type> iterable) {

		return iterable.iterator ().next ();

	}

	public static <Type extends Comparable <Type>>
	List <Type> listSorted (
			@NonNull Iterable <Type> iterable) {

		List <Type> sortedList =
			new ArrayList <Type> ();

		for (
			Type item
				: iterable
		) {
			sortedList.add (
				item);
		}

		Collections.sort (
			sortedList);

		return ImmutableList.copyOf (
			sortedList);

	}

	public static <Type>
	Stream <Type> collectionStream (
			@NonNull Collection <Type> collection) {

		return collection.stream ();

	}

	public static <Type>
	List <Type> emptyList () {

		return ImmutableList.of ();

	}

}
