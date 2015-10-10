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
@DataClass ("context-responder-page")
@PrototypeComponent ("contextResponderPageSpec")
@ConsoleModuleData
public
class ContextResponderPageSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String beanName;

	@DataAttribute ("file")
	String fileName;

	@DataAttribute ("responder")
	String responderName;

	@DataAttribute ("responder-bean")
	String responderBeanName;

}
