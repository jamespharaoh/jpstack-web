package wbs.console.supervisor;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("simple-part")
@PrototypeComponent ("supervisorSimplePartSpec")
@ConsoleModuleData
public
class SupervisorSimplePartSpec {

	// tree attributes

	@DataParent
	SupervisorConfigSpec supervisorConfig;

	// attributes

	@DataAttribute (
		name = "name",
		required = true)
	@Getter @Setter
	String beanName;

}
