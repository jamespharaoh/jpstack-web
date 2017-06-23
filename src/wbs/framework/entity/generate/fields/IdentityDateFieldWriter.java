package wbs.framework.entity.generate.fields;

import lombok.NonNull;

import org.joda.time.LocalDate;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.generate.ModelRecordGenerator;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.identities.IdentityDateFieldSpec;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("identityDateFieldWriter")
@ModelWriter
public
class IdentityDateFieldWriter
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	IdentityDateFieldSpec spec;

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

				.typeClass (
					LocalDate.class)

				.propertyName (
					spec.name ())

				.setUpdatedFieldName (
					ModelRecordGenerator.recordUpdatedFieldName)

				.writeBlock (
					taskLogger,
					target.imports (),
					target.formatWriter ())

			;

		}

	}

}
