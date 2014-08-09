package wbs.platform.object.search;

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
@DataClass ("object-search-page")
@PrototypeComponent ("objectSearchPageSpec")
@ConsoleModuleData
public
class ObjectSearchPageSpec {

	// tree attributes

	@DataAncestor
	ConsoleSpec consoleSpec;

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
