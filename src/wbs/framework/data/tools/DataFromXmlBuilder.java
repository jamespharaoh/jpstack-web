package wbs.framework.data.tools;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.nullIfEmptyString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.framework.data.tools.DataFromXmlImplementation.DataClassInfo;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class DataFromXmlBuilder {

	@Setter
	TaskLogger taskLogger;

	Map <String, List <DataClassInfo>> dataClassesMap =
		new HashMap<> ();

	Map <String, Map <String, ?>> namedObjectCollections =
		new HashMap<> ();

	public
	DataFromXmlBuilder addNamedObjectCollection (
			@NonNull String collectionName,
			@NonNull Map <String, ?> namedObjectCollection) {

		if (namedObjectCollection.containsKey (collectionName)) {

			throw new RuntimeException (
				stringFormat (
					"Named object collection \"%s\" added twice",
					collectionName));

		}

		namedObjectCollections.put (
			collectionName,
			namedObjectCollection);

		return this;

	}

	public
	DataFromXmlBuilder registerBuilderClasses (
			@NonNull List <Class <?>> builderClasses) {

		for (
			Class <?> builderClass
				: builderClasses
		) {

			registerBuilderClass (
				builderClass);

		}

		return this;

	}

	public
	DataFromXmlBuilder registerBuilderClasses (
			@NonNull Class <?>... builderClasses) {

		return registerBuilderClasses (
			Arrays.asList (
				builderClasses));

	}

	public
	DataFromXmlBuilder registerBuilderClass (
			@NonNull Class <?> builderClass) {

		Provider <?> builderProvider =
			new Provider <Object> () {

			@Override
			public
			Object get () {

				try {

					return builderClass.newInstance ();

				} catch (Exception exception) {

					throw new RuntimeException (
						stringFormat (
							"Unable to instantiate builder class %s",
							builderClass.getName ()),
						exception);

				}

			}

		};

		registerBuilder (
			builderClass,
			builderProvider);

		return this;

	}

	public
	DataFromXmlBuilder registerBuilder (
			@NonNull Class <?> dataClass,
			@NonNull Provider <?> builderProvider) {

		DataClass dataClassAnnotation =
			dataClass.getAnnotation (
				DataClass.class);

		if (dataClassAnnotation == null) {

			throw new RuntimeException (
				stringFormat (
					"Builder class %s has no @DataClass annotation",
					dataClass.getName ()));

		}

		String elementName =
			ifNull (
				nullIfEmptyString (
					dataClassAnnotation.value ()),
				camelToHyphen (
					dataClass.getSimpleName ()));

		Field parentField =
			findParentField (
				dataClass);

		Class <?> parentClass =
			parentField != null
				? parentField.getType ()
				: Object.class;

		// add to map

		List <DataClassInfo> dataClassInfos =
			dataClassesMap.get (
				elementName);

		if (dataClassInfos == null) {

			dataClassInfos =
				new ArrayList <DataClassInfo> ();

			dataClassesMap.put (
				elementName,
				dataClassInfos);

		}

		dataClassInfos.add (
			new DataClassInfo ()

			.parentClass (
				parentClass)

			.dataClass (
				dataClass)

			.provider (
				builderProvider)

		);

		return this;

	}

	Field findParentField (
			@NonNull Class<?> dataClass) {

		Field parentField = null;

		for (
			Field field
				: dataClass.getDeclaredFields ()
		) {

			DataParent dataParentAnnotation =
				field.getAnnotation (
					DataParent.class);

			if (dataParentAnnotation == null)
				continue;

			if (parentField != null)
				throw new RuntimeException ();

			parentField =
				field;

		}

		return parentField;

	}

	public
	DataFromXml build () {

		return new DataFromXmlImplementation ()

			.taskLogger (
				taskLogger)

			.dataClassesMap (
				ImmutableMap.copyOf (
					dataClassesMap))

			.namedObjectCollections (
				namedObjectCollections);

	}

}
