package wbs.console.combo;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

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
		name = "tab",
		required = true)
	String tabName;

	@DataAttribute
	String responderName;

	@DataAttribute (
		name = "page-part")
	String pagePartName;

}
