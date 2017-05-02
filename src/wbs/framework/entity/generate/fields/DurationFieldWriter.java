package wbs.framework.entity.generate.fields;

import static wbs.utils.etc.ReflectionUtils.methodGetStaticRequired;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.ReadableDuration;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.fields.DurationFieldSpec;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.time.TimeUtils;

@PrototypeComponent ("durationFieldWriter")
@ModelWriter
public
class DurationFieldWriter
	implements BuilderComponent {

	// singleton component

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	DurationFieldSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

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
					Duration.class)

				.setterTypeClass (
					ReadableDuration.class)

				.setterConversion (
					methodGetStaticRequired (
						TimeUtils.class,
						"toDurationNullSafe",
						ImmutableList.of (
							ReadableDuration.class)))

				.writeBlock (
					taskLogger,
					target.imports (),
					target.formatWriter ());

		}

	}

}
