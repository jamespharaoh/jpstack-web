package wbs.framework.data.tools;

import static wbs.utils.etc.Misc.toEnumGeneric;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ReflectionUtils.fieldSet;
import static wbs.utils.etc.ResultUtils.errorResultFormat;
import static wbs.utils.etc.ResultUtils.isError;
import static wbs.utils.etc.ResultUtils.resultValueRequired;
import static wbs.utils.etc.ResultUtils.successResultAbsent;
import static wbs.utils.etc.ResultUtils.successResultPresent;
import static wbs.utils.etc.TypeUtils.classEqualSafe;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.etc.TypeUtils.isSubclassOf;
import static wbs.utils.string.StringUtils.nullIfEmptyString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import lombok.NonNull;

import org.json.simple.JSONObject;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;

import fj.data.Either;

public
class DataFromJson {

	public <Data>
	Data fromJson (
			@NonNull Class <Data> dataClass,
			@NonNull String jsonString) {

		JsonParser jsonParser =
			new JsonParser ();

		JsonObject jsonObject =
			(JsonObject)
			jsonParser.parse (
				jsonString);

		return fromJson (
			dataClass,
			jsonObject);

	}

	public <Data>
	Data fromJson (
			@NonNull Class <Data> dataClass,
			@NonNull JsonElement jsonElement) {

		if (
			classEqualSafe (
				dataClass,
				String.class)
		) {

			return dataClass.cast (
				jsonElement.getAsString ());

		} else {

			return fromJsonObject (
				dataClass,
				jsonElement.getAsJsonObject ());

		}

	}

	// private implementation

	private <Data>
	void doDataAttribute (
			@NonNull Class <Data> dataClass,
			@NonNull Object dataValue,
			@NonNull Field field,
			@NonNull DataAttribute dataAttribute,
			@NonNull JsonElement jsonValue) {

		Either <Optional <Object>, String> nativeValueResult =
			fromJsonSimple (
				dataAttribute,
				field.getType (),
				jsonValue);

		if (
			isError (
				nativeValueResult)
		) {

			throw new RuntimeException (
				stringFormat (
					"Unable to map json attribute %s.%s ",
					classNameSimple (
						field.getDeclaringClass ()),
					field.getName (),
					"of type %s ",
					classNameSimple (
						jsonValue.getClass ()),
					"to field with type %s",
					classNameSimple (
						field.getType ())));

		}

		Optional <Object> nativeValueOptional =
			resultValueRequired (
				nativeValueResult);

		fieldSet (
			field,
			dataValue,
			nativeValueOptional);

	}

	private <Data>
	Data fromJsonObject (
			@NonNull Class <Data> dataClass,
			@NonNull JsonObject jsonObject) {

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

				JsonElement fieldValue =
					jsonObject.get (
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
					jsonObject.get (
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
					(JsonObject)
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
					jsonObject.get (
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
	Either <Optional <Object>, String> fromJsonSimple (
			@NonNull DataAttribute dataAttribute,
			@NonNull Class <?> targetType,
			@NonNull JsonElement jsonElement) {

		if (jsonElement.isJsonNull ()) {
			return successResultAbsent ();
		}

		if (jsonElement.isJsonObject ()) {

			return successResultPresent (
				jsonElement.getAsJsonObject ());

		}

		if (jsonElement.isJsonPrimitive ()) {

			JsonPrimitive jsonPrimitive =
				jsonElement.getAsJsonPrimitive ();

			if (
				classEqualSafe (
					Long.class,
					targetType)
			) {

				return successResultPresent (
					jsonPrimitive.getAsLong ());

			} else if (
				classEqualSafe (
					String.class,
					targetType)
			) {

				return successResultPresent (
					jsonPrimitive.getAsString ());

			} else if (
				classEqualSafe (
					Integer.class,
					targetType)
			) {

				return successResultPresent (
					jsonPrimitive.getAsInt ());

			} else if (
				classEqualSafe (
					Boolean.class,
					targetType)
			) {

				return successResultPresent (
					jsonPrimitive.getAsBoolean ());

			} else if (
				classEqualSafe (
					Double.class,
					targetType)
			) {

				return successResultPresent (
					jsonPrimitive.getAsDouble ());

			} else if (
				isSubclassOf (
					Enum.class,
					targetType)
			) {

				return successResultPresent (
					toEnumGeneric (
						targetType,
						jsonPrimitive.getAsString ()));

			}

		}

		return errorResultFormat (
			"Don't know how to map %s as %s",
			classNameSimple (
				jsonElement.getClass ()),
			classNameSimple (
				targetType));

	}

	private <Data>
	void doDataChild (
			@NonNull Class <Data> dataClass,
			@NonNull Object dataValue,
			@NonNull Field field,
			@NonNull DataChild dataChild,
			@NonNull JsonObject fieldValue) {

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

			JsonArray fieldValue =
				(JsonArray)
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
						(JsonElement)
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
