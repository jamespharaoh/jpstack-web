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
@DataClass ("context-tab-responder-page")
@PrototypeComponent ("contextTabResponderPageSpec")
@ConsoleModuleData
public
class ContextTabResponderPageSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute ("tab")
	String tabName;

	@DataAttribute
	String tabLabel;

	@DataAttribute ("file")
	String fileName;

	@DataAttribute ("responder")
	String responderName;

	@DataAttribute ("title")
	String pageTitle;

	@DataAttribute ("page-part")
	String pagePartName;

	@DataAttribute
	Boolean hideTab = false;

}
