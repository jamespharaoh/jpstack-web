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
@DataClass ("context-file")
@PrototypeComponent ("contextFileSpec")
public
class ContextFileSpec
	implements ConsoleModuleData {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "file")
	String fileName;

	@DataAttribute (
		name = "get-responder")
	String getResponderName;

	@DataAttribute (
		name = "get-action")
	String getActionName;

	@DataAttribute (
		name = "post-action")
	String postActionName;

}
