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
@DataClass ("context-tab-page")
@PrototypeComponent ("contextTabPageSpec")
public
class ContextTabPageSpec
	implements ConsoleSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute
	String name;

	@DataAttribute
	String tabName;

	@DataAttribute
	String tabLabel;

	@DataAttribute
	String fileName;

	@DataAttribute
	String responderName;

	@DataAttribute
	String title;

	@DataAttribute (
		name = "page-part")
	String pagePartName;

	@DataAttribute
	Boolean hideTab = false;

}
