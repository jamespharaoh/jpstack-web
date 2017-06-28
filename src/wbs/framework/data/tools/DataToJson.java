package wbs.framework.data.tools;

import static wbs.utils.etc.EnumUtils.enumNameHyphens;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.ReflectionUtils.fieldGet;
import static wbs.utils.string.StringUtils.nullIfEmptyString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import lombok.NonNull;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

public
class DataToJson {

	public
	JsonElement toJson (
			@NonNull Object dataValue) {

		Class <?> dataClass =
			dataValue.getClass ();

		if (simpleClasses.contains (dataValue.getClass ())) {

			if (dataValue instanceof Number) {

				return new JsonPrimitive (
					(Number) dataValue);

			} else if (dataValue instanceof String) {

				return new JsonPrimitive (
					(String) dataValue);

			} else if (dataValue instanceof Boolean) {

				return new JsonPrimitive (
					(Boolean) dataValue);

			} else if (dataValue instanceof Character) {

				return new JsonPrimitive (
					(Character) dataValue);
			} else {

				throw shouldNeverHappen ();

			}

		} else if (dataValue instanceof Enum) {

			Enum <?> dataEnum =
				(Enum <?>) dataValue;

			return new JsonPrimitive (
				enumNameHyphens (
					dataEnum));

		} else if (dataValue instanceof List) {

			List <?> dataList =
				(List <?>) dataValue;

			JsonArray jsonArray =
				new JsonArray ();

			for (
				Object dataListElement
					: dataList
			) {

				jsonArray.add (
					toJson (
						dataListElement));

			}

			return jsonArray;

		} else if (dataValue instanceof Map) {

			Map <?, ?> dataMap =
				(Map <?, ?>) dataValue;

			JsonObject jsonObject =
				new JsonObject ();

			for (
				Map.Entry <?, ?> dataMapEntry
					: dataMap.entrySet ()
			) {

				jsonObject.add (
					(String) dataMapEntry.getKey (),
					toJson (
						dataMapEntry.getValue ()));

			}

			return jsonObject;

		} else {

			DataClass dataClassAnnotation =
				dataClass.getAnnotation (
					DataClass.class);

			if (dataClassAnnotation == null) {

				throw new RuntimeException (
					stringFormat (
						"Don't know how to convert %s ",
						dataClass.getSimpleName (),
						"to JSON"));

			}

			JsonObject jsonObject =
				new JsonObject ();

			for (
				Field field
					: dataClass.getDeclaredFields ()
			) {

				field.setAccessible (
					true);

				Object fieldValue =
					fieldGet (
						field,
						dataValue);

				if (fieldValue == null)
					continue;

				DataAttribute dataAttribute =
					field.getAnnotation (
						DataAttribute.class);

				if (dataAttribute != null) {

					jsonObject.add (
						ifNull (
							nullIfEmptyString (
								dataAttribute.name ()),
							field.getName ()),
						toJson (
							fieldValue));

				}

				DataChild dataChild =
					field.getAnnotation (
						DataChild.class);

				if (dataChild != null) {

					jsonObject.add (
						ifNull (
							nullIfEmptyString (
								dataChild.name ()),
							field.getName ()),
						toJson (
							fieldValue));

				}

				DataChildren dataChildren =
					field.getAnnotation (
						DataChildren.class);

				if (
					isNotNull (
						dataChildren)
				) {

					jsonObject.add (
						ifNull (
							nullIfEmptyString (
								dataChildren.childElement ()),
							field.getName ()),
						toJson (
							fieldValue));

				}

			}

			return jsonObject;

		}

	}

	// data

	Set <Class <?>> simpleClasses =
		ImmutableSet.<Class <?>> of (
			Boolean.class,
			Character.class,
			Double.class,
			Float.class,
			Integer.class,
			Long.class,
			String.class);

}
