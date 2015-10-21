package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.capitalise;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.ForeignIdFieldSpec;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.PropertyWriter;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("foreignIdFieldWriter")
@ModelWriter
public
class ForeignIdFieldWriter {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	ForeignIdFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		javaWriter.writeFormat (
			"\t@ForeignIdField (\n");

		javaWriter.writeFormat (
			"\t\tfield = \"%s\")\n",
			spec.fieldName ().replace ("\"", "\\\""));

		// write field

		new PropertyWriter ()

			.thisClassNameFormat (
				"%sRec",
				capitalise (
					parent.name ()))

			.typeNameFormat (
				"Integer")

			.propertyNameFormat (
				"id")

			.write (
				javaWriter,
				"\t");

	}

}
