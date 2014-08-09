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
@DataClass ("simple-file")
@PrototypeComponent ("simpleFileSpec")
@ConsoleModuleData
public
class SimpleFileSpec {

	// attributes

	@DataAncestor
	ConsoleSpec consoleSpec;

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
