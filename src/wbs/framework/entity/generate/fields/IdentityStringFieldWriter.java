package wbs.framework.entity.generate.fields;

import lombok.NonNull;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.identities.IdentityStringFieldSpec;

@PrototypeComponent ("identityStringFieldWriter")
@ModelWriter
public
class IdentityStringFieldWriter {

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	IdentityStringFieldSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		// write field

		new JavaPropertyWriter ()

			.thisClassNameFormat (
				"%s.model.%s",
				context.modelMeta ().plugin ().packageName (),
				context.recordClassName ())

			.typeClass (
				String.class)

			.propertyName (
				spec.name ())

			.writeBlock (
				target.imports (),
				target.formatWriter ());

	}

}
