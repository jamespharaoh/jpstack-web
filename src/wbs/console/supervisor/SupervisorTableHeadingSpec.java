package wbs.console.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

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
