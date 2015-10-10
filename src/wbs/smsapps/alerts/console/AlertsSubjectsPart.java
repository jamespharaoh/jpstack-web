package wbs.smsapps.alerts.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.smsapps.alerts.model.AlertsSettingsRec;
import wbs.smsapps.alerts.model.AlertsSubjectRec;

@PrototypeComponent ("alertsSubjectsPart")
public
class AlertsSubjectsPart
	extends AbstractPagePart {

	@Inject
	AlertsSettingsConsoleHelper alertsSettingsHelper;

	@Inject
	ConsoleObjectManager objectManager;

	List<AlertsSubjectRec> alertsSubjects;

	@Override
	public
	void prepare () {

		AlertsSettingsRec alertsSettings =
			alertsSettingsHelper.find (
				requestContext.stuffInt ("alertsSettingsId"));

		alertsSubjects =
			new ArrayList<AlertsSubjectRec> (
				alertsSettings.getAlertsSubjects ());

		Collections.sort (
			alertsSubjects);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Type</th>\n",
			"<th>Object</th>\n",
			"<th>Include</th>\n",
			"<tr>\n");

		for (AlertsSubjectRec alertsSubject
				: alertsSubjects) {

			Record<?> object =
				objectManager.findObject (
					new GlobalId (
						alertsSubject.getObjectType ().getId (),
						alertsSubject.getObjectId ()));

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				alertsSubject.getObjectType ().getCode (),

				"%s\n",
				objectManager.tdForObjectMiniLink (
					object),

				"<td>%h</td>\n",
				alertsSubject.getInclude () ? "yes" : "no",

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
