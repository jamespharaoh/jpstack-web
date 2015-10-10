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
@DataClass ("context-file")
@PrototypeComponent ("contextFileSpec")
@ConsoleModuleData
public
class ContextFileSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute ("file")
	String fileName;

	@DataAttribute ("get-responder")
	String getResponderName;

	@DataAttribute ("get-action")
	String getActionName;

	@DataAttribute ("post-action")
	String postActionName;

}
