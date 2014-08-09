package wbs.platform.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.platform.console.spec.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("sum-stats-aggregator")
@PrototypeComponent ("supervisorSumStatsAggregatorSpec")
@ConsoleModuleData
public
class SupervisorSumStatsAggregatorSpec {

	@DataParent
	SupervisorPageSpec supervisorPageSpec;

	@DataAttribute
	String name;

}
