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
@DataClass ("simple-path")
@PrototypeComponent ("simplePathSpec")
@ConsoleModuleData
public
class SimplePathSpec {

	// attributes

	@DataAncestor
	ConsoleSpec consoleSpec;

	@DataAttribute (
		required = true)
	String path;

	@DataAttribute ("handler")
	String pathHandlerName;

}
