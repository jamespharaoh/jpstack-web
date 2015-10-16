package wbs.framework.entity.generate;

import java.io.IOException;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.IdentityStringFieldSpec;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("identityStringFieldWriter")
@ModelWriter
public
class IdentityStringFieldWriter {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	IdentityStringFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		javaWriter.writeFormat (
			"\t@IdentitySimpleField\n");

		javaWriter.writeFormat (
			"\tString %s;\n",
			spec.name ());

		javaWriter.writeFormat (
			"\n");

	}

}
