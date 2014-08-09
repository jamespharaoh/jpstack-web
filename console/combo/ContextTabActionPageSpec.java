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
@DataClass ("context-tab-action-page")
@PrototypeComponent ("contextTabActionPageSpec")
@ConsoleModuleData
public
class ContextTabActionPageSpec {

	// tree attributes

	@DataAncestor
	ConsoleSpec consoleSpec;

	// attributes

	@DataAttribute
	String name;

	@DataAttribute
	String title;

	@DataAttribute ("tab")
	String tabName;

	@DataAttribute
	String tabLabel;

	@DataAttribute
	String localFile;

	@DataAttribute ("responder")
	String responderName;

	@DataAttribute ("action")
	String actionName;

	@DataAttribute ("page-part")
	String pagePartName;

	@DataAttribute
	Boolean hideTab = false;

}
