package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.Writer;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.GeneratedIdFieldSpec;
import wbs.framework.entity.meta.ModelMetaSpec;

@PrototypeComponent ("generatedIdFieldWriter")
@ModelWriter
public
class GeneratedIdFieldWriter {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	GeneratedIdFieldSpec spec;

	@BuilderTarget
	Writer javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		javaWriter.write (
			stringFormat (

				"\t@GeneratedIdField\n",

				"\tInteger id;\n",

				"\n"));

	}

}
