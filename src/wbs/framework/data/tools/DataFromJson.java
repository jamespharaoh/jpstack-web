package wbs.framework.data.tools;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ReflectionUtils.fieldSet;
import static wbs.utils.etc.TypeUtils.classEqualSafe;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.StringUtils.nullIfEmptyString;
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
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;

public
class DataFromJson {

	public <Data>
	Data fromJson (
			@NonNull Class <Data> dataClass,
			@NonNull JSONObject jsonValue) {

		Data dataValue =
			classInstantiate (
				dataClass);

		for (
			Field field
				: dataClass.getDeclaredFields ()
		) {

			field.setAccessible (true);

			// handle data attribute

			DataAttribute dataAttribute =
				field.getAnnotation (
					DataAttribute.class);

			if (
				isNotNull (
					dataAttribute)
			) {

				Object fieldValue =
					jsonValue.get (
						ifNull (
							nullIfEmptyString (
								dataAttribute.name ()),
							field.getName ()));

				if (fieldValue == null) {
					continue;
				}

				doDataAttribute (
					dataClass,
					dataValue,
					field,
					dataAttribute,
					fieldValue);

			}

			// handle data child

			DataChild dataChild =
				field.getAnnotation (
					DataChild.class);

			if (
				isNotNull (
					dataChild)
			) {

				Object fieldValue =
					jsonValue.get (
						ifNull (
							nullIfEmptyString (
								dataChild.name ()),
							field.getName ()));

				if (fieldValue == null) {
					continue;
				}

				doDataChild (
					dataClass,
					dataValue,
					field,
					dataChild,
					(JSONObject)
					fieldValue);

			}

			// handle data children

			DataChildren dataChildren =
				field.getAnnotation (
					DataChildren.class);

			if (
				isNotNull (
					dataChildren)
			) {

				Object fieldValue =
					jsonValue.get (
						ifNull (
							dataChildren.childrenElement (),
							field.getName ()));

				if (fieldValue == null) {
					continue;
				}

				doDataChildren (
					dataClass,
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
			@NonNull Object dataValue,
			@NonNull Field field,
			@NonNull DataAttribute dataAttribute,
			@NonNull Object fieldValue) {

		if (
			classEqualSafe (
				Long.class,
				field.getType ())
		) {

			fieldSet (
				field,
				dataValue,
				optionalOf (
					(Long)
					fieldValue));

		} else if (
			classEqualSafe (
				String.class,
				field.getType ())
		) {

			fieldSet (
				field,
				dataValue,
				optionalOf (
					(String)
					fieldValue));

		} else if (
			classEqualSafe (
				Integer.class,
				field.getType ())
		) {

			fieldSet (
				field,
				dataValue,
				optionalOf (
					toJavaIntegerRequired (
						(Long)
						fieldValue)));

		} else if (
			classEqualSafe (
				JSONObject.class,
				field.getType ())
		) {

			fieldSet (
				field,
				dataValue,
				optionalOf (
					(JSONObject)
					fieldValue));

		} else {

			throw new RuntimeException (
				stringFormat (
					"Unable to map json attribute %s.%s ",
					classNameSimple (
						field.getDeclaringClass ()),
					field.getName (),
					"of type %s",
					classNameSimple (
						field.getType ())));

		}

	}

	private <Data>
	void doDataChild (
			@NonNull Class <Data> dataClass,
			@NonNull Object dataValue,
			@NonNull Field field,
			@NonNull DataChild dataChild,
			@NonNull JSONObject fieldValue) {

		fieldSet (
			field,
			dataValue,
			optionalOf (
				fromJson (
					field.getType (),
					fieldValue)));

	}

	private <Data>
	void doDataChildren (
			@NonNull Class <Data> dataClass,
			@NonNull Object dataValue,
			@NonNull Field field,
			@NonNull DataChildren dataChildren,
			@NonNull Object fieldValueObject) {

		if (
			classEqualSafe (
				List.class,
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
					fromJson (
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
			classEqualSafe (
				Map.class,
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
