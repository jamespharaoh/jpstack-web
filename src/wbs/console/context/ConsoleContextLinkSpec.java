package wbs.console.context;

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
		name = "name",
		required = true)
	String localName;

	@DataAttribute (
		name = "link",
		required = true)
	String linkName;

	@DataAttribute (
		required = true)
	String label;

	@DataAttribute
	String privKey;

}
