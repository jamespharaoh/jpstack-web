package wbs.platform.console.context;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;
import wbs.platform.console.module.ConsoleModuleSpec;

@Accessors (fluent = true)
@Data
@DataClass ("context-link")
@PrototypeComponent ("consoleContextLinkSpec")
@ConsoleModuleData
public
class ConsoleContextLinkSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute (
		value = "name",
		required = true)
	String localName;

	@DataAttribute (
		value = "link",
		required = true)
	String linkName;

	@DataAttribute (
		required = true)
	String label;

	@DataAttribute (
		required = true)
	String privKey;

}
