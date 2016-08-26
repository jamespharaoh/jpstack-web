package wbs.framework.entity.generate.fields;

import static wbs.framework.utils.etc.Misc.getStaticMethodRequired;
import lombok.NonNull;

import org.joda.time.ReadableInstant;

import com.google.common.collect.ImmutableList;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.TimestampFieldSpec;
import wbs.framework.utils.etc.TimeUtils;
import wbs.framework.utils.formatwriter.FormatWriter;

@PrototypeComponent ("timestampFieldWriter")
@ModelWriter
public
class TimestampFieldWriter {

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	TimestampFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		// write field

		JavaPropertyWriter propertyWriter =
			new JavaPropertyWriter ()

			.thisClassNameFormat (
				"%s",
				context.recordClassName ())

			.propertyNameFormat (
				"%s",
				spec.name ())

			.typeNameFormat (
				"Instant")

			.setterTypeNameFormat (
				"ReadableInstant")

			.setterConversion (
				getStaticMethodRequired (
					TimeUtils.class,
					"toInstantNullSafe",
					ImmutableList.of (
						ReadableInstant.class)));

		propertyWriter.write (
			javaWriter,
			"\t");

	}

}
