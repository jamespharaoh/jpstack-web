package wbs.framework.utils.etc;

import lombok.NonNull;

public
class TypeUtils {

	public static
	boolean isInstanceOf (
			@NonNull Class <?> theClass,
			@NonNull Object object) {

		return theClass.isInstance (
			object);

	}

	public static
	boolean isNotInstanceOf (
			@NonNull Class <?> theClass,
			@NonNull Object object) {

		return ! theClass.isInstance (
			object);

	}

	public static <Type>
	Type dynamicCast (
			@NonNull Class <Type> classToCastTo,
			@NonNull Object value) {

		return classToCastTo.cast (
			value);

	}

	public static
	boolean classEqual (
			@NonNull Class <?> class0,
			@NonNull Class <?> class1) {

		return class0 == class1;

	}

	public static
	boolean classNotEqual (
			@NonNull Class <?> class0,
			@NonNull Class <?> class1) {

		return class0 != class1;

	}

	public static
	boolean classInSafe (
			@NonNull Class <?> value,
			@NonNull Iterable <Class <?>> examples) {

		for (
			Class <?> example
				: examples
		) {

			if (value == example) {
				return true;
			}

		}

		return false;

	}

	public static
	boolean classInSafe (
			@NonNull Class <?> value,
			@NonNull Class <?> ... examples) {

		for (
			Class <?> example
				: examples
		) {

			if (value == example) {
				return true;
			}

		}

		return false;

	}

	public static
	boolean classNotInSafe (
			@NonNull Class <?> value,
			@NonNull Class <?> ... examples) {

		for (
			Class <?> example
				: examples
		) {

			if (value == example) {
				return false;
			}

		}

		return true;

	}

}
