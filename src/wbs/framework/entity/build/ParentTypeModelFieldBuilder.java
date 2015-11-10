package wbs.framework.entity.build;

import static wbs.framework.utils.etc.Misc.classForNameRequired;
import static wbs.framework.utils.etc.Misc.ifNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.ParentTypeFieldSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;

import com.google.common.collect.ImmutableList;

@PrototypeComponent ("parentTypeModelFieldBuilder")
@ModelBuilder
public
class ParentTypeModelFieldBuilder {

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	ParentTypeFieldSpec spec;

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
				"parentType")

			.label (
				"parent type")

			.type (
				ModelFieldType.parentType)

			.parent (
				true)

			.identity (
				false)

			// TODO this should noe be hard-coded

			.valueType (
				classForNameRequired (
					"wbs.platform.object.core.model.ObjectTypeRec"))

			.nullable (
				false)

			.columnNames (
				ImmutableList.<String>of (
					ifNull (
						spec.columnName (),
						"parent_type_id")));

		// store field

		target.fields ().add (
			modelField);

		target.fieldsByName ().put (
			modelField.name (),
			modelField);

		if (target.model ().parentTypeField () != null)
			throw new RuntimeException ();

		target.model ().parentTypeField (
			modelField);

	}

}
