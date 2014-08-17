package wbs.platform.object.browse;

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
@DataClass ("object-browse-page")
@PrototypeComponent ("objectBrowsePageSpec")
@ConsoleModuleData
public
class ObjectBrowsePageSpec {

	// attributes

	@DataAncestor
	ConsoleSpec consoleSpec;

	@DataAttribute
	String typeCode;

	@DataAttribute ("fields")
	String fieldsName;

	@DataAttribute ("target-context-type")
	String targetContextTypeName;

}
