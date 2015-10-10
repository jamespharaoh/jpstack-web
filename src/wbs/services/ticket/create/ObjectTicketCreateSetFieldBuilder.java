package wbs.services.ticket.create;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.module.ConsoleModuleImpl;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("objectTicketCreateSetFieldBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectTicketCreateSetFieldBuilder {

	// builder

	@BuilderParent
	ObjectTicketCreatePageBuilder objectTicketCreatePageBuilder;

	@BuilderSource
	ObjectTicketCreatePageSpec objectTicketCreatePageSpec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {
	}


}
