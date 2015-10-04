package wbs.platform.object.create;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;
import wbs.platform.console.module.ConsoleModuleSpec;

@Accessors (fluent = true)
@Data
@DataClass ("object-create-page")
@PrototypeComponent ("objectCreatePageSpec")
@ConsoleModuleData
public
class ObjectCreatePageSpec {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute
	String typeCode;

	@DataAttribute ("tab")
	String tabName;

	@DataAttribute
	String localFile;

	@DataAttribute ("responder")
	String responderName;

	@DataAttribute ("target-context-type")
	String targetContextTypeName;

	@DataAttribute ("target-responder")
	String targetResponderName;

	@DataAttribute ("fields")
	String fieldsName;

	@DataAttribute ("fields-provider")
	String fieldsProviderName;

	@DataAttribute ("create-time")
	String createTimeFieldName;

	@DataAttribute ("create-user")
	String createUserFieldName;

	@DataAttribute
	String createPrivDelegate;

	@DataAttribute
	String createPrivCode;

	@DataAttribute
	String privKey;

}
