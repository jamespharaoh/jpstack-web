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
@DataClass ("simple-responder")
@PrototypeComponent ("simpleResponderSpec")
@ConsoleModuleData
public
class SimpleResponderSpec {

	// attributes

	@DataAncestor
	ConsoleSpec consoleSpec;

	@DataAttribute
	String name;

	@DataAttribute
	String responderName;

	@DataAttribute
	String responderBeanName;

}
