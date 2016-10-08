package wbs.utils.etc;

import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.reflect.MethodUtils;

import wbs.utils.exception.RuntimeIllegalAccessException;
import wbs.utils.exception.RuntimeInstantiationException;
import wbs.utils.exception.RuntimeInvocationTargetException;
import wbs.utils.exception.RuntimeNoSuchMethodException;

public
class ReflectionUtils {

	public static
	Object fieldGet (
			@NonNull Field field,
			@NonNull Object object) {

		try {

			return field.get (
				object);

		} catch (IllegalAccessException illegalAccessException) {

			throw new RuntimeIllegalAccessException (
				illegalAccessException);

		}

	}

	public static
	void fieldSet (
			@NonNull Field field,
			@NonNull Object object,
			@NonNull Optional <Object> value) {

		try {

			field.set (
				object,
				optionalOrNull (
					value));

		} catch (IllegalAccessException illegalAccessException) {

			throw new RuntimeIllegalAccessException (
				illegalAccessException);

		}

	}

	public static <Type>
	Constructor <Type> getConstructor (
			@NonNull Class <Type> theClass,
			@NonNull Class <?> ... parameterTypes) {

		try {

			return theClass.getConstructor (
				parameterTypes);

		} catch (NoSuchMethodException noSuchMethodException) {

			throw new RuntimeNoSuchMethodException (
				noSuchMethodException);

		}

	}

	public static <Type>
	Type constructorInvoke (
			@NonNull Constructor <Type> constructor,
			@NonNull Object ... parameters) {

		try {

			return constructor.newInstance (
				parameters);

		} catch (IllegalAccessException illegalAccessException) {

			throw new RuntimeIllegalAccessException (
				illegalAccessException);

		} catch (InvocationTargetException invocationTargetException) {

			throw new RuntimeInvocationTargetException (
				invocationTargetException);

		} catch (InstantiationException instantiationException) {

			throw new RuntimeInstantiationException (
				instantiationException);

		}

	}

	public static
	Object methodInvoke (
			@NonNull Method method,
			@NonNull Object object,
			@NonNull Object ... parameters) {

		try {

			return method.invoke (
				object,
				parameters);

		} catch (IllegalAccessException illegalAccessException) {

			throw new RuntimeIllegalAccessException (
				illegalAccessException);

		} catch (InvocationTargetException invocationTargetException) {

			throw new RuntimeInvocationTargetException (
				invocationTargetException);

		}

	}

	public static
	Object methodInvokeByName (
			@NonNull Object object,
			@NonNull String methodName,
			@NonNull Object ... parameters) {

		try {

			return MethodUtils.invokeMethod (
				object,
				methodName,
				parameters);

		} catch (NoSuchMethodException noSuchMethodException) {

			throw new RuntimeNoSuchMethodException (
				noSuchMethodException);

		} catch (IllegalAccessException illegalAccessException) {

			throw new RuntimeIllegalAccessException (
				illegalAccessException);

		} catch (InvocationTargetException invocationTargetException) {

			throw new RuntimeInvocationTargetException (
				invocationTargetException);

		}

	}
			
}
