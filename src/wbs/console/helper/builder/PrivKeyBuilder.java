package wbs.console.helper.builder;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.spec.PrivKeySpec;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("privKeyBuilder")
@ConsoleModuleBuilderHandler
public
class PrivKeyBuilder {

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	PrivKeySpec spec;

	@BuilderTarget
	Object target;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

	}

}
