package wbs.apn.chat.graphs.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenGetAction;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.YearMonth;

import wbs.console.html.ObsoleteDateLinks;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
public abstract
class AbstractMonthlyGraphPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// dependencies

	@Getter @Setter
	String myLocalPart;

	@Getter @Setter
	String imageLocalPart;

	// state

	YearMonth yearMonth;

	// implementation

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

			yearMonth =
				YearMonth.parse (
					requestContext.parameterRequired (
						"month"));

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

			htmlFormOpenGetAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					myLocalPart));

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"Month<br>");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"month\"",
				" value=\"%h\"",
				yearMonth.toString (),
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"ok\"",
				">");

			htmlParagraphClose (
				formatWriter);

			htmlFormClose (
				formatWriter);

			if (yearMonth != null) {

				ObsoleteDateLinks.monthlyBrowserParagraph (
					formatWriter,
					requestContext.resolveLocalUrl (
						myLocalPart),
					emptyMap (),
					yearMonth.toLocalDate (1));

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"<img",
					" style=\"graph\"",
					" src=\"%h\"",
					requestContext.resolveLocalUrl (
						stringFormat (
							"%s",
							imageLocalPart,
							"?month=%u",
							yearMonth.toString ())),
					">");

				htmlParagraphClose (
					formatWriter);

			}

		}

	}

}
