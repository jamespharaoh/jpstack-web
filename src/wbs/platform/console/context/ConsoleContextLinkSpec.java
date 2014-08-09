package wbs.platform.console.context;

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
@DataClass ("context-link")
@PrototypeComponent ("consoleContextLinkSpec")
@ConsoleModuleData
public
class ConsoleContextLinkSpec {

	// tree attributes

	@DataAncestor
	ConsoleSpec consoleSpec;

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
