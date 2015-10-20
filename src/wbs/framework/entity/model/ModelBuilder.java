package wbs.framework.entity.model;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.camelToUnderscore;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.nullIfEmptyString;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.uncapitalise;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.entity.annotations.meta.EntityMeta;
import wbs.framework.entity.annotations.meta.EntityMetaCreate;
import wbs.framework.entity.annotations.meta.EntityMetaMutable;
import wbs.framework.entity.annotations.meta.EntityMetaTable;
import wbs.framework.entity.annotations.meta.FieldMeta;
import wbs.framework.entity.annotations.meta.FieldMetaColumn;
import wbs.framework.entity.annotations.meta.FieldMetaColumns;
import wbs.framework.entity.annotations.meta.FieldMetaCounter;
import wbs.framework.entity.annotations.meta.FieldMetaElement;
import wbs.framework.entity.annotations.meta.FieldMetaField;
import wbs.framework.entity.annotations.meta.FieldMetaHibernateTypeHelper;
import wbs.framework.entity.annotations.meta.FieldMetaIndex;
import wbs.framework.entity.annotations.meta.FieldMetaKey;
import wbs.framework.entity.annotations.meta.FieldMetaNullable;
import wbs.framework.entity.annotations.meta.FieldMetaOrderBy;
import wbs.framework.entity.annotations.meta.FieldMetaSequence;
import wbs.framework.entity.annotations.meta.FieldMetaSqlType;
import wbs.framework.entity.annotations.meta.FieldMetaTable;
import wbs.framework.entity.annotations.meta.FieldMetaWhere;
import wbs.framework.schema.helper.SchemaNamesHelper;
import wbs.framework.schema.helper.SchemaTypesHelper;

@Accessors (fluent = true)
@Log4j
@PrototypeComponent ("modelBuilder")
public
class ModelBuilder {

	@Inject
	SchemaNamesHelper schemaNamesHelper;

	@Inject
	SchemaTypesHelper schemaTypesHelper;

	@Getter @Setter
	Class<?> objectClass;

	Model model;

	public
	Model build () {

		model =
			new Model ();

		Annotation entityAnnotation =
			findAnnotation (
				objectClass.getDeclaredAnnotations (),
				EntityMeta.class);

		if (entityAnnotation == null) {

			log.error (
				stringFormat (
					"No entity annotation found on %s",
					objectClass ().getName ()));

			return null;

		}

		// object class

		model.objectClass (
			objectClass);

		// object name

		Pattern classNamePattern =
			Pattern.compile ("(.+)(Rec|View)");

		Matcher classNameMatcher =
			classNamePattern.matcher (objectClass.getSimpleName ());

		if (! classNameMatcher.matches ())
			throw new RuntimeException ();

		model.objectName (
			uncapitalise (
				classNameMatcher.group (1)));

		// object type code

		model.objectTypeCode (
			camelToUnderscore (
				classNameMatcher.group (1)));

		// object type id

		// TODO or not maybe...

		// table name

		model.tableName (
			annotationStringParam (
				entityAnnotation,
				EntityMetaTable.class,
				schemaNamesHelper.tableName (
					objectClass)));

		// create

		model.create (
			(Boolean)
			annotationParam (
				entityAnnotation,
				EntityMetaCreate.class,
				true));

		// mutable

		model.mutable (
			(Boolean)
			annotationParam (
				entityAnnotation,
				EntityMetaMutable.class,
				true));

		// fields

		for (Field field
				: objectClass.getDeclaredFields ()) {

			Annotation fieldAnnotation =
				findAnnotation (
					field.getAnnotations (),
					FieldMeta.class);

			if (fieldAnnotation == null)
				continue;

			ModelField modelField =
				createField (
					null,
					field,
					fieldAnnotation);

			storeField (
				model,
				modelField);

		}

		// object helper class

		String objectHelperClassName =
			stringFormat (
				"%s.%sObjectHelper",
				objectClass.getPackage ().getName (),
				capitalise (model.objectName ()));

		try {

			Class<?> objectHelperClass =
				Class.forName (objectHelperClassName);

			model.helperClass (
				objectHelperClass);

		} catch (ClassNotFoundException exception) {

			log.warn (
				stringFormat (
					"Object helper class %s not found for %s",
					objectHelperClassName,
					model.objectName ()));

		}

		// and return

		return model;

	}

