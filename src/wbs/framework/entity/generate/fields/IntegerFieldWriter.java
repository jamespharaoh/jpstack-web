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
import wbs.framework.entity.meta.IntegerFieldSpec;

@PrototypeComponent ("integerFieldWriter")
@ModelWriter
public
class IntegerFieldWriter {

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	IntegerFieldSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		// write field

		JavaPropertyWriter propertyWriter =
			new JavaPropertyWriter ()

			.thisClassNameFormat (
				"%s.model.%s",
				context.modelMeta ().plugin ().packageName (),
				context.recordClassName ())

			.typeClass (
				Long.class)

			.propertyName (
				spec.name ());

		if (spec.defaultValue () != null) {

			propertyWriter.defaultValueFormat (
				"%sl",
				Long.toString (
					spec.defaultValue ()));

		}

		propertyWriter.writeBlock (
			target.imports (),
			target.formatWriter ());

	}

}
