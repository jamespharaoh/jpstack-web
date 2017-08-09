package wbs.console.supervisor;

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

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorTableStatsGroupPart")
public
class SupervisorTableStatsGroupPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	StatsConsoleLogic statsConsoleLogic;

	// properties

	@Getter @Setter
	StatsGrouper statsGrouper;

	@Getter @Setter
	StatsResolver statsResolver;

	@Getter @Setter
	StatsFormatter statsFormatter;

	@Getter @Setter
	Map <String, StatsDataSet> statsDataSetsByName;

	@Getter @Setter
	StatsPeriod statsPeriod;

	// implementation

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			statsConsoleLogic.writeGroup (
				transaction,
				formatWriter,
				statsDataSetsByName,
				statsPeriod,
				statsGrouper,
				statsResolver,
				statsFormatter);

		}

	}

}
