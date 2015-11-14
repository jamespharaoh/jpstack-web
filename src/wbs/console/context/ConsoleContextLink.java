package wbs.console.context;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("context-link")
@PrototypeComponent ("consoleContextLink")
public
class ConsoleContextLink {

	// properties

	@DataAttribute
	String localName;

	@DataAttribute
	String linkName;

	@DataAttribute
	String label;

	@DataAttribute
	String extensionPointName;

	@DataAttribute
	String tabLocation;

	@DataAttribute
	String privKey;

}
