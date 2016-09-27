package wbs.smsapps.alerts.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPost;
import static wbs.utils.web.HtmlInputUtils.htmlSelectYesNo;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
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
	void prepare () {

		alertsSettings =
			alertsSettingsHelper.findRequired (
				requestContext.stuffInteger (
					"alertsSettingsId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

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

			formatWriter.writeFormat (
				"<input",
				" type=\"text\"",
				" name=\"%h\"",
				stringFormat (
					"name_%s",
					alertsNumber.getId ()),
				" value=\"%h\"",
				requestContext.getForm (
					stringFormat (
						"name_%s",
						alertsNumber.getId ()),
					alertsNumber.getName ()),
				">");

			htmlTableCellClose ();

			// number

			htmlTableCellOpen ();

			formatWriter.writeFormat (
				"<input",
				" type=\"text\"",
				" name=\"%h\"",
				stringFormat (
					"number_%s",
					alertsNumber.getId ()),
				" value=\"%h\"",
				requestContext.getForm (
					stringFormat (
						"number_%s",
						alertsNumber.getId ()),
					alertsNumber.getNumber ().getNumber ()),
				"></td>\n");

			htmlTableCellOpen ();

			// enabled

			htmlSelectYesNo (
				stringFormat (
					"enabled_%s",
					alertsNumber.getId ()),
				requestContext.getForm (
					stringFormat (
						"enabled_%s",
						alertsNumber.getId ())),
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
					alertsNumber.getId ()),
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
			requestContext.getForm (
				"name_new",
				""),
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"text\"",
			" name=\"number_new\"",
			" value=\"%h\"",
			requestContext.getForm (
				"number_new",
				""),
			">");

		htmlTableCellClose ();

		htmlTableCellOpen ();

		htmlSelectYesNo (
			"enabled_new",
			requestContext.getForm (
				"enabled_new"),
			true);

		htmlTableCellClose ();

		// submit

		htmlTableCellOpen ();

		formatWriter.writeFormat (
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

			formatWriter.writeFormat (
				"<input",
				" type=\"submit\"",
				" value=\"save changes\"",
				">");

			htmlParagraphClose ();

		}

		htmlFormClose ();

	}

}
