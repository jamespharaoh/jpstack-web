package wbs.framework.entity.generate.fields;

import static wbs.utils.etc.Misc.getStaticMethodRequired;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import org.joda.time.Instant;
import org.joda.time.ReadableInstant;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.fields.TimestampFieldSpec;
import wbs.utils.time.TimeUtils;

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
	ModelFieldWriterTarget target;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		// write field

		new JavaPropertyWriter ()

			.thisClassNameFormat (
				"%s.model.%s",
				context.modelMeta ().plugin ().packageName (),
				context.recordClassName ())

			.propertyNameFormat (
				"%s",
				spec.name ())

			.typeClass (
				Instant.class)

			.setterTypeClass (
				ReadableInstant.class)

			.setterConversion (
				getStaticMethodRequired (
					TimeUtils.class,
					"toInstantNullSafe",
					ImmutableList.of (
						ReadableInstant.class)))

			.writeBlock (
				target.imports (),
				target.formatWriter ());

	}

}
