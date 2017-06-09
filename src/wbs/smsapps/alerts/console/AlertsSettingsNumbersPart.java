package wbs.smsapps.alerts.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;
import static wbs.web.utils.HtmlInputUtils.htmlSelectYesNo;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.smsapps.alerts.model.AlertsNumberRec;
import wbs.smsapps.alerts.model.AlertsSettingsRec;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("alertsSettingsNumbersPart")
public
class AlertsSettingsNumbersPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	AlertsSettingsConsoleHelper alertsSettingsHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	AlertsSettingsRec alertsSettings;

	// prepare

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

			alertsSettings =
				alertsSettingsHelper.findFromContextRequired (
					transaction);

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

			// top

			htmlFormOpenPost (
				formatWriter);

			if (
				requestContext.canContext (
					"super")
			) {

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" value=\"save changes\"",
					">");

				htmlParagraphClose (
					formatWriter);

			}

			// entries

			htmlTableOpenList (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
				"Name",
				"Number",
				"Enabled",
				"");

			// rows

			for (
				AlertsNumberRec alertsNumber
					: alertsSettings.getAlertsNumbers ()
			) {

				htmlTableRowOpen (
					formatWriter);

				// name

				htmlTableCellOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"<input",
					" type=\"text\"",
					" name=\"%h\"",
					stringFormat (
						"name_%s",
						integerToDecimalString (
							alertsNumber.getId ())),
					" value=\"%h\"",
					requestContext.formOrDefault (
						stringFormat (
							"name_%s",
							integerToDecimalString (
								alertsNumber.getId ())),
						alertsNumber.getName ()),
					">");

				htmlTableCellClose (
					formatWriter);

				// number

				htmlTableCellOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"<input",
					" type=\"text\"",
					" name=\"%h\"",
					stringFormat (
						"number_%s",
						integerToDecimalString (
							alertsNumber.getId ())),
					" value=\"%h\"",
					requestContext.formOrDefault (
						stringFormat (
							"number_%s",
							integerToDecimalString (
								alertsNumber.getId ())),
						alertsNumber.getNumber ().getNumber ()),
					">");

				htmlTableCellClose (
					formatWriter);

				htmlTableCellOpen (
					formatWriter);

				// enabled

				htmlSelectYesNo (
					formatWriter,
					stringFormat (
						"enabled_%s",
						integerToDecimalString (
							alertsNumber.getId ())),
					requestContext.formOrEmptyString (
						stringFormat (
							"enabled_%s",
							integerToDecimalString (
								alertsNumber.getId ()))),
					alertsNumber.getEnabled ());

				htmlTableCellClose (
					formatWriter);

				// submit

				htmlTableCellOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"%h\"",
					stringFormat (
						"delete_%s",
						integerToDecimalString (
							alertsNumber.getId ())),
					" value=\"delete\"",
					">");

				htmlTableCellClose (
					formatWriter);

				// close row

				htmlTableRowClose (
					formatWriter);

			}

			// add new

			htmlTableRowOpen (
				formatWriter);

			htmlTableCellOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"name_new\"",
				" value=\"%h\"",
				requestContext.formOrEmptyString (
					"name_new"),
				">");

			htmlTableCellClose (
				formatWriter);

			htmlTableCellOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"number_new\"",
				" value=\"%h\"",
				requestContext.formOrEmptyString (
					"number_new"),
				">");

			htmlTableCellClose (
				formatWriter);

			htmlTableCellOpen (
				formatWriter);

			htmlSelectYesNo (
				formatWriter,
				"enabled_new",
				requestContext.formOrEmptyString (
					"enabled_new"),
				true);

			htmlTableCellClose (
				formatWriter);

			// submit

			htmlTableCellOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"add_new\"",
				" value=\"add new\"",
				">");

			htmlTableCellClose (
				formatWriter);

			// close row

			htmlTableRowClose (
				formatWriter);

			// bottom

			htmlTableClose (
				formatWriter);

			if (
				requestContext.canContext (
					"alertsSettings.manage")
			) {

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" value=\"save changes\"",
					">");

				htmlParagraphClose (
					formatWriter);

			}

			htmlFormClose (
				formatWriter);

		}

	}

}
