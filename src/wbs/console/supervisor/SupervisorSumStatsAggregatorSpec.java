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
@DataClass ("sum-stats-aggregator")
@PrototypeComponent ("supervisorSumStatsAggregatorSpec")
@ConsoleModuleData
public
class SupervisorSumStatsAggregatorSpec {

	@DataParent
	SupervisorConfigSpec supervisorConfigSpec;

	@DataAttribute
	String name;

}
