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
@DataClass ("context-file")
@PrototypeComponent ("contextFileSpec")
@ConsoleModuleData
public
class ContextFileSpec {

	// tree attributes

	@DataAncestor
	ConsoleSpec consoleSpec;

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
