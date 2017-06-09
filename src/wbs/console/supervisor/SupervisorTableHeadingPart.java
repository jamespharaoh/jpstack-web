package wbs.console.supervisor;

import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.text.SimpleDateFormat;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.console.misc.ConsoleUserHelper;
import wbs.console.part.AbstractPagePart;
import wbs.console.reporting.StatsPeriod;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorTableHeadingPart")
public
class SupervisorTableHeadingPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	SupervisorTableHeadingSpec supervisorTableHeadingSpec;

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

			// main heading

			if (supervisorTableHeadingSpec.label () != null) {

				htmlTableRowOpen (
					formatWriter);

				htmlTableHeaderCellWrite (
					formatWriter,
					supervisorTableHeadingSpec.label (),
					htmlColumnSpanAttribute (
						statsPeriod.size () + 2l));

				htmlTableRowClose (
					formatWriter);

			}

			// hours

			htmlTableRowOpen (
				formatWriter);

			htmlTableHeaderCellWrite (
				formatWriter,
				supervisorTableHeadingSpec.groupLabel ());

			for (
				Instant step
					: statsPeriod.steps ()
			) {

				htmlTableHeaderCellWrite (
					formatWriter,
					String.format (
						"%02d",
						step
							.toDateTime (
								consoleUserHelper.timezone (
									transaction))
							.getHourOfDay ()));

			}

			htmlTableHeaderCellWrite (
				formatWriter,
				"Total");

			htmlTableRowClose (
				formatWriter);

		}

	}

	static
	SimpleDateFormat hourFormat =
		new SimpleDateFormat ("HH");

}
