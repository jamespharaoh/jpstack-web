package wbs.smsapps.alerts.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.smsapps.alerts.model.AlertsNumberRec;
import wbs.smsapps.alerts.model.AlertsSettingsObjectHelper;
import wbs.smsapps.alerts.model.AlertsSettingsRec;

@PrototypeComponent ("alertsSettingsNumbersPart")
public
class AlertsSettingsNumbersPart
	extends AbstractPagePart {

	@Inject
	AlertsSettingsObjectHelper alertsSettingsHelper;

	// state

	AlertsSettingsRec alertsSettings;

	// prepare

	@Override
	public
	void prepare () {

		alertsSettings =
			alertsSettingsHelper.find (
				requestContext.stuffInt ("alertsSettingsId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		// top

		printFormat (
			"<form method=\"post\">\n");

		if (requestContext.canContext ("super")) {

			printFormat (
				"<p>",
				"<input " +
					"type=\"submit\" " +
					"value=\"save changes\">",
				"</p>\n");

		}

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Name</th>\n",
			"<th>Number</th>\n",
			"<th>Enabled</th>\n",
			"<th></th>\n",
			"</tr>\n");

		// rows

		for (AlertsNumberRec alertsNumber
				: alertsSettings.getAlertsNumbers ()) {

			printFormat (
				"<tr>\n");

			printFormat (
				"<td>%s</td>\n",
				stringFormat (
					"<input",
					" type=\"text\"",
					" name=\"%h\"",
					stringFormat (
						"name_%s",
						alertsNumber.getId ()),
					" value=\"%h\">",
					requestContext.getForm (
						stringFormat (
							"name_%s",
							alertsNumber.getId ()),
						alertsNumber.getName ())));

			printFormat (
				"<td>%s</td>\n",
				stringFormat (
					"<input",
					" type=\"text\"",
					" name=\"%h\"",
					stringFormat (
						"number_%s",
						alertsNumber.getId ()),
					" value=\"%h\">",
					requestContext.getForm (
						stringFormat (
							"number_%s",
							alertsNumber.getId ()),
						alertsNumber.getNumber ().getNumber ())));

			printFormat (
				"<td>%s</td>\n",
				Html.selectYesNo (
					stringFormat (
						"enabled_%s",
						alertsNumber.getId ()),
					requestContext.getForm (
						stringFormat (
							"enabled_%s",
							alertsNumber.getId ())),
					alertsNumber.getEnabled ()));

			printFormat (
				"<td>%s</td>\n",
				stringFormat (
					"<input",
					" type=\"submit\"",
					" name=\"%h\"",
					stringFormat (
						"delete_%s",
						alertsNumber.getId ()),
					" value=\"delete\"",
					">"));

			printFormat (
				"</tr>\n");

		}

		// add new

		printFormat (
			"<tr>\n");

		printFormat (
			"<td>%s</td>\n",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"name_new\"",
				" value=\"%h\"",
				requestContext.getForm ("name_new"),
				">"));

		printFormat (
			"<td>%s</td>\n",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"number_new\"",
				" value=\"%h\"",
				requestContext.getForm ("number_new"),
				">"));

		printFormat (
			"<td>%s</td>\n",
			Html.selectYesNo (
				"enabled_new",
				requestContext.getForm ("enabled_new"),
				true));

		printFormat (
			"<td><input",
			" type=\"submit\"",
			" name=\"add_new\"",
			" value=\"add new\"",
			"></td>\n");

		printFormat (
			"</tr>\n");

		// bottom

		printFormat (
			"</table>\n");

		if (requestContext.canContext ("alertsSettings.manage")) {

			printFormat (
				"<p>",
				"<input",
				" type=\"submit\"",
				" value=\"save changes\"",
				"></p>\n");

		}

		printFormat (
			"</form>\n");

	}

}
