package wbs.framework.entity.generate;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.PropertyWriter;
import wbs.framework.entity.meta.TimestampFieldSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("timestampFieldWriter")
@ModelWriter
public
class TimestampFieldWriter {

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	TimestampFieldSpec spec;

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

			.propertyNameFormat (
				"%s",
				spec.name ());

		switch (spec.columnType ()) {

		case sql:
		case iso:

			propertyWriter.typeNameFormat (
				"Instant");

			break;

		case postgresql:

			propertyWriter.typeNameFormat (
				"Date");

			break;

		default:

			throw new RuntimeException ();

		}

		propertyWriter.write (
			javaWriter,
			"\t");

	}

}
