package wbs.platform.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.platform.console.module.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("heading")
@PrototypeComponent ("supervisorTableHeadingSpec")
@ConsoleModuleData
public
class SupervisorTableHeadingSpec {

	@DataParent
	SupervisorTablePartSpec supervisorTablePartSpec;

	@DataAttribute
	String label;

	@DataAttribute (required = true)
	String groupLabel;

}
