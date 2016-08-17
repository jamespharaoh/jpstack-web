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

}
