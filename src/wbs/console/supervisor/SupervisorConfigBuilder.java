package wbs.console.supervisor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.module.ConsoleModuleImpl;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.console.part.PagePart;
import wbs.console.reporting.StatsAggregator;
import wbs.console.reporting.StatsFormatter;
import wbs.console.reporting.StatsGrouper;
import wbs.console.reporting.StatsProvider;
import wbs.console.reporting.StatsResolver;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorConfigBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorConfigBuilder {

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	SupervisorConfigSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// properties

	@Getter @Setter
	Map<String,StatsProvider> statsProvidersByName =
		new LinkedHashMap<String,StatsProvider> ();

	@Getter @Setter
	Map<String,StatsAggregator> statsAggregatorsByName =
		new LinkedHashMap<String,StatsAggregator> ();

	@Getter @Setter
	Map<String,StatsFormatter> statsFormattersByName =
		new LinkedHashMap<String,StatsFormatter> ();

	@Getter @Setter
	Map<String,StatsGrouper> statsGroupersByName =
		new LinkedHashMap<String,StatsGrouper> ();

	@Getter @Setter
	Map<String,StatsResolver> statsResolversByName =
		new LinkedHashMap<String,StatsResolver> ();

	@Getter @Setter
	List<Provider<PagePart>> pagePartFactories =
		new ArrayList<Provider<PagePart>> ();

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		builder.descend (
			spec,
			spec.builders (),
			this);

		consoleModule.addSupervisorConfig (
			new SupervisorConfig ()
				.name (spec.name ())
				.label (spec.label ())
				.spec (spec)
				.pagePartFactories (pagePartFactories));

	}

}
