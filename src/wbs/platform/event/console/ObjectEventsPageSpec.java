package wbs.platform.event.console;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;
import wbs.platform.console.module.ConsoleModuleSpec;

@Accessors (fluent = true)
@Data
@DataClass ("object-events-page")
@PrototypeComponent ("objectEventsPageSpec")
@ConsoleModuleData
public
class ObjectEventsPageSpec {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute
	String privKey;

	@DataAttribute
	String tabName;

	@DataAttribute
	String fileName;

	@DataAttribute
	String responderName;

}
