package wbs.platform.supervisor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.platform.console.part.PagePart;

@Accessors (fluent = true)
@Data
public
class SupervisorConfig {

	String name;

	SupervisorConfigSpec spec;

	List<Provider<PagePart>> pagePartFactories =
		new ArrayList<Provider<PagePart>> ();

}
