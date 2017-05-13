package wbs.platform.object.browse;

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
@DataClass ("object-browse-page")
@PrototypeComponent ("objectBrowsePageSpec")
public
class ObjectBrowsePageSpec
	implements ConsoleModuleData {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute
	String typeCode;

	@DataAttribute (
		name = "form")
	String formContextName;

	@DataAttribute (
		name = "target-context-type")
	String targetContextTypeName;

}
