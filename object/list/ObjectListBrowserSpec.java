package wbs.platform.object.list;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("list-browser")
@PrototypeComponent ("objectListBrowserSpec")
@ConsoleModuleData
public
class ObjectListBrowserSpec {

	// attributes

	@DataAttribute (
		value = "field",
		required = true)
	String fieldName;

	@DataAttribute
	String label;

}
