package wbs.framework.entity.generate.fields;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.BinaryFieldSpec;
import wbs.framework.entity.meta.PropertyWriter;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("binaryFieldWriter")
@ModelWriter
public
class BinaryFieldWriter {

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	BinaryFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		// write field

		new PropertyWriter ()

			.thisClassNameFormat (
				"%s",
				context.recordClassName ())

			.typeNameFormat (
				"byte[]")

			.propertyNameFormat (
				"%s",
				spec.name ())

			.write (
				javaWriter,
				"\t");

	}

}

