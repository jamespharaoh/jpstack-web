package wbs.console.supervisor;

import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.part.PagePart;
import wbs.console.part.TextPart;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorTableSeparatorBuilder")
public
class SupervisorTableSeparatorBuilder
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextPart> textPartProvider;

	// builder

	@BuilderParent
	SupervisorConfigSpec container;

	@BuilderSource
	SupervisorTableSeparatorSpec spec;

	@BuilderTarget
	SupervisorTablePartBuilder supervisorTablePartBuilder;

	// build

	@Override
	@BuildMethod
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

			Provider <PagePart> pagePartFactory =
				() -> textPartProvider.get ()

				.text (
					stringFormat (
						"<tr class=\"sep\"></tr>\n"))

			;

			supervisorTablePartBuilder.pagePartFactories ().add (
				pagePartFactory);

		}

	}

}
