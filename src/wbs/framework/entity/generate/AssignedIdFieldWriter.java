package wbs.framework.entity.generate;

import java.io.IOException;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.AnnotationWriter;
import wbs.framework.entity.meta.AssignedIdFieldSpec;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("assignedIdFieldWriter")
@ModelWriter
public
class AssignedIdFieldWriter {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	AssignedIdFieldSpec spec;

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
				"AssignedIdField");

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

		javaWriter.writeFormat (
			"\tInteger id;\n");

		javaWriter.writeFormat (
			"\n");

	}

}
