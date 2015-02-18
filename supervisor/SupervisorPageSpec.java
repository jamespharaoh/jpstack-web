package wbs.platform.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("supervisor-page")
@PrototypeComponent ("supervisorPageSpec")
@ConsoleModuleData
public
class SupervisorPageSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String tabName;

	@DataAttribute
	String tabLabel;

	@DataAttribute
	String fileName;

	@DataAttribute
	String responderName;

	@DataAttribute
	String title;

	@DataAttribute ("config")
	String configName;

}
