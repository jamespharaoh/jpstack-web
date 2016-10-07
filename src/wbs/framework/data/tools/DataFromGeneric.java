package wbs.framework.data.tools;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ReflectionUtils.fieldSet;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.isInstanceOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;

public
class DataFromGeneric {

	public <Data>
	Data fromObject (
			@NonNull Class <Data> dataClass,
			@NonNull Object object) {

		if (
			isInstanceOf (
				Map.class,
				object)
		) {

			return fromMap (
				dataClass,
				(Map <?, ?>) object);

		} else {

			throw new ClassCastException (
				stringFormat (
					"Unable to construct '%s' from '%s'",
					dataClass.getSimpleName (),
					object.getClass ().getSimpleName ()));

		}

	}

	public <Data>
	Data fromMap (
			@NonNull Class <Data> dataClass,
			@NonNull Map <?, ?> map) {

		Data dataValue =
			classInstantiate (
				dataClass);

		for (
			Field field
				: dataClass.getDeclaredFields ()
		) {

			field.setAccessible (true);

			DataAttribute dataAttribute =
				field.getAnnotation (
					DataAttribute.class);

			if (
				isNotNull (
					dataAttribute)
			) {

				String dataName =
					ifNull (
						dataAttribute.name (),
						field.getName ());

				Object fieldValue =
					map.get (
						dataName);
	
				if (

					dataAttribute.required ()

					&& isNull (
						fieldValue)

				) {

					throw new RuntimeException (
						stringFormat (
						"Required field %s.%s not present",
						dataClass.getSimpleName (),
						field.getName (),
						dataName));

				} else if (
					isNotNull (
						fieldValue)
				) {

					doDataAttribute (
						dataClass,
						fieldValue,
						dataValue,
						field,
						dataAttribute,
						fieldValue);

				}

			}

			DataChildren dataChildren =
				field.getAnnotation (
					DataChildren.class);

			if (
				isNotNull (
					dataChildren)
			) {

				Object fieldValue =
					map.get (
						field.getName ());
	
				doDataChildren (
					dataClass,
					fieldValue,
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
			@NonNull Class <Data> dataClass,
			@NonNull Object jsonValue,
			@NonNull Object dataValue,
			@NonNull Field field,
			@NonNull DataAttribute dataAttribute,
			@NonNull Object fieldValue) {

		if (field.getType () == Integer.class) {

			fieldSet (
				field,
				dataValue,
				optionalFromNullable (
					toJavaIntegerRequired (
						(Long)
						fieldValue)));

		} else if (field.getType () == Long.class) {

			fieldSet (
				field,
				dataValue,
				optionalFromNullable (
					(Long)
					fieldValue));

		} else if (field.getType () == String.class) {

			fieldSet (
				field,
				dataValue,
				optionalFromNullable (
					(String)
					fieldValue));

		} else if (field.getType () == JSONObject.class) {

			fieldSet (
				field,
				dataValue,
				optionalFromNullable (
					(JSONObject)
					fieldValue));

		} else {

			throw new RuntimeException (
				stringFormat (
					"Unable to map map attribute %s.%s ",
					field.getDeclaringClass ().getSimpleName (),
					field.getName (),
					"of type %s",
					field.getType ()));

		}

	}

	private <Data>
	void doDataChildren (
			@NonNull Class <Data> dataClass,
			@NonNull Object fieldValue2,
			@NonNull Object dataValue,
			@NonNull Field field,
			@NonNull DataChildren dataChildren,
			@NonNull Object fieldValueObject) {

		if (
			List.class.isAssignableFrom (
				field.getType ())
		) {

			JSONArray fieldValue =
				(JSONArray)
				fieldValueObject;

			List <Object> listValue =
				new ArrayList<> ();

			for (
				Object jsonObject
					: fieldValue
			) {

				ParameterizedType parameterizedType =
					(ParameterizedType)
					field.getGenericType ();

				listValue.add (
					fromObject (
						(Class <?>)
							parameterizedType.getActualTypeArguments () [0],
						(JSONObject)
							jsonObject));

			}

			fieldSet (
				field,
				dataValue,
				optionalOf (
					listValue));

		} else if (
			Map.class.isAssignableFrom (
				field.getType ())
		) {

			JSONObject fieldValue =
				(JSONObject)
				fieldValueObject;

			Map <String, Object> mapValue =
				new LinkedHashMap<> ();

			for (
				Object jsonEntryObject
					: fieldValue.entrySet ()
			) {

				Map.Entry <?, ?> jsonEntry =
					(Map.Entry <?, ?>)
					jsonEntryObject;

				mapValue.put (
					jsonEntry.getKey ().toString (),
					jsonEntry.getValue ().toString ());

			}

			fieldSet (
				field,
				dataValue,
				optionalOf (
					mapValue));

		} else {

			throw new RuntimeException ();

		}

	}

}
