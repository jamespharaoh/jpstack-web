package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.Collection;
import java.util.List;

import lombok.NonNull;

public
class CollectionUtils {

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
	Type listLastElementRequired (
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
	boolean collectionHasTwoElements (
			@NonNull Collection <?> collection) {

		return collection.size () == 2;

	}

	public static
	boolean collectionDoesNotHaveTwoElements (
			@NonNull Collection <?> collection) {

		return collection.size () != 2;

	}

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

	public static
	boolean collectionHasFourElements (
			@NonNull Collection <?> collection) {

		return collection.size () == 4;

	}

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
	Type collectionFirstElementRequired (
			@NonNull Collection <Type> collection) {

		return collection.iterator ().next ();

	}

}
