package wbs.console.helper.builder;

import static wbs.utils.etc.Misc.doNothing;

import lombok.NonNull;

import wbs.console.helper.provider.PrivKeySpec;
import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.SimpleConsoleBuilderContainer;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("privKeyBuilder")
public
class PrivKeyBuilder
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	PrivKeySpec spec;

	@BuilderTarget
	Object target;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		doNothing ();

	}

}
