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
import wbs.framework.entity.meta.TimestampFieldSpec;
import wbs.framework.entity.meta.TimestampFieldSpec.ColumnType;
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

		boolean nullable =
			ifNull (
				spec.nullable (),
				false);

		// write annotation

		if (nullable || spec.columnType () != ColumnType.postgresql) {

			javaWriter.write (
				"\t@SimpleField (\n");

		} else {

			javaWriter.write (
				"\t@SimpleField\n");

		}

		// write nullable

		if (nullable) {

			javaWriter.write (
				"\t\tnullable = true,\n");

		}

		// write type helper

		switch (spec.columnType ()) {

		case sql:

			javaWriter.write (
				"\t\thibernateTypeHelper = PersistentInstantAsTimestamp.class)\n");

			break;

		case postgresql:

			break;

		case iso:

			javaWriter.write (
				"\t\thibernateTypeHelper = PersistentInstantAsString.class)\n");

			break;

		default:

			throw new RuntimeException (
				spec.columnType ().toString ());

		}

		// write member

		switch (spec.columnType ()) {

		case sql:
		case iso:

			javaWriter.write (
				"\tInstant %s;\n",
				spec.name ());

			break;

		case postgresql:

			javaWriter.write (
				"\tDate %s;\n",
				spec.name ());

			break;

		default:

			throw new RuntimeException ();

		}

		// write blank line

		javaWriter.write (
			"\n");

	}

}
