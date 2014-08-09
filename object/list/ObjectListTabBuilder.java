package wbs.platform.object.list;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.module.ConsoleModuleImpl;

@PrototypeComponent ("objectListTabBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectListTabBuilder {

	// builder

	@BuilderParent
	ObjectListPageBuilder objectListPageBuilder;

	@BuilderSource
	ObjectListPageSpec objectListPageSpec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {
	}

}
