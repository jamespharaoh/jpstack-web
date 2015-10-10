package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.Writer;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.YesNoFieldSpec;

@PrototypeComponent ("yesNoFieldWriter")
@ModelWriter
public
class YesNoFieldWriter {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	YesNoFieldSpec spec;

	@BuilderTarget
	Writer javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		if (ifNull (spec.nullable (), false)) {

			javaWriter.write (
				stringFormat (

					"\t@SimpleField (\n",

					"\t\tnullable = true)\n"));

		} else {

			javaWriter.write (
				stringFormat (

					"\t@SimpleField\n"));

		}

		javaWriter.write (
			stringFormat (

				"\tBoolean %s;\n",
				spec.name ()));

		javaWriter.write (
			stringFormat (

				"\n"));

	}

}
