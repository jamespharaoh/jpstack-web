package wbs.platform.object.browse;

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
@DataClass ("object-browse-page")
@PrototypeComponent ("objectBrowsePageSpec")
@ConsoleModuleData
public
class ObjectBrowsePageSpec {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute
	String typeCode;

	@DataAttribute ("fields")
	String fieldsName;

	@DataAttribute ("target-context-type")
	String targetContextTypeName;

}
