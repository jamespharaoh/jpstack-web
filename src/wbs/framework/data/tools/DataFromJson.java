package wbs.framework.data.tools;

import java.lang.reflect.Field;

import lombok.SneakyThrows;
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
			Object jsonValue) {

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

			System.out.println (
				"ATTR: " + field.getName ());

		}

		return dataValue;

	}

}
