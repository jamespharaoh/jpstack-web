package wbs.platform.object.list;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;
import wbs.platform.console.module.ConsoleModuleSpec;

@Accessors (fluent = true)
@Data
@DataClass ("object-list-page")
@PrototypeComponent ("objectListPageSpec")
@ConsoleModuleData
public
class ObjectListPageSpec {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute
	String typeCode;

	@DataAttribute ("fields")
	String fieldsName;

	@DataAttribute ("target-context-type")
	String targetContextTypeName;

	// children

	@DataChildren (
		direct = true)
	List<ObjectListTabSpec> listTabs;

	@DataChildrenIndex
	Map<String,ObjectListTabSpec> listTabsByName;

}
