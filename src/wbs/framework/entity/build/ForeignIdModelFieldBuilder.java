package wbs.framework.entity.build;

import static wbs.framework.utils.etc.Misc.camelToUnderscore;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.ForeignIdFieldSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.schema.helper.SchemaNamesHelper;

import com.google.common.collect.ImmutableList;

@PrototypeComponent ("foreignIdModelFieldBuilder")
@ModelBuilder
public
class ForeignIdModelFieldBuilder {

	// dependencies

	@Inject
	SchemaNamesHelper schemaNamesHelper;

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	ForeignIdFieldSpec spec;

	@BuilderTarget
	ModelFieldBuilderTarget target;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		// create model field

		ModelField modelField =
			new ModelField ()

			.model (
				target.model ())

			.parentField (
				context.parentModelField ())

			.name (
				"id")

			.label (
				"id")

			.type (
				ModelFieldType.foreignId)

			.parent (
				false)

			.identity (
				false)

			.valueType (
				Integer.class)

			.nullable (
				false)

			.foreignFieldName (
				spec.fieldName ())

			.columnNames (
				ImmutableList.<String>of (
					stringFormat (
						"%s_id",
						camelToUnderscore (
							spec.fieldName ()))));

		// store field

		target.fields ().add (
			modelField);

		target.fieldsByName ().put (
			modelField.name (),
			modelField);

		if (target.model ().idField () != null)
			throw new RuntimeException ();

		target.model ().idField (
			modelField);

	}

}
