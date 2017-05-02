package wbs.console.reporting;

import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("unaryStatsGrouper")
public
class UnaryStatsGrouper
	implements StatsGrouper {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	String label;

	// implementation

	@Override
	public
	Set<Object> getGroups (
			StatsDataSet dataSet) {

		return Collections.singleton (
			StatsDatum.UNARY);

	}

	@Override
	public
	void writeTdForGroup (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Object group) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"writeTdForGroup");

		) {

			htmlTableCellWrite (
				formatWriter,
				label);

		}

	}

	@Override
	public
	List <Object> sortGroups (
			@NonNull Transaction parentTransaction,
			@NonNull Set <Object> groups) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sortGroups");

		) {

			if (groups.size () != 1) {
				throw new IllegalArgumentException ();
			}

			if (
				doesNotContain (
					groups,
					StatsDatum.UNARY)
			) {
				throw new IllegalArgumentException ();
			}

			return Collections.singletonList (
				StatsDatum.UNARY);

		}

	}

}
