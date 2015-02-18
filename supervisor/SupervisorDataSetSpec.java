package wbs.platform.supervisor;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("data-set")
@PrototypeComponent ("supervisorDataSetSpec")
@ConsoleModuleData
public
class SupervisorDataSetSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		value = "provider",
		required = true)
	String providerBeanName;

	@DataChildren
	List<SupervisorDataSetConditionSpec> conditions =
		new ArrayList<SupervisorDataSetConditionSpec> ();

}
