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
@DataClass ("simple-file")
@PrototypeComponent ("simpleFileSpec")
@ConsoleModuleData
public
class SimpleFileSpec {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute (
		required = true)
	String path;

	@DataAttribute ("get-responder")
	String getResponderName;

	@DataAttribute ("get-action")
	String getActionName;

	@DataAttribute ("post-responder")
	String postResponderName;

	@DataAttribute ("post-action")
	String postActionName;

}
