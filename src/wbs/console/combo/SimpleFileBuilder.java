package wbs.console.combo;

import static wbs.utils.etc.OptionalUtils.optionalFromNullable;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.console.responder.ConsoleFile;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
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

@PrototypeComponent ("simpleFileBuilder")
@ConsoleModuleBuilderHandler
public
class SimpleFileBuilder
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFileProvider;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer simpleContainerSpec;

	@BuilderSource
	SimpleFileSpec simpleFileSpec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String path;
	String getResponderName;
	String getActionName;
	String postResponderName;
	String postActionName;

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

			setDefaults ();

			buildFile (
				taskLogger);

		}

	}

	void buildFile (
			@NonNull TaskLogger parentTaskLogger) {

		consoleModule.addFile (
			path,
			consoleFileProvider.get ()

				.getResponderName (
					getResponderName)

				.getActionName (
					parentTaskLogger,
					optionalFromNullable (
						getActionName))

				.postActionName (
					parentTaskLogger,
					optionalFromNullable (
						postActionName)));

	}

	// defaults

	void setDefaults () {

		path =
			simpleFileSpec.path ();

		getResponderName =
			simpleFileSpec.getResponderName ();

		getActionName =
			simpleFileSpec.getActionName ();

		postResponderName =
			simpleFileSpec.postResponderName ();

		postActionName =
			simpleFileSpec.postActionName ();

	}

}
