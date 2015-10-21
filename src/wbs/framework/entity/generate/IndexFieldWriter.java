package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.IndexFieldSpec;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.PropertyWriter;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("indexFieldWriter")
@ModelWriter
public
class IndexFieldWriter {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	IndexFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		if (spec.counterName () != null) {

			javaWriter.writeFormat (
				"\t@IndexField (\n");

			javaWriter.writeFormat (
				"\t\tcounter = \"%s\")\n",
				spec.counterName ());

		} else {

			javaWriter.writeFormat (
				"\t@IndexField\n");

		}

		// write field

		new PropertyWriter ()

			.thisClassNameFormat (
				"%sRec",
				capitalise (
					parent.name ()))

			.typeNameFormat (
				"Integer")

			.propertyNameFormat (
				"%s",
				ifNull (
					spec.name (),
					"index"))

			.write (
				javaWriter,
				"\t");

	}

}
