package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.ifNull;

import java.io.IOException;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.IndexFieldSpec;
import wbs.framework.entity.meta.ModelMetaSpec;
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
			Builder builder)
		throws IOException {

		if (spec.counterName () != null) {

			javaWriter.write (

				"\t@IndexField (\n",

				"\t\tcounter = \"%s\")\n",
				spec.counterName ());

		} else {

			javaWriter.write (

				"\t@IndexField\n");

		}

		javaWriter.write (

			"\tInteger %s;\n",
			ifNull (
				spec.name (),
				"index"),

			"\n");

	}

}
