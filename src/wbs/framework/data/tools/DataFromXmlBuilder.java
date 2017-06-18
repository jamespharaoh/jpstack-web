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

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.framework.data.tools.DataFromXmlImplementation.DataClassInfo;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("dataFromXmlBuilder")
@Accessors (fluent = true)
public
class DataFromXmlBuilder {

	// singeton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	ComponentProvider <DataFromXmlImplementation>
		dataFromXmlImplementationProvider;

	// state

	Map <String, List <DataClassInfo>> dataClassesMap =
		new HashMap<> ();

	Map <String, Map <String, ?>> namedObjectCollections =
		new HashMap<> ();

	// implementation

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <Class <?>> builderClasses) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerBuilderClasses");

		) {

			for (
				Class <?> builderClass
					: builderClasses
			) {

				registerBuilderClass (
					taskLogger,
					builderClass);

			}

			return this;

		}

	}

	public
	DataFromXmlBuilder registerBuilderClasses (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <?>... builderClasses) {

		return registerBuilderClasses (
			parentTaskLogger,
			Arrays.asList (
				builderClasses));

	}

	public
	DataFromXmlBuilder registerBuilderClass (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <?> builderClass) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerBuilderClass");

		) {

			ComponentProvider <?> builderProvider =
				new ComponentProvider <Object> () {

				@Override
				public
				Object provide (
						@NonNull TaskLogger parentTaskLogger) {

					try (

						OwnedTaskLogger taskLogger =
							logContext.nestTaskLogger (
								parentTaskLogger,
								"provide");

					) {

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
				taskLogger,
				builderClass,
				builderProvider);

			return this;

		}

	}

	public <Type>
	DataFromXmlBuilder registerBuilders (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Map <Class <?>, ComponentProvider <Type>> builders) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerBuilders");

		) {

			builders.entrySet ().forEach (
				builder ->
					registerBuilder (
						taskLogger,
						builder.getKey (),
						builder.getValue ()));

			return this;

		}

	}

	public
	DataFromXmlBuilder registerBuilder (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <?> dataClass,
			@NonNull ComponentProvider <?> builderProvider) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerBuilder");

		) {

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
	DataFromXml build (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			return dataFromXmlImplementationProvider.provide (
				taskLogger)

				.dataClassesMap (
					ImmutableMap.copyOf (
						dataClassesMap))

				.namedObjectCollections (
					namedObjectCollections)

			;

		}

	}

}
