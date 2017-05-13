package wbs.console.combo;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("simple-responder-file")
@PrototypeComponent ("simpleResponderFileSpec")
public
class SimpleResponderFileSpec
	implements ConsoleModuleData {

	// tree attributes

	@DataParent
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute (
		required = true)
	String path;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "responder")
	String responderName;

	@DataAttribute
	String responderBeanName;

}
