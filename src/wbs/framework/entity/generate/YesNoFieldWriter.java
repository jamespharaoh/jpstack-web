package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.ifNull;

import java.io.IOException;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.YesNoFieldSpec;
import wbs.framework.utils.etc.FormatWriter;

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
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		if (ifNull (spec.nullable (), false)) {

			javaWriter.write (
				"\t@SimpleField (\n");

			javaWriter.write (
				"\t\tnullable = true)\n");

		} else {

			javaWriter.write (
				"\t@SimpleField\n");

		}

		if (spec.defaultValue () != null) {

			javaWriter.write (
				"\tBoolean %s = %s;\n",
				spec.name (),
				spec.defaultValue ()
					? "true"
					: "false");

		} else {

			javaWriter.write (
				"\tBoolean %s;\n",
				spec.name ());

		}

		javaWriter.write (
			"\n");

	}

}
