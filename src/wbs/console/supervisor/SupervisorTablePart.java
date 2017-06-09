package wbs.console.supervisor;

import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.part.AbstractPagePart;
import wbs.console.part.PagePart;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsPeriod;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorTablePart")
public
class SupervisorTablePart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	SupervisorTablePartBuilder supervisorTablePartBuilder;

	@Getter @Setter
	StatsPeriod statsPeriod;

	@Getter @Setter
	Map <String, StatsDataSet> statsDataSets;

	// state

	List <PagePart> pageParts =
		Collections.emptyList ();

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			// prepare page parts

			ImmutableList.Builder <PagePart> pagePartsBuilder =
				ImmutableList.<PagePart> builder ();

			for (
				StatsPagePartFactory pagePartFactory
					: supervisorTablePartBuilder.pagePartFactories ()
			) {

				PagePart pagePart =
					pagePartFactory.buildPagePart (
						transaction,
						statsPeriod,
						statsDataSets);

				pagePart.prepare (
					transaction);

				pagePartsBuilder.add (
					pagePart);

			}

			pageParts =
				pagePartsBuilder.build ();

		}

	}

	@Override
	public
	void renderHtmlHeadContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlHeadContent");

		) {

			for (
				PagePart pagePart
					: pageParts
			) {

				pagePart.renderHtmlHeadContent (
					transaction,
					formatWriter);

			}

		}

	}

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

			htmlTableOpenList (
				formatWriter);

			for (
				PagePart pagePart
					: pageParts
			) {

				pagePart.renderHtmlBodyContent (
					transaction,
					formatWriter);

			}

			htmlTableClose (
				formatWriter);

		}

	}

}
