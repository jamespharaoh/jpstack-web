package wbs.framework.data.tools;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.lang.reflect.Field;

import lombok.SneakyThrows;

import org.json.simple.JSONObject;

import wbs.framework.data.annotations.DataAttribute;

public
class DataFromJson {

	@SneakyThrows ({
		InstantiationException.class,
		IllegalAccessException.class
	})
	public <Data>
	Data fromJson (
			Class<Data> dataClass,
			JSONObject jsonValue) {

		Data dataValue =
			dataClass.newInstance ();

		for (
			Field field
				: dataClass.getDeclaredFields ()
		) {

			DataAttribute dataAttribute =
				field.getAnnotation (
					DataAttribute.class);

			if (dataAttribute == null)
				continue;

			field.setAccessible (true);

			Object fieldValue =
				jsonValue.get (
					field.getName ());

			if (fieldValue == null)
				continue;

			if (field.getType () == Integer.class) {

				field.set (
					dataValue,
					(int) (long) (Long)
					fieldValue);

			} else if (field.getType () == Long.class) {

				field.set (
					dataValue,
					(long) (Long)
					fieldValue);

			} else if (field.getType () == String.class) {

				field.set (
					dataValue,
					(String)
					fieldValue);

			} else {

				throw new RuntimeException (
					stringFormat (
						"Unable to map json attribute %s.%s ",
						field.getDeclaringClass ().getSimpleName (),
						field.getName (),
						"of type %s",
						field.getType ()));

			}

		}

		return dataValue;

	}

}
