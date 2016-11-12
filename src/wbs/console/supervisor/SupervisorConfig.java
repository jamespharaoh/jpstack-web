package wbs.console.supervisor;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.part.PagePartFactory;

@Accessors (fluent = true)
@Data
public
class SupervisorConfig {

	String name;
	String label;

	SupervisorConfigSpec spec;

	List <PagePartFactory> pagePartFactories;

}
