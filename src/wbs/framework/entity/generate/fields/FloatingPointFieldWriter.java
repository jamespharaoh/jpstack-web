package wbs.framework.entity.generate.fields;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.FloatingPointFieldSpec;
import wbs.framework.entity.meta.PropertyWriter;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("floatingPointFieldWriter")
@ModelWriter
public
class FloatingPointFieldWriter {

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	FloatingPointFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		// write field

		PropertyWriter propertyWriter =
			new PropertyWriter ()

			.thisClassNameFormat (
				"%s",
				context.recordClassName ())

			.typeNameFormat (
				"Double")

			.propertyNameFormat (
				"%s",
				spec.name ());

		if (spec.defaultValue () != null) {

			propertyWriter.defaultValueFormat (
				"%s",
				spec.defaultValue ());

		}

		propertyWriter.write (
			javaWriter,
			"\t");

	}

}
