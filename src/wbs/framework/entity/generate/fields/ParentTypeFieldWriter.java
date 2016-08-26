package wbs.framework.entity.generate.fields;

import static wbs.framework.utils.etc.NullUtils.ifNull;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.ParentTypeFieldSpec;
import wbs.framework.utils.formatwriter.FormatWriter;

@PrototypeComponent ("parentTypeFieldWriter")
@ModelWriter
public
class ParentTypeFieldWriter {

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	ParentTypeFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		// write field

		// TODO this class name should not be hard-coded

		JavaPropertyWriter propertyWriter =
			new JavaPropertyWriter ()

			.thisClassNameFormat (
				"%s",
				context.recordClassName ())

			.typeNameFormat (
				"wbs.platform.object.core.model.ObjectTypeRec")

			.propertyNameFormat (
				"%s",
				ifNull (
					spec.name (),
					"parentType"));

		propertyWriter.write (
			javaWriter,
			"\t");

	}

}
