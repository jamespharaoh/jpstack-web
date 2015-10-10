package wbs.platform.object.search;

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
@DataClass ("object-search-page")
@PrototypeComponent ("objectSearchPageSpec")
@ConsoleModuleData
public
class ObjectSearchPageSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute ("search-class")
	String searchClassName;

	@DataAttribute
	String sessionKey;

	@DataAttribute ("search-fields")
	String searchFieldsName;

	@DataAttribute ("results-fields")
	String resultsFieldsName;

	@DataAttribute
	String privKey;

	@DataAttribute
	String parentIdKey;

	@DataAttribute
	String parentIdName;

	@DataAttribute
	String tabName;

	@DataAttribute
	String fileName;

	@DataAttribute ("search-responder")
	String searchResponderName;

	@DataAttribute ("results-responder")
	String searchResultsResponderName;

}
