package wbs.platform.object.search;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("results-mode")
@PrototypeComponent ("objectSearchResultsModeSpec")
@ConsoleModuleData
public
class ObjectSearchResultsModeSpec {

	@DataParent
	ObjectSearchPageSpec page;

	@DataAttribute
	String name;

	@DataAttribute (
		name = "form")
	String formContextName;

}
