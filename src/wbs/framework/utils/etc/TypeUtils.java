package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.StringUtils.stringFormatArray;

import com.google.common.base.Optional;

import lombok.NonNull;

public
class TypeUtils {

	// ---------- class instances

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
	boolean isSubclassOf (
			@NonNull Class <?> superclass,
			@NonNull Class <?> subclass) {

		return superclass.isAssignableFrom (
			subclass);

	}

	public static
	boolean isNotSubclassOf (
			@NonNull Class <?> superclass,
			@NonNull Class <?> subclass) {

		return ! superclass.isAssignableFrom (
			subclass);

	}

	public static
	boolean isSuperclassOf (
			@NonNull Class <?> subclass,
			@NonNull Class <?> superclass) {

		return superclass.isAssignableFrom (
			subclass);

	}

	public static
	boolean isNotSuperclassOf (
			@NonNull Class <?> subclass,
			@NonNull Class <?> superclass) {

		return ! superclass.isAssignableFrom (
			subclass);

	}

	// ---------- class equality

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

	// ---------- class lookup

	public static
	Optional <Class <?>> classForName (
			@NonNull String className) {

		try {

			return Optional.<Class<?>>of (
				Class.forName (
					className));

		} catch (ClassNotFoundException exception) {

			return Optional.absent ();

		}

	}

	public static
	Optional <Class <?>> classForName (
			@NonNull String packageName,
			@NonNull String className) {

		try {

			return Optional.<Class<?>>of (
				Class.forName (
					packageName + "." + className));

		} catch (ClassNotFoundException exception) {

			return Optional.absent ();

		}

	}

	public static
	Class<?> classForNameRequired (
			@NonNull String className) {

		try {

			return Class.forName (
				className);

		} catch (ClassNotFoundException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	Class<?> classForNameRequired (
			@NonNull String packageName,
			@NonNull String className) {

		try {

			return Class.forName (
				packageName + "." + className);

		} catch (ClassNotFoundException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	Optional <Class <?>> classForNameFormat (
			@NonNull Object ... arguments) {

		return classForName (
			stringFormatArray (
				arguments));

	}

	public static
	Class <?> classForNameFormatRequired (
			@NonNull Object ... arguments) {

		return classForNameRequired (
			stringFormatArray (
				arguments));

	}

	// ---------- class names

	public static
	String classNameSimple (
			@NonNull Class <?> theClass) {

		return theClass.getSimpleName ();

	}

	public static
	String objectClassNameSimple (
			@NonNull Object object) {

		return object.getClass ().getSimpleName ();

	}

	public static
	String classNameFull (
			@NonNull Class <?> theClass) {

		return theClass.getName ();

	}

	public static
	String objectClassNameFull (
			@NonNull Object object) {

		return object.getClass ().getName ();

	}

	public static
	String classPackageName (
			@NonNull Class <?> theClass) {

		return theClass.getPackage ().getName ();

	}

	public static
	String objectPackageName (
			@NonNull Object object) {

		return object.getClass ().getPackage ().getName ();

	}

}
