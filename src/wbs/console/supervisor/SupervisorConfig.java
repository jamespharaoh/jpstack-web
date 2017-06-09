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

	SupervisorConfigSpec spec;

	List <StatsPagePartFactory> pagePartFactories;

}
