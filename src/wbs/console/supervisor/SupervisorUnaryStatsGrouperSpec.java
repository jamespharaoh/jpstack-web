package wbs.console.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("unary-stats-grouper")
@PrototypeComponent ("supervisorUnaryStatsGrouperSpec")
public
class SupervisorUnaryStatsGrouperSpec
	implements ConsoleSpec {

	@DataParent
	SupervisorConfigSpec supervisorConfigSpec;

	@DataAttribute (required = true)
	String name;

	@DataAttribute (required = true)
	String label;

}
