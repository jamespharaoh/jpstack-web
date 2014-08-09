package wbs.platform.supervisor;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.platform.console.spec.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("simple-part")
@PrototypeComponent ("supervisorSimplePartSpec")
@ConsoleModuleData
public
class SupervisorSimplePartSpec {

	// tree attributes

	@DataParent
	SupervisorPageSpec supervisorPageSpec;

	// attributes

	@DataAttribute (
		value = "name",
		required = true)
	@Getter @Setter
	String beanName;

}
