package wbs.utils.etc;

import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import java.lang.reflect.Field;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.utils.exception.RuntimeIllegalAccessException;

public
class ReflectionUtils {

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

}
