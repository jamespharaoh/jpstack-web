package wbs.console.supervisor;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class SupervisorConfig {

	String name;
	String label;

	Long offsetHours;

	List <SupervisorConditionSpec> conditionSpecs;
	List <SupervisorDataSetSpec> dataSetSpecs;

	List <StatsPagePartFactory> pagePartFactories;

}