	ModelField createField (
			ModelField parentModelField,
			Field field,
			Annotation fieldAnnotation) {

		FieldMeta fieldMetaAnnotation =
			fieldAnnotation
				.annotationType ()
				.getAnnotation (FieldMeta.class);

		ModelField modelField =
			new ModelField ()

			.model (
				model)

			.parentField (
				parentModelField)

			.name (
				field.getName ())

			.label (
				camelToSpaces (
					field.getName ()))

			.type (
				fieldMetaAnnotation.modelFieldType ())

			.parent (
				fieldMetaAnnotation.treeParent ())

			.identity (
				fieldMetaAnnotation.treeIdentity ())

			.valueType (
				field.getType ())

			.field (
				field)

			.annotation (
				fieldAnnotation);

		// collection types

		if (field.getGenericType () instanceof ParameterizedType) {

			modelField.parameterizedType (
				(ParameterizedType)
				field.getGenericType ());

		}

		if (modelField.valueType () == Set.class) {

			modelField.collectionKeyType (
				modelField.parameterizedType ()
					.getActualTypeArguments () [0]);

			modelField.collectionValueType (
				modelField.parameterizedType ()
					.getActualTypeArguments () [0]);

		}

		if (modelField.valueType () == Map.class) {

			modelField.collectionKeyType (
				modelField.parameterizedType ()
					.getActualTypeArguments () [0]);

			modelField.collectionValueType (
				modelField.parameterizedType ()
					.getActualTypeArguments () [1]);

		}

		if (modelField.valueType () == List.class) {

			modelField.collectionKeyType (
				Integer.class);

			modelField.collectionValueType (
				modelField.parameterizedType ()
					.getActualTypeArguments () [0]);

		}

		// foreign field

		modelField.foreignFieldName (
			nullIfEmptyString (
				(String)
				annotationParam (
					fieldAnnotation,
					FieldMetaField.class,
					"")));

		Field foreignField = null;

		if (modelField.foreignFieldName () != null) {

			try {

				foreignField =
					objectClass.getDeclaredField (
						modelField.foreignFieldName ());

			} catch (NoSuchFieldException exception) {

				log.error (
					stringFormat (
						"Foreign field %s.%s doesn't exist",
						model.objectName (),
						modelField.foreignFieldName ()));

				return null;

			}

		}

		// sequence name

		if (modelField.generatedId ()) {

			modelField.sequenceName (
				annotationStringParam (
					fieldAnnotation,
					FieldMetaSequence.class,
					schemaNamesHelper.idSequenceName (
						model.objectClass ())));

		}

		// column names

		String[] annotationColumnNames =
			(String[])
			annotationParam (
				fieldAnnotation,
				FieldMetaColumns.class,
				new String [] {});

		String annotationColumnName =
			(String)
			annotationParam (
				fieldAnnotation,
				FieldMetaColumn.class,
				"");

		if (annotationColumnNames.length > 0
				&& ! annotationColumnName.isEmpty ())
			throw new RuntimeException ();

		if (annotationColumnNames.length > 0) {

			modelField.columnNames (
				Arrays.asList (annotationColumnNames));

		} else if (! annotationColumnName.isEmpty ()) {

			modelField.columnNames (
				Collections.singletonList (
					annotationColumnName));

		} else if (foreignField != null) {

			modelField.columnNames (
				Collections.singletonList (
					schemaNamesHelper.idColumnName (
						foreignField)));

		} else if (modelField.reference ()) {

			modelField.columnNames (
				Collections.singletonList (
					schemaNamesHelper.idColumnName (field)));

		} else if (modelField.value ()) {

			modelField.columnNames (
				Collections.singletonList (
					schemaNamesHelper.columnName (field)));

		}

		// type names

		Class<?> annotationHibernateTypeHelper =
			(Class<?>)
			annotationParam (
				fieldAnnotation,
				FieldMetaHibernateTypeHelper.class,
				Object.class);

		if (annotationHibernateTypeHelper != Object.class) {

			modelField.hibernateTypeHelper (
				annotationHibernateTypeHelper.getName ());

		}

		// other misc files

		modelField.sqlType (
			annotationStringParam (
				fieldAnnotation,
				FieldMetaSqlType.class,
				null));

		modelField.nullable (
			(Boolean) annotationParam (
				fieldAnnotation,
				FieldMetaNullable.class,
				false));

		modelField.orderBy (
			annotationStringParam (
				fieldAnnotation,
				FieldMetaOrderBy.class,
				null));

		modelField.where (
			annotationStringParam (
				fieldAnnotation,
				FieldMetaWhere.class,
				null));

		modelField.key (
			annotationStringParam (
				fieldAnnotation,
				FieldMetaKey.class,
				null));

		modelField.index (
			annotationStringParam (
				fieldAnnotation,
				FieldMetaIndex.class,
				null));

		modelField.element (
			annotationStringParam (
				fieldAnnotation,
				FieldMetaElement.class,
				null));

		modelField.table (
			annotationStringParam (
				fieldAnnotation,
				FieldMetaTable.class,
				null));

		modelField.counter (
			annotationStringParam (
				fieldAnnotation,
				FieldMetaCounter.class,
				null));

		// member fields

		if (modelField.composite ()) {

			Class<?> componentClass =
				modelField.valueType ();

			for (
				Field componentField
					: componentClass.getDeclaredFields ()
			) {

				Annotation componentFieldAnnotation =
					findAnnotation (
						componentField.getAnnotations (),
						FieldMeta.class);

				if (componentFieldAnnotation == null)
					continue;

				ModelField componentModelField =
					createField (
						modelField,
						componentField,
						componentFieldAnnotation);

				modelField.fields.add (
					componentModelField);

				modelField.fieldsByName.put (
					componentModelField.name (),
					componentModelField);

			}

		}

		return modelField;

	}

