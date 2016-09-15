package wbs.smsapps.alerts.console;

import static wbs.utils.etc.Misc.booleanToYesNo;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.smsapps.alerts.model.AlertsSettingsRec;
import wbs.smsapps.alerts.model.AlertsSubjectRec;

@PrototypeComponent ("alertsSubjectsPart")
public
class AlertsSubjectsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	AlertsSettingsConsoleHelper alertsSettingsHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// state

	List <AlertsSubjectRec> alertsSubjects;

	// implementation

	@Override
	public
	void prepare () {

		AlertsSettingsRec alertsSettings =
			alertsSettingsHelper.findRequired (
				requestContext.stuffInteger (
					"alertsSettingsId"));

		alertsSubjects =
			new ArrayList<> (
				alertsSettings.getAlertsSubjects ());

		Collections.sort (
			alertsSubjects);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlTableOpenDetails ();

		htmlTableHeaderRowWrite (
			"Type",
			"Object",
			"Include");

		for (
			AlertsSubjectRec alertsSubject
				: alertsSubjects
		) {

			Record <?> object =
				objectManager.findObject (
					new GlobalId (
						alertsSubject.getObjectType ().getId (),
						alertsSubject.getObjectId ()));

			htmlTableRowOpen ();

			htmlTableCellWrite (
				alertsSubject.getObjectType ().getCode ());

			objectManager.writeTdForObjectMiniLink (
				object);

			htmlTableCellWrite (
				booleanToYesNo (
					alertsSubject.getInclude ()));

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}
