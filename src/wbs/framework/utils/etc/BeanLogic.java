package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;

public
class BeanLogic {

	private
	BeanLogic () {
		// never instantiated
	}

	public static
	Object getProperty (
			@NonNull Object object,
			@NonNull String propertyName) {

		try {

			Class<?> objectClass =
				object.getClass ();

			Method getter =
				getPropertyGetter (
					objectClass,
					propertyName);

			return getter.invoke (object);

		} catch (IllegalAccessException exception) {

			throw new RuntimeException (exception);

		} catch (InvocationTargetException exception) {

			throw new RuntimeException (exception);

		}

	}

	public static
	Method getPropertyGetter (
			@NonNull Class<?> objectClass,
			@NonNull String propertyName) {

		try {

			return objectClass.getMethod (
				getterName (propertyName));

		} catch (NoSuchMethodException exception) {
		}

		try {

			return objectClass.getMethod (
				isGetterName (propertyName));

		} catch (NoSuchMethodException exception) {
		}

		try {

			return objectClass.getMethod (
				propertyName);

		} catch (NoSuchMethodException exception) {
		}

		throw new RuntimeException (
			stringFormat (
				"Can't find getter for %s on %s",
				propertyName,
				objectClass.getName ()));

	}

	public static
	Object get (
			@NonNull Object object,
			@NonNull String propertyName) {

		try {

			Class<?> objectClass =
				object.getClass ();

			Method getter =
				objectClass.getMethod (
					propertyName);

			return getter.invoke (object);

		} catch (NoSuchMethodException exception) {

			throw new RuntimeException (exception);

		} catch (IllegalAccessException exception) {

			throw new RuntimeException (exception);

		} catch (InvocationTargetException exception) {

			throw new RuntimeException (exception);

		}

	}

	public static
	void setProperty (
			@NonNull Object object,
			@NonNull String propertyName,
			Object newValue) {

		try {

			Class<?> objectClass =
				object.getClass ();

			Method setter =
				getPropertySetter (
					objectClass,
					propertyName);

			setter.invoke (
				object,
				newValue);

		} catch (IllegalAccessException exception) {

			throw new RuntimeException (exception);

		} catch (InvocationTargetException exception) {

			throw new RuntimeException (exception);

		}

	}

	public static
	Method getPropertySetter (
			@NonNull Class<?> objectClass,
			@NonNull String propertyName) {

		String setterName =
			setterName (propertyName);

		Method[] methods =
			objectClass.getMethods ();

		for (Method method
				: methods) {

			if (method.getParameterTypes ().length != 1)
				continue;

			if (! method.getName ().equals (setterName))
				continue;

			return method;

		}

		for (Method method
				: methods) {

			if (method.getParameterTypes ().length != 1)
				continue;

			if (! method.getName ().equals (propertyName))
				continue;

			return method;

		}

		throw new RuntimeException (
			stringFormat (
				"Can't find setter for %s on %s",
				propertyName,
				objectClass.getName ()));

	}

	public static
	void set (
			Object object,
			String propertyName,
			Object newValue) {

		try {

			Class<?> objectClass =
				object.getClass ();

			Method[] methods =
				objectClass.getMethods ();

			for (Method method
					: methods) {

				if (method.getParameterTypes ().length != 1)
					continue;

				if (! method.getName ().equals (propertyName))
					continue;

				if (! method.getParameterTypes () [0].isInstance (newValue))
					continue;

				method.invoke (
					object,
					newValue);

				return;

			}

			throw new RuntimeException (
				stringFormat (
					"Method %s.%s (%s) not found",
					objectClass.getName (),
					propertyName,
					newValue.getClass ().getSimpleName ()));

		} catch (IllegalAccessException exception) {

			throw new RuntimeException (
				exception);

		} catch (InvocationTargetException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	String getterName (
			String name) {

		return "get"
			+ Character.toUpperCase (name.charAt (0))
			+ name.substring (1);

	}

	public static
	String isGetterName (
			String name) {

		return "is"
			+ Character.toUpperCase (name.charAt (0))
			+ name.substring (1);

	}

	public static
	String setterName (
			String name) {

		return "set"
			+ Character.toUpperCase (name.charAt (0))
			+ name.substring (1);

	}

	public static
	String nameFromGetter (
			String getterName) {

		Matcher matcher =
			getterPattern.matcher (getterName);

		if (! matcher.matches ())
			throw new IllegalArgumentException ();

		String upcasedName =
			matcher.group (1);

		return Character.toLowerCase (upcasedName.charAt (0))
				+ upcasedName.substring (1);

	}

	public static
	String nameFromSetter (
			String setterName) {

		Matcher matcher =
			setterPattern.matcher (setterName);

		if (! matcher.matches ())
			throw new IllegalArgumentException ();

		String upcasedName =
			matcher.group (1);

		return Character.toLowerCase (upcasedName.charAt (0))
				+ upcasedName.substring (1);

	}

	private final static
	Pattern getterPattern =
		Pattern.compile ("get([A-Z].*)");

	private final static
	Pattern setterPattern =
		Pattern .compile ("set([A-Z].*)");

	public static
	boolean isSetter (
			String name) {

		return setterPattern
			.matcher (name)
			.matches ();

	}

	public static
	Class<?> propertyClassForObject (
			@NonNull Object object,
			@NonNull String propertyName) {

		Class<?> objectClass =
			object.getClass ();

		return propertyClassForClass (
			objectClass,
			propertyName);

	}

	public static
	Class<?> propertyClassForClass (
			@NonNull Class<?> objectClass,
			@NonNull String propertyName) {

		Method getter =
			getPropertyGetter (
				objectClass,
				propertyName);

		return getter.getReturnType ();

	}

}
