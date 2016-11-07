package wbs.console.helper.provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.spec.ConsoleHelperProviderSpec;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.object.ObjectManager;

@PrototypeComponent ("consoleHelperProviderBuilder")
@ConsoleModuleBuilderHandler
public
class ConsoleHelperProviderBuilder {

	// singleton dependencies

	@SingletonDependency
	ObjectManager objectManager;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	ConsoleHelperProviderSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

	}

}
