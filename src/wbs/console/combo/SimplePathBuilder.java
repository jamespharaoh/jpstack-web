package wbs.console.combo;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.web.PathHandler;

@PrototypeComponent ("simplePathBuilder")
@ConsoleModuleBuilderHandler
public
class SimplePathBuilder {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer simpleContainerSpec;

	@BuilderSource
	SimplePathSpec simplePathSpec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String path;
	String pathHandlerName;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		buildPath ();

	}

	void buildPath () {

		PathHandler pathHandler =
			componentManager.getComponentRequired (
				pathHandlerName,
				PathHandler.class);

		if (pathHandler == null)
			throw new RuntimeException ();

		consoleModule.addPath (
			path,
			pathHandler);

	}

	// defaults

	void setDefaults () {

		path =
			simplePathSpec.path ();

		pathHandlerName =
			simplePathSpec.pathHandlerName ();

	}

}
