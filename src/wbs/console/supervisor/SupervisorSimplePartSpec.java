package wbs.console.supervisor;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("simple-part")
@PrototypeComponent ("supervisorSimplePartSpec")
public
class SupervisorSimplePartSpec
	implements ConsoleModuleData {

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
