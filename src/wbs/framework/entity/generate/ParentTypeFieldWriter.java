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
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.ParentTypeFieldSpec;

@PrototypeComponent ("parentTypeFieldWriter")
@ModelWriter
public
class ParentTypeFieldWriter {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	ParentTypeFieldSpec spec;

	@BuilderTarget
	Writer javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		// TODO this should not be hardcoded, but not sure what instead

		javaWriter.write (
			stringFormat (

				"\t@ParentTypeField\n",

				"\twbs.platform.object.core.model.ObjectTypeRec parentType;\n",

				"\n"));

	}

}
