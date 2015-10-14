package wbs.framework.entity.generate;

import java.io.IOException;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.AnnotationWriter;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.ParentIdFieldSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("parentIdFieldWriter")
@ModelWriter
public
class ParentIdFieldWriter {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	ParentIdFieldSpec spec;

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
				"ParentIdField");

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

		javaWriter.write (
			"\tInteger parentId;\n");

		javaWriter.write (
			"\n");

	}

}
