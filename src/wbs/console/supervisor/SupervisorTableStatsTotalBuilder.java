package wbs.console.supervisor;

import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.part.PagePart;
import wbs.console.reporting.StatsFormatter;
import wbs.console.reporting.StatsGrouper;
import wbs.console.reporting.StatsResolver;
import wbs.console.reporting.UnaryStatsGrouper;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;

@PrototypeComponent ("supervisorTableStatsTotalBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorTableStatsTotalBuilder {

	// prototype dependencies

	@PrototypeDependency
	Provider <SupervisorTableStatsGroupPart>
	supervisorTableStatsGroupPartProvider;

	@PrototypeDependency
	Provider <UnaryStatsGrouper> unaryStatsGrouperProvider;

	// builder

	@BuilderParent
	SupervisorTablePartSpec container;

	@BuilderSource
	SupervisorTableStatsTotalSpec spec;

	@BuilderTarget
	SupervisorTablePartBuilder supervisorTablePartBuilder;

	// state

	StatsGrouper statsGrouper;
	StatsResolver statsResolver;
	StatsFormatter statsFormatter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		SupervisorConfigBuilder supervisorConfigBuilder =
			supervisorTablePartBuilder.supervisorConfigBuilder;

		String label =
			spec.label ();

		statsGrouper =
			unaryStatsGrouperProvider.get ()
				.label (label);

		statsResolver =
			supervisorConfigBuilder.statsResolversByName ().get (
				spec.resolverName ());

		if (statsResolver == null) {

			throw new RuntimeException (
				stringFormat (
					"Stats resolver %s does not exist",
					spec.resolverName ()));

		}

		statsFormatter =
			supervisorConfigBuilder.statsFormattersByName ().get (
				spec.formatterName ());

		if (statsFormatter == null) {

			throw new RuntimeException (
				stringFormat (
					"Stats formatter %s does not exist",
					spec.formatterName ()));

		}

		Provider <PagePart> pagePartFactory =
			new Provider <PagePart> () {

			@Override
			public
			PagePart get () {

				return supervisorTableStatsGroupPartProvider.get ()

					.statsGrouper (
						statsGrouper)

					.statsResolver (
						statsResolver)

					.statsFormatter (
						statsFormatter);

			}

		};

		supervisorTablePartBuilder.pagePartFactories ()
			.add (pagePartFactory);

	}

}
