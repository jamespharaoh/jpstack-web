package wbs.console.supervisor;

import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.part.AbstractPagePart;
import wbs.console.reporting.StatsConsoleLogic;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsFormatter;
import wbs.console.reporting.StatsGrouper;
import wbs.console.reporting.StatsPeriod;
import wbs.console.reporting.StatsResolver;
import wbs.framework.application.annotations.PrototypeComponent;

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
				printWriter,
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
