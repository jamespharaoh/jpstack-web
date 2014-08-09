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
@DataClass ("simple-action-page")
@PrototypeComponent ("simpleActionPageSpec")
@ConsoleModuleData
public
class SimpleActionPageSpec {

	// tree attributes

	@DataAncestor
	ConsoleSpec consoleSpec;

	// attributes

	@DataAttribute (
		required = true)
	String path;

	@DataAttribute
	String name;

	@DataAttribute ("action")
	String actionName;

	@DataAttribute ("responder")
	String responderName;

	@DataAttribute ("responder-bean")
	String responderBeanName;

}
