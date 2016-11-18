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

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.smsapps.alerts.model.AlertsNumberRec;
import wbs.smsapps.alerts.model.AlertsSettingsObjectHelper;
import wbs.smsapps.alerts.model.AlertsSettingsRec;

@PrototypeComponent ("alertsSettingsNumbersPart")
public
class AlertsSettingsNumbersPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	AlertsSettingsObjectHelper alertsSettingsHelper;

	// state

	AlertsSettingsRec alertsSettings;

	// prepare

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		alertsSettings =
			alertsSettingsHelper.findRequired (
				requestContext.stuffInteger (
					"alertsSettingsId"));

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		// top

		htmlFormOpenPost ();

		if (
			requestContext.canContext (
				"super")
		) {

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"save changes\"",
				">");

			htmlParagraphClose ();

		}

		// entries

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Name",
			"Number",
			"Enabled",
			"");

		// rows

		for (
			AlertsNumberRec alertsNumber
				: alertsSettings.getAlertsNumbers ()
		) {

			htmlTableRowOpen ();

			// name

			htmlTableCellOpen ();

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

			htmlTableCellClose ();

			// number

			htmlTableCellOpen ();

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

			htmlTableCellClose ();

			htmlTableCellOpen ();

			// enabled

			htmlSelectYesNo (
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

			htmlTableCellClose ();

			// submit

			htmlTableCellOpen ();

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

			htmlTableCellClose ();

			// close row

			htmlTableRowClose ();

		}

		// add new

		htmlTableRowOpen ();

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"text\"",
			" name=\"name_new\"",
			" value=\"%h\"",
			requestContext.formOrEmptyString (
				"name_new"),
			">");

		htmlTableCellClose ();

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"text\"",
			" name=\"number_new\"",
			" value=\"%h\"",
			requestContext.formOrEmptyString (
				"number_new"),
			">");

		htmlTableCellClose ();

		htmlTableCellOpen ();

		htmlSelectYesNo (
			"enabled_new",
			requestContext.formOrEmptyString (
				"enabled_new"),
			true);

		htmlTableCellClose ();

		// submit

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"add_new\"",
			" value=\"add new\"",
			">");

		htmlTableCellClose ();

		// close row

		htmlTableRowClose ();

		// bottom

		htmlTableClose ();

		if (
			requestContext.canContext (
				"alertsSettings.manage")
		) {

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"save changes\"",
				">");

			htmlParagraphClose ();

		}

		htmlFormClose ();

	}

}
