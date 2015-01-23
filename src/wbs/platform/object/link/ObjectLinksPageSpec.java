package wbs.platform.object.link;

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
@DataClass ("object-links-page")
@PrototypeComponent ("objectLinksPageSpec")
@ConsoleModuleData
public
class ObjectLinksPageSpec {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (value = "links-field", required = true)
	String linksFieldName;

	@DataAttribute (value = "target-links-field", required = true)
	String targetLinksFieldName;

	@DataAttribute (required = true)
	String addEventName;

	@DataAttribute (required = true)
	String removeEventName;

	@DataAttribute (required = true)
	ObjectLinksAction.EventOrder eventOrder;

	@DataAttribute ("signal")
	String updateSignalName;

	@DataAttribute ("target-signal")
	String targetUpdateSignalName;

	@DataAttribute (required = true)
	String successNotice;

	@DataAttribute ("fields")
	String fieldsName;

}
