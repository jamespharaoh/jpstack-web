package wbs.console.combo;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleSpec;
import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("context-tab-responder")
@PrototypeComponent ("simpleTabResponderSpec")
public
class ContextTabResponderSpec
	implements ConsoleSpec {

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
