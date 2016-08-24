package wbs.framework.entity.generate.fields;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.DateFieldSpec;
import wbs.framework.entity.meta.PropertyWriter;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("dateFieldWriter")
@ModelWriter
public
class DateFieldWriter {

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	DateFieldSpec spec;

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
				"LocalDate")

			.propertyNameFormat (
				"%s",
				spec.name ())

			.write (
				javaWriter,
				"\t");

	}

}
