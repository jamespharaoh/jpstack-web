package wbs.framework.data.tools;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.NonNull;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;

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

			field.setAccessible (true);

			Object fieldValue =
				jsonValue.get (
					field.getName ());

			if (fieldValue == null) {
				continue;
			}

			DataAttribute dataAttribute =
				field.getAnnotation (
					DataAttribute.class);

			if (
				isNotNull (
					dataAttribute)
			) {

				doDataAttribute (
					dataClass,
					jsonValue,
					dataValue,
					field,
					dataAttribute,
					fieldValue);

			}

			DataChildren dataChildren =
				field.getAnnotation (
					DataChildren.class);

			if (
				isNotNull (
					dataChildren)
			) {

				doDataChildren (
					dataClass,
					jsonValue,
					dataValue,
					field,
					dataChildren,
					fieldValue);

			}

		}

		return dataValue;

	}

	private <Data>
	void doDataAttribute (
			@NonNull Class<Data> dataClass,
			@NonNull JSONObject jsonValue,
			@NonNull Object dataValue,
			@NonNull Field field,
			@NonNull DataAttribute dataAttribute,
			@NonNull Object fieldValue)
		throws
			InstantiationException,
			IllegalAccessException {

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

	private <Data>
	void doDataChildren (
			@NonNull Class<Data> dataClass,
			@NonNull JSONObject jsonValue,
			@NonNull Object dataValue,
			@NonNull Field field,
			@NonNull DataChildren dataChildren,
			@NonNull Object fieldValueObject)
		throws
			InstantiationException,
			IllegalAccessException {

		JSONObject fieldValue =
			(JSONObject)
			fieldValueObject;

		if (
			Map.class.isAssignableFrom (
				field.getType ())
		) {

			Map<String,Object> mapValue =
				new LinkedHashMap<String,Object> ();

			for (
				Object jsonEntryObject
					: fieldValue.entrySet ()
			) {

				Map.Entry<?,?> jsonEntry =
					(Map.Entry<?,?>)
					jsonEntryObject;

				mapValue.put (
					jsonEntry.getKey ().toString (),
					jsonEntry.getValue ().toString ());

			}

			field.set (
				dataValue,
				mapValue);

		} else {

			throw new RuntimeException ();

		}

	}

}
