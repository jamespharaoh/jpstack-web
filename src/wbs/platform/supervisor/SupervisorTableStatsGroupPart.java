package wbs.platform.supervisor;

import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.reporting.console.StatsConsoleLogic;
import wbs.platform.reporting.console.StatsDataSet;
import wbs.platform.reporting.console.StatsFormatter;
import wbs.platform.reporting.console.StatsGrouper;
import wbs.platform.reporting.console.StatsPeriod;
import wbs.platform.reporting.console.StatsResolver;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorTableStatsGroupPart")
public
class SupervisorTableStatsGroupPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	StatsConsoleLogic statsConsoleLogic;

	// properties

	@Getter @Setter
	StatsGrouper statsGrouper;

	@Getter @Setter
	StatsResolver statsResolver;

	@Getter @Setter
	StatsFormatter statsFormatter;

	// state

	Map<String,StatsDataSet> statsDataSetsByName;
	StatsPeriod statsPeriod;

	@Override
	@SuppressWarnings ("unchecked")
	public
	void prepare () {

		statsPeriod =
			(StatsPeriod)
			parameters.get ("statsPeriod");

		statsDataSetsByName =
			(Map<String,StatsDataSet>)
			parameters.get ("statsDataSetsByName");

	}

	@Override
	public
	void renderHtmlBodyContent () {

		try {

			statsConsoleLogic.outputGroup (
				out,
				statsDataSetsByName,
				statsPeriod,
				statsGrouper,
				statsResolver,
				statsFormatter);

		} catch (Exception exception) {

			throw new RuntimeException (
				exception);

		}

	}

}
