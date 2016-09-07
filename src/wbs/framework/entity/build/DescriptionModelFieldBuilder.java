package wbs.framework.entity.build;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.camelToUnderscore;

import com.google.common.collect.ImmutableList;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.meta.fields.DescriptionFieldSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;

@PrototypeComponent ("descriptionModelFieldBuilder")
@ModelBuilder
public
class DescriptionModelFieldBuilder {

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	DescriptionFieldSpec spec;

	@BuilderTarget
	ModelFieldBuilderTarget target;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String fieldName =
			ifNull (
				spec.name (),
				"description");

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
				ModelFieldType.description)

			.parent (
				false)

			.identity (
				false)

			.valueType (
				String.class)

			.nullable (
				false)

			.columnNames (
				ImmutableList.<String>of (
					ifNull (
						spec.columnName (),
						camelToUnderscore (
							fieldName))));

		// store field

		target.fields ().add (
			modelField);

		target.fieldsByName ().put (
			modelField.name (),
			modelField);

		if (target.model ().descriptionField () != null)
			throw new RuntimeException ();

		target.model ().descriptionField (
			modelField);

	}

}
