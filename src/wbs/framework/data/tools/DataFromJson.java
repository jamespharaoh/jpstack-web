package wbs.framework.data.tools;

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

			field.set (
				dataValue,
				fieldValue);

		}

		return dataValue;

	}

}
