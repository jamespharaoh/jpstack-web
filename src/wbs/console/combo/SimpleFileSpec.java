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
@DataClass ("simple-file")
@PrototypeComponent ("simpleFileSpec")
@ConsoleModuleData
public
class SimpleFileSpec {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute (
		required = true)
	String path;

	@DataAttribute (
		name = "get-responder")
	String getResponderName;

	@DataAttribute (
		name = "get-action")
	String getActionName;

	@DataAttribute (
		name = "post-responder")
	String postResponderName;

	@DataAttribute (
		name = "post-action")
	String postActionName;

}
