package wbs.platform.object.settings;

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
@DataClass ("object-settings-page")
@PrototypeComponent ("objectSettingsPageSpec")
@ConsoleModuleData
public
class ObjectSettingsPageSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute
	String objectName;

	@DataAttribute ("fields")
	String fieldsName;

	@DataAttribute ("fields-provider")
	String fieldsProviderName;

	@DataAttribute
	String privKey;

	@DataAttribute
	String name;

	@DataAttribute
	String shortName;

	@DataAttribute
	String longName;

	@DataAttribute
	String friendlyLongName;

	@DataAttribute
	String friendlyShortName;

	@DataAttribute
	String responderName;

	@DataAttribute
	String fileName;

	@DataAttribute
	String tabName;

	@DataAttribute
	String tabLocation;

	@DataAttribute
	String listContextTypeName;

}
