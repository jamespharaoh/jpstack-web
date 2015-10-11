package wbs.framework.entity.generate;

import java.io.IOException;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.TimestampFieldSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("timestampFieldWriter")
@ModelWriter
public
class TimestampFieldWriter {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	TimestampFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		javaWriter.write (

			"\t@SimpleField (\n");

		switch (spec.columnType ()) {

		case timestamp:

			javaWriter.write (

				"\t\thibernateTypeHelper = PersistentInstantAsTimestamp.class)\n");

			break;

		default:

			throw new RuntimeException ();

		}

		javaWriter.write (

			"\tInstant %s;\n",
			spec.name (),

			"\n");

	}

}
