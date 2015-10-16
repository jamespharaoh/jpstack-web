package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.ifNull;

import java.io.IOException;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.AnnotationWriter;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.TimestampFieldSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("timestampFieldWriter")
@ModelWriter
public
class TimestampFieldWriter {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	TimestampFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		// write field annotation

		AnnotationWriter annotationWriter =
			new AnnotationWriter ()

			.name (
				"SimpleField");

		if (ifNull (spec.nullable (), false)) {

			annotationWriter.addAttributeFormat (
				"nullable",
				"true");

		}

		switch (spec.columnType ()) {

		case sql:

			annotationWriter.addAttributeFormat (
				"hibernateTypeHelper",
				"PersistentInstantAsTimestamp.class");

			break;

		case postgresql:

			break;

		case iso:

			annotationWriter.addAttributeFormat (
				"hibernateTypeHelper",
				"PersistentInstantAsString.class");

			break;

		default:

			throw new RuntimeException (
				spec.columnType ().toString ());

		}

		if (spec.columnName () != null) {

			annotationWriter.addAttributeFormat (
				"column",
				"\"%s\"",
				spec.columnName ().replace ("\"", "\\\""));

		}

		annotationWriter.write (
			javaWriter,
			"\t");

		// write member

		switch (spec.columnType ()) {

		case sql:
		case iso:

			javaWriter.writeFormat (
				"\tInstant %s;\n",
				spec.name ());

			break;

		case postgresql:

			javaWriter.writeFormat (
				"\tDate %s;\n",
				spec.name ());

			break;

		default:

			throw new RuntimeException ();

		}

		// write blank line

		javaWriter.writeFormat (
			"\n");

	}

}
