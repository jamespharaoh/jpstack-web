package wbs.platform.object.search;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("object-search-page")
@PrototypeComponent ("objectSearchPageSpec")
public
class ObjectSearchPageSpec
	implements ConsoleModuleData {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute
	String name;

	@DataAttribute (
		name = "object-type")
	String objectTypeName;

	@DataAttribute
	String sessionKey;

	@DataAttribute (
		name = "search-class")
	String searchClassName;

	@DataAttribute (
		name = "search-fields")
	String searchFormFieldsName;

	@DataAttribute (
		name = "search-dao-method")
	String searchDaoMethodName;

	@DataAttribute (
		name = "results-class")
	String resultsClassName;

	@DataAttribute (
		name = "results-column-fields")
	String resultsColumnFormFieldsName;

	@DataAttribute (
		name = "results-row-fields")
	String resultsRowFormFieldsName;

	@DataAttribute (
		name = "results-dao-method")
	String resultsDaoMethodName;

	@DataAttribute
	String privKey;

	@DataAttribute
	String parentIdKey;

	@DataAttribute
	String parentIdName;

	@DataAttribute
	String tabName;

	@DataAttribute
	String tabLabel;

	@DataAttribute
	String fileName;

	@DataAttribute (
		name = "search-responder")
	String searchResponderName;

	@DataAttribute (
		name = "results-responder")
	String searchResultsResponderName;

	@DataChildren (
		direct = true,
		childElement = "results-mode")
	List <ObjectSearchResultsModeSpec> resultsModes;

}
