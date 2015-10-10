package wbs.platform.object.list;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataClass;

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

	@DataAttribute ("fields-provider")
	String fieldsProviderName;

	@DataAttribute ("target-context-type")
	String targetContextTypeName;

	// children

	@DataChildren (
		direct = true,
		childElement = "list-browser")
	List<ObjectListBrowserSpec> listBrowsers;

	@DataChildrenIndex
	Map<String,ObjectListBrowserSpec> listBrowsersByFieldName;

	@DataChildren (
		direct = true,
		childElement = "list-tab")
	List<ObjectListTabSpec> listTabs;

	@DataChildrenIndex
	Map<String,ObjectListTabSpec> listTabsByName;

}
