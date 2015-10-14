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
import wbs.framework.entity.meta.StringFieldSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("stringFieldWriter")
@ModelWriter
public
class StringFieldWriter {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	StringFieldSpec spec;

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

		if (spec.columnName () != null) {

			annotationWriter.addAttributeFormat (
				"column",
				"\"%s\"",
				spec.columnName ().replace ("\"", "\\\""));

		}

		annotationWriter.write (
			javaWriter,
			"\t");

		// write field

		if (spec.defaultValue () == null) {

			javaWriter.write (
				"\tString %s;\n",
				spec.name ());

		} else if (spec.defaultValue ().isEmpty ()) {

			javaWriter.write (
				"\tString %s = \"\";\n",
				spec.name ());

		} else {

			javaWriter.write (
				"\tString %s =\n",
				spec.name ());

			javaWriter.write (
				"\t\t\"%s\";\n",
				spec.defaultValue ().replace ("\"", "\\\""));

		}

		javaWriter.write (
			"\n");

	}

}
