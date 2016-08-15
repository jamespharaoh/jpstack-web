package wbs.framework.entity.model;

import static wbs.framework.utils.etc.Misc.classForNameRequired;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.StringUtils.camelToUnderscore;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.uncapitalise;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.entity.build.ModelBuilderManager;
import wbs.framework.entity.build.ModelFieldBuilderContext;
import wbs.framework.entity.build.ModelFieldBuilderTarget;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.Record;
import wbs.framework.schema.helper.SchemaNamesHelper;
import wbs.framework.schema.helper.SchemaTypesHelper;

@Accessors (fluent = true)
@PrototypeComponent ("modelBuilder")
public
class ModelBuilder {

	// dependencies

	@Inject
	ModelBuilderManager modelBuilderManager;

	@Inject
	SchemaNamesHelper schemaNamesHelper;

	@Inject
	SchemaTypesHelper schemaTypesHelper;

	// properties

	@Getter @Setter
	ModelMetaSpec modelMeta;

	// state

	PluginModelSpec pluginModel;
	PluginSpec plugin;

	ModelImplementation model;

	String recordClassName;
	String recordClassNameFull;
	Class<? extends Record<?>> recordClass;

	String objectHelperClassName;
	String objectHelperClassNameFull;
	Class<? extends ObjectHelper<?>> objectHelperClass;

	// implementation

	public
	Model build () {

		try {

			return buildReal ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating model %s",
					modelMeta.name ()),
				exception);

		}

	}

	private
	Model buildReal () {

		plugin =
			modelMeta.plugin ();

		// record class

		recordClassName =
			stringFormat (
				"%sRec",
				capitalise (
					modelMeta.name ()));

		recordClassNameFull =
			stringFormat (
				"%s.model.%s",
				plugin.packageName (),
				recordClassName);

		@SuppressWarnings ("unchecked")
		Class<? extends Record<?>> recordClassTemp =
			(Class<? extends Record<?>>)
			classForNameRequired (
				recordClassNameFull);

		recordClass =
			recordClassTemp;

		// object helper class

		objectHelperClassName =
			stringFormat (
				"%sObjectHelper",
				capitalise (
					modelMeta.name ()));

		objectHelperClassNameFull =
			stringFormat (
				"%s.model.%s",
				plugin.packageName (),
				objectHelperClassName);

		@SuppressWarnings ("unchecked")
		Class<? extends ObjectHelper<?>> objectHelperClassTemp =
			(Class<? extends ObjectHelper<?>>)
			classForNameRequired (
				objectHelperClassNameFull);

		objectHelperClass =
			objectHelperClassTemp;

		// model

		model =
			new ModelImplementation ()

			.objectClass (
				recordClass)

			.objectName (
				uncapitalise (
					modelMeta.name ()))

			.objectTypeCode (
				camelToUnderscore (
					modelMeta.name ()))

			.tableName (
				ifNull (
					modelMeta.tableName (),
					schemaNamesHelper.tableName (
						recordClass)))

			.create (
				ifNull (
					modelMeta.create (),
					true))

			.mutable (
				ifNull (
					modelMeta.mutable (),
					true))

			.helperClass (
				objectHelperClass);

		// model fields

		ModelFieldBuilderContext context =
			new ModelFieldBuilderContext ()

			.modelMeta (
				modelMeta)

			.recordClass (
				recordClass);

		ModelFieldBuilderTarget target =
			new ModelFieldBuilderTarget ()

			.model (
				model)

			.fields (
				model.fields ())

			.fieldsByName (
				model.fieldsByName ());

		modelBuilderManager.build (
			context,
			modelMeta.fields (),
			target);

		modelBuilderManager.build (
			context,
			modelMeta.collections (),
			target);

		// and return

		return model;

	}

	public
	static Annotation findAnnotation (
			Annotation[] annotations,
			Class<? extends Annotation> metaAnnotation) {

		for (Annotation annotation
				: annotations) {

			if (annotation.annotationType ()
					.isAnnotationPresent (metaAnnotation))
				return annotation;

		}

		return null;

	}

	/**
	 * Retrieves the value of the annotation parameter which is annotated with
	 * the specified meta-annotation.
	 */
	public
	Object annotationParam (
			Annotation annotation,
			Class<? extends Annotation> metaAnnotation,
			Object defaultValue) {

		for (Method method
				: annotation.annotationType ().getMethods ()) {

			if (! method.isAnnotationPresent (metaAnnotation))
				continue;

			try {

				return method.invoke (annotation);

			} catch (Exception exception) {

				throw new RuntimeException (exception);

			}

		}

		return defaultValue;

	}

	/**
	 * Retrieves the value of the annotation parameter which is annotated with
	 * the specified meta-annotation. Specifically for string values, this will
	 * return a default value if the meta annotation is not present or if the
	 * value is the empty string.
	 */
	public
	String annotationStringParam (
			Annotation annotation,
			Class<? extends Annotation> metaAnnotation,
			String defaultValue) {

		for (
			Method method
				: annotation.annotationType ().getMethods ()
		) {

			if (method.getReturnType () != String.class)
				continue;

			if (! method.isAnnotationPresent (metaAnnotation))
				continue;

			try {

				String value =
					(String)
					method.invoke (annotation);

				if (value.isEmpty ())
					return defaultValue;

				return value;

			} catch (Exception exception) {

				throw new RuntimeException (exception);

			}

		}

		return defaultValue;

	}

}
