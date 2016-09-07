package wbs.framework.entity.build;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.camelToUnderscore;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.meta.ids.ForeignIdFieldSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.schema.helper.SchemaNamesHelper;

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
				Long.class)

			.nullable (
				false)

			.foreignFieldName (
				spec.fieldName ())

			.columnNames (
				ImmutableList.of (
					ifNull (
						spec.columnName (),
						stringFormat (
							"%s_id",
							camelToUnderscore (
								spec.fieldName ())))));

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
