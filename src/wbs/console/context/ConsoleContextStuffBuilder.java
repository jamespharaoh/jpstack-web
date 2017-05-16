package wbs.console.context;

import static wbs.utils.etc.Misc.doNothing;

import lombok.NonNull;

import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.SimpleConsoleBuilderContainer;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("consoleContextStuffBuilder")
public
class ConsoleContextStuffBuilder
	implements ConsoleModuleBuilderComponent {

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	ConsoleContextStuffSpec spec;

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
