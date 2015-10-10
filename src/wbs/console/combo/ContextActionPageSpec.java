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
@DataClass ("context-action-page")
@PrototypeComponent ("contextActionPageSpec")
@ConsoleModuleData
public
class ContextActionPageSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleModule;

	// attributes

	@DataAttribute
	String name;

	@DataAttribute
	String fileName;

	@DataAttribute ("action")
	String actionName;

	@DataAttribute ("responder")
	String responderName;

	@DataAttribute ("responder-bean")
	String responderBeanName;

}
