package wbs.platform.supervisor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.module.ConsoleModuleImpl;
import wbs.platform.console.module.SimpleConsoleBuilderContainer;
import wbs.platform.console.part.PagePart;
import wbs.platform.reporting.console.StatsAggregator;
import wbs.platform.reporting.console.StatsFormatter;
import wbs.platform.reporting.console.StatsGrouper;
import wbs.platform.reporting.console.StatsProvider;
import wbs.platform.reporting.console.StatsResolver;

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
