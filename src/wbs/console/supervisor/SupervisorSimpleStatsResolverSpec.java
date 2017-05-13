package wbs.console.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("simple-stats-resolver")
@PrototypeComponent ("supervisorSimpleStatsResolverSpec")
public
class SupervisorSimpleStatsResolverSpec
	implements ConsoleModuleData {

	@DataParent
	SupervisorConfigSpec supervisorConfigSpec;

	@DataAttribute
	String name;

	@DataAttribute (
		name = "index")
	String indexName;

	@DataAttribute (
		name = "value")
	String valueName;

	@DataAttribute (
		name = "data-set")
	String dataSetName;

	@DataAttribute (
		name = "aggregator",
		required = true)
	String aggregatorName;

}
