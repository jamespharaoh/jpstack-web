package wbs.platform.supervisor;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.part.PagePart;
import wbs.platform.reporting.console.StatsFormatter;
import wbs.platform.reporting.console.StatsGrouper;
import wbs.platform.reporting.console.StatsResolver;
import wbs.platform.reporting.console.UnaryStatsGrouper;

@PrototypeComponent ("supervisorTableStatsTotalBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorTableStatsTotalBuilder {

	@Inject
	Provider<SupervisorTableStatsGroupPart> supervisorTableStatsGroupPart;

	@Inject
	Provider<UnaryStatsGrouper> unaryStatsGrouper;

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
			unaryStatsGrouper.get ()
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

		Provider<PagePart> pagePartFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				return supervisorTableStatsGroupPart.get ()
					.statsGrouper (statsGrouper)
					.statsResolver (statsResolver)
					.statsFormatter (statsFormatter);

			}

		};

		supervisorTablePartBuilder.pagePartFactories ()
			.add (pagePartFactory);

	}

}
