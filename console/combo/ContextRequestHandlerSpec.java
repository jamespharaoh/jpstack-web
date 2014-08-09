package wbs.platform.console.combo;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.spec.ConsoleModuleData;
import wbs.platform.console.spec.ConsoleSpec;

@Accessors (fluent = true)
@Data
@DataClass ("context-request-handler")
@PrototypeComponent ("contextRequestHandlerSpec")
@ConsoleModuleData
public
class ContextRequestHandlerSpec {

	// tree attributes

	@DataAncestor
	ConsoleSpec consoleSpec;

	// attributes

	@DataAttribute
	String name;

	@DataAttribute
	String fileName;

	@DataAttribute ("request-handler")
	String requestHandlerName;

}
