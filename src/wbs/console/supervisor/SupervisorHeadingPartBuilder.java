package wbs.console.supervisor;

import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.part.PagePartFactory;
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

@PrototypeComponent ("supervisorHeadingPartBuilder")
public
class SupervisorHeadingPartBuilder
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextPart> textPartProvider;

	// builder

	@BuilderParent
	SupervisorConfigSpec supervisorConfigSpec;

	@BuilderSource
	SupervisorHeadingPartSpec supervisorHeadingPartSpec;

	@BuilderTarget
	SupervisorConfigBuilder supervisorConfigBuilder;

	// state

	String label;
	String text;

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

			label =
				supervisorHeadingPartSpec.label ();

			text =
				stringFormat (
					"<h2>%h</h2>\n",
					label);

			PagePartFactory pagePartFactory =
				nextTaskLogger ->
					textPartProvider.get ()

				.text (
					text);

			supervisorConfigBuilder.pagePartFactories ().add (
				pagePartFactory);

		}

	}

}
