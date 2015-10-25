package wbs.framework.entity.build;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.camelToUnderscore;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.orNull;

import java.util.Date;
import java.util.Map;

import org.jadira.usertype.dateandtime.joda.PersistentInstantAsString;
import org.jadira.usertype.dateandtime.joda.PersistentInstantAsTimestamp;
import org.joda.time.Instant;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.TimestampFieldSpec;
import wbs.framework.entity.meta.TimestampFieldSpec.ColumnType;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;

@PrototypeComponent ("timestampModelFieldBuilder")
@ModelBuilder
public
class TimestampModelFieldBuilder {

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	TimestampFieldSpec spec;

	@BuilderTarget
	ModelFieldBuilderTarget target;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String fieldName =
			spec.name ();

		// create model field

		ModelField modelField =
			new ModelField ()

			.model (
				target.model ())

			.parentField (
				context.parentModelField ())

			.name (
				fieldName)

			.label (
				camelToSpaces (
					fieldName))

			.type (
				ModelFieldType.simple)

			.parent (
				false)

			.identity (
				false)

			.valueType (
				valueTypeByColumnType.get (
					spec.columnType ()))

			.nullable (
				ifNull (
					spec.nullable (),
					false))

			.columnNames (
				ImmutableList.<String>of (
					ifNull (
						spec.columnName (),
						camelToUnderscore (
							fieldName))))

			.hibernateTypeHelper (
				orNull (
					hibernateTypeHelperByColumnType.get (
						spec.columnType ())))

			.sqlType (
				orNull (
					sqlTypeByColumnType.get (
						spec.columnType ())));

		// store field

		target.fields ().add (
			modelField);

		target.fieldsByName ().put (
			modelField.name (),
			modelField);

		if (target.model ().timestampField () == null) {

			target.model ().timestampField (
				modelField);

		}

	}

	public final static
	Map<ColumnType,Optional<Class<?>>> hibernateTypeHelperByColumnType =
		ImmutableMap.<ColumnType,Optional<Class<?>>>builder ()

		.put (
			ColumnType.sql,
			Optional.<Class<?>>of (
				PersistentInstantAsTimestamp.class))

		.put (
			ColumnType.postgresql,
			Optional.<Class<?>>absent ())

		.put (
			ColumnType.iso,
			Optional.<Class<?>>of (
				PersistentInstantAsString.class))

		.build ();

	public final static
	Map<ColumnType,Optional<String>> sqlTypeByColumnType =
		ImmutableMap.<ColumnType,Optional<String>>builder ()

		.put (
			ColumnType.sql,
			Optional.<String>absent ())

		.put (
			ColumnType.postgresql,
			Optional.<String>of (
				"timestamp with time zone"))

		.put (
			ColumnType.iso,
			Optional.<String>absent ())

		.build ();

	public final static
	Map<ColumnType,Class<?>> valueTypeByColumnType =
		ImmutableMap.<ColumnType,Class<?>>builder ()

		.put (
			ColumnType.sql,
			Instant.class)

		.put (
			ColumnType.iso,
			Instant.class)

		.put (
			ColumnType.postgresql,
			Date.class)

		.build ();

}
