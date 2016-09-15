package wbs.framework.data.tools;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import lombok.NonNull;
import lombok.SneakyThrows;
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
				toJavaIntegerRequired (
					(Long)
					fieldValue));

		} else if (field.getType () == Long.class) {

			field.set (
				dataValue,
				(Long)
				fieldValue);

		} else if (field.getType () == String.class) {

			field.set (
				dataValue,
				(String)
				fieldValue);

		} else if (field.getType () == JSONObject.class) {

			field.set (
				dataValue,
				(JSONObject)
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

		if (
			List.class.isAssignableFrom (
				field.getType ())
		) {

			JSONArray fieldValue =
				(JSONArray)
				fieldValueObject;

			List<Object> listValue =
				new ArrayList<> ();

			for (
				Object jsonObject
					: fieldValue
			) {

				ParameterizedType parameterizedType =
					(ParameterizedType)
					field.getGenericType ();

				listValue.add (
					fromJson (
						(Class<?>)
							parameterizedType.getActualTypeArguments () [0],
						(JSONObject)
							jsonObject));

			}

			field.set (
				dataValue,
				listValue);

		} else if (
			Map.class.isAssignableFrom (
				field.getType ())
		) {

			JSONObject fieldValue =
				(JSONObject)
				fieldValueObject;

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
