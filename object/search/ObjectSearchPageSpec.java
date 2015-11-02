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

	@DataAttribute (
		name = "search-class")
	String searchClassName;

	@DataAttribute
	String sessionKey;

	@DataAttribute (
		name = "search-fields")
	String searchFieldsName;

	@DataAttribute (
		name = "results-fields")
	String resultsFieldsName;

	@DataAttribute (
		name = "results-rows-fields")
	String resultsRowsFieldsName;

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

	@DataAttribute (
		name = "search-responder")
	String searchResponderName;

	@DataAttribute (
		name = "results-responder")
	String searchResultsResponderName;

}
