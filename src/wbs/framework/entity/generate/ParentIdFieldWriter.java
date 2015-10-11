package wbs.framework.entity.generate;

import java.io.IOException;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
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

		// TODO this should not be hardcoded, but not sure what instead

		javaWriter.write (

			"\t@ParentIdField\n",

			"\tInteger parentId;\n",

			"\n");

	}

}
