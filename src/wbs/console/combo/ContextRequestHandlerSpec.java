package wbs.console.combo;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("context-request-handler")
@PrototypeComponent ("contextRequestHandlerSpec")
@ConsoleModuleData
public
class ContextRequestHandlerSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute
	String name;

	@DataAttribute
	String fileName;

	@DataAttribute (
		name = "request-handler")
	String requestHandlerName;

}
