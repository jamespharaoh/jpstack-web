package wbs.console.context;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;

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
