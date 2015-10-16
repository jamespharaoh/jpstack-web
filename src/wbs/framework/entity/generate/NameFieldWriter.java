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
import wbs.framework.entity.meta.NameFieldSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("nameFieldWriter")
@ModelWriter
public
class NameFieldWriter {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	NameFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		javaWriter.writeFormat (
			"\t@NameField\n");

		javaWriter.writeFormat (
			"\tString %s;\n",
			ifNull (
				spec.name (),
				"name"));

		javaWriter.writeFormat (
			"\n");

	}

}
