package wbs.console.context;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("consoleContextStuffBuilder")
@ConsoleModuleBuilderHandler
public
class ConsoleContextStuffBuilder {

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	ConsoleContextStuffSpec spec;

	@BuilderTarget
	Object target;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

	}

}
