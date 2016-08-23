package wbs.framework.entity.build;

import static wbs.framework.utils.etc.NullUtils.ifNull;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.GeneratedIdFieldSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.schema.helper.SchemaNamesHelper;

@PrototypeComponent ("generatedIdModelFieldBuilder")
@ModelBuilder
public
class GeneratedIdModelFieldBuilder {

	// dependencies

	@Inject
	SchemaNamesHelper schemaNamesHelper;

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	GeneratedIdFieldSpec spec;

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
				ModelFieldType.generatedId)

			.parent (
				false)

			.identity (
				false)

			.valueType (
				Long.class)

			.nullable (
				false)

			.columnNames (
				ImmutableList.of (
					"id"))

			.sequenceName (
				ifNull (
					spec.sequenceName (),
					schemaNamesHelper.idSequenceName (
						context.recordClass ())));

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
