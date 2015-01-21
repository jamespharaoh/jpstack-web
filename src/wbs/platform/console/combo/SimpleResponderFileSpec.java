package wbs.platform.console.combo;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.platform.console.module.ConsoleModuleData;
import wbs.platform.console.module.ConsoleModuleSpec;

@Accessors (fluent = true)
@Data
@DataClass ("simple-responder-file")
@PrototypeComponent ("simpleResponderFileSpec")
@ConsoleModuleData
public
class SimpleResponderFileSpec {

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

	@DataAttribute ("responder")
	String responderName;

	@DataAttribute
	String responderBeanName;

}
