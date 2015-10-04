package wbs.platform.object.summary;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;
import wbs.platform.console.module.ConsoleModuleSpec;

@Accessors (fluent = true)
@Data
@DataClass ("object-summary-page")
@PrototypeComponent ("objectSummaryPageSpec")
@ConsoleModuleData
public
class ObjectSummaryPageSpec {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute ("fields")
	String fieldsName;

	@DataAttribute ("fields-provider")
	String fieldsProviderName;

	@DataAttribute
	String privKey;

	@DataChildren (
		direct = true)
	List<Object> builders =
		new ArrayList<Object> ();

}