	void storeField (
			Model model,
			ModelField modelField) {

		model.fields ().add (
			modelField);

		model.fieldsByName ().put (
			modelField.name (),
			modelField);

		if (
			in (modelField.type (),
				ModelFieldType.assignedId,
				ModelFieldType.generatedId,
				ModelFieldType.foreignId,
				ModelFieldType.compositeId)
		) {

			if (model.idField () != null)
				throw new RuntimeException ();

			model.idField (modelField);

		}

		if (
			in (modelField.type (),
				ModelFieldType.parent,
				ModelFieldType.master)
		) {

			if (model.parentField () != null)
				throw new RuntimeException ();

			model.parentField (modelField);

		}

		if (modelField.type () == ModelFieldType.parentType) {

			if (model.parentTypeField () != null)
				throw new RuntimeException ();

			model.parentTypeField (modelField);

		}

		if (modelField.type () == ModelFieldType.parentId) {

			if (model.parentIdField () != null)
				throw new RuntimeException ();

			model.parentIdField (modelField);

		}

		if (modelField.type () == ModelFieldType.typeCode) {

			if (model.typeCodeField () != null)
				throw new RuntimeException ();

			model.typeCodeField (modelField);

		}

		if (modelField.type () == ModelFieldType.code) {

			if (model.codeField () != null)
				throw new RuntimeException ();

			model.codeField (modelField);

		}

		if (modelField.type () == ModelFieldType.index) {

			if (model.indexField () != null)
				throw new RuntimeException ();

			model.indexField (modelField);

		}

		if (modelField.type () == ModelFieldType.name) {

			if (model.nameField () != null)
				throw new RuntimeException ();

			model.nameField (modelField);

		}

		if (modelField.type () == ModelFieldType.description) {

			if (model.descriptionField () != null)
				throw new RuntimeException ();

			model.descriptionField (modelField);

		}

		if (modelField.type () == ModelFieldType.deleted) {

			if (model.deletedField () != null)
				throw new RuntimeException ();

			model.deletedField (modelField);

		}

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
