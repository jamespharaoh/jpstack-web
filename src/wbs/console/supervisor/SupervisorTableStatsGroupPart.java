package wbs.console.supervisor;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.part.AbstractPagePart;
import wbs.console.reporting.StatsConsoleLogic;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsFormatter;
import wbs.console.reporting.StatsGrouper;
import wbs.console.reporting.StatsPeriod;
import wbs.console.reporting.StatsResolver;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorTableStatsGroupPart")
public
class SupervisorTableStatsGroupPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	StatsConsoleLogic statsConsoleLogic;

	// properties

	@Getter @Setter
	StatsGrouper statsGrouper;

	@Getter @Setter
	StatsResolver statsResolver;

	@Getter @Setter
	StatsFormatter statsFormatter;

	// state

	Map <String, StatsDataSet> statsDataSetsByName;

	StatsPeriod statsPeriod;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		statsPeriod =
			genericCastUnchecked (
				parameters.get (
					"statsPeriod"));

		statsDataSetsByName =
			genericCastUnchecked (
				parameters.get (
					"statsDataSetsByName"));

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		try {

			statsConsoleLogic.writeGroup (
				formatWriter,
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
