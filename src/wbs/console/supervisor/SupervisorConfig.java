package wbs.console.supervisor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.part.PagePart;

@Accessors (fluent = true)
@Data
public
class SupervisorConfig {

	String name;
	String label;

	SupervisorConfigSpec spec;

	List<Provider<PagePart>> pagePartFactories =
		new ArrayList<Provider<PagePart>> ();

}
