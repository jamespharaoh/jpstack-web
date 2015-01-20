package wbs.platform.console.combo;

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
@DataClass ("context-tab-responder")
@PrototypeComponent ("simpleTabResponderSpec")
@ConsoleModuleData
public
class ContextTabResponderSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String title;

	@DataAttribute (
		value = "tab",
		required = true)
	String tabName;

	@DataAttribute
	String responderName;

	@DataAttribute ("page-part")
	String pagePartName;

}
