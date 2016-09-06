package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.ArrayUtils.arrayIsEmpty;
import static wbs.framework.utils.etc.ArrayUtils.arrayMap;
import static wbs.framework.utils.etc.CollectionUtils.collectionIsEmpty;
import static wbs.framework.utils.etc.IterableUtils.iterableMap;
import static wbs.framework.utils.etc.LogicUtils.referenceNotEqualUnsafe;
import static wbs.framework.utils.etc.MapUtils.mapItemForKeyOrKey;
import static wbs.framework.utils.etc.StringUtils.joinWithCommaAndSpace;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringFormatArray;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.codegen.JavaImportRegistry;

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

	public static <CastType>
	CastType dynamicCast (
			@NonNull Class <CastType> classToCastTo,
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
	boolean classEqualSafe (
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

	public static
	String typeSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull Map <String, String> typeParameterMappings,
			@NonNull Type type) {

		if (
			isInstanceOf (
				Class.class,
				type)
		) {

			return classSourceName (
				imports,
				(Class <?>)
				type);

		} else if (
			isInstanceOf (
				ParameterizedType.class,
				type)
		) {

			return parameterizedTypeSourceName (
				imports,
				typeParameterMappings,
				(ParameterizedType)
				type);

		} else if (
			isInstanceOf (
				TypeVariable.class,
				type)
		) {

			return typeVariableSourceName (
				imports,
				typeParameterMappings,
				(TypeVariable <?>)
				type);

		} else if (
			isInstanceOf (
				WildcardType.class,
				type)
		) {

			return wildcardTypeSourceName (
				imports,
				(WildcardType)
				type);

		} else {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to handle a %s",
					type.getClass ().getSimpleName ()));

		}

	}

	public static
	String classSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull Class <?> theClass) {

		if (theClass.isArray ()) {

			return stringFormat (
				"%s[]",
				classSourceName (
					imports,
					theClass.getComponentType ()));

		} else {

			return imports.register (
				theClass);

		}

	}

	public static
	String parameterizedTypeSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull Map <String, String> typeParameterMappings,
			@NonNull ParameterizedType parameterizedType) {

		if (
			arrayIsEmpty (
				parameterizedType.getActualTypeArguments ())
		) {

			return imports.register (
				(Class <?>)
				parameterizedType.getRawType ());

		} else {

			return stringFormat (
				"%s <%s>",
				typeSourceName (
					imports,
					typeParameterMappings,
					parameterizedType.getRawType ()),
				joinWithCommaAndSpace (
					arrayMap (
						typeArgument ->
							typeSourceName (
								imports,
								typeParameterMappings,
								typeArgument),
						parameterizedType.getActualTypeArguments ())));

		}

	}

	public static
	String typeVariableSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull Map <String, String> typeParameterMappings,
			@NonNull TypeVariable <?> typeVariable) {

		return mapItemForKeyOrKey (
			typeParameterMappings,
			typeVariable.getName ());

	}

	public static
	String typeVariableSourceDeclaration (
			@NonNull JavaImportRegistry imports,
			@NonNull Map <String, String> typeParameterMappings,
			@NonNull TypeVariable <?> typeVariable) {

		List <Type> bounds =
			Arrays.stream (
				typeVariable.getBounds ())

			.filter (
				bound ->
					referenceNotEqualUnsafe (
						bound,
						Object.class))

			.collect (
				Collectors.toList ());

		if (
			collectionIsEmpty (
				bounds)
		) {

			return typeVariable.getName ();

		} else {

			return stringFormat (
				"%s extends %s",
				typeVariable.getName (),
				joinWithCommaAndSpace (
					iterableMap (
						bound ->
							typeSourceName (
								imports,
								typeParameterMappings,
								bound),
						bounds)));

		}

	}

	public static
	String wildcardTypeSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull WildcardType wildcardType) {

		return wildcardType.toString ();

	}

	public static
	String parameterSourceTypeName (
			@NonNull JavaImportRegistry imports,
			@NonNull Map <String, String> typeParameterMappings,
			@NonNull Parameter parameter) {

		if (parameter.isVarArgs ()) {

			return stringFormat (
				"%s ...",
				typeSourceName (
					imports,
					typeParameterMappings,
					parameter.getType ().getComponentType ()));

		} else {

			return typeSourceName (
				imports,
				typeParameterMappings,
				parameter.getParameterizedType ());

		}

	}

}
