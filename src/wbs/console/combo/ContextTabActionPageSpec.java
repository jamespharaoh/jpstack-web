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
@DataClass ("context-tab-action-page")
@PrototypeComponent ("contextTabActionPageSpec")
@ConsoleModuleData
public
class ContextTabActionPageSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute
	String name;

	@DataAttribute
	String title;

	@DataAttribute (
		name = "tab")
	String tabName;

	@DataAttribute
	String tabLabel;

	@DataAttribute
	String localFile;

	@DataAttribute (
		name = "responder")
	String responderName;

	@DataAttribute (
		name = "action")
	String actionName;

	@DataAttribute (
		name = "page-part")
	String pagePartName;

	@DataAttribute
	Boolean hideTab =
		false;

	@DataAttribute
	String privKey;

}
