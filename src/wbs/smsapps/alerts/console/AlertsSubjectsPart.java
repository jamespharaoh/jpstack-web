package wbs.smsapps.alerts.console;

import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

import wbs.smsapps.alerts.model.AlertsSettingsRec;
import wbs.smsapps.alerts.model.AlertsSubjectRec;

@PrototypeComponent ("alertsSubjectsPart")
public
class AlertsSubjectsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	AlertsSettingsConsoleHelper alertsSettingsHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// state

	List <AlertsSubjectRec> alertsSubjects;

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

			AlertsSettingsRec alertsSettings =
				alertsSettingsHelper.findFromContextRequired (
					transaction);

			alertsSubjects =
				new ArrayList<> (
					alertsSettings.getAlertsSubjects ());

			Collections.sort (
				alertsSubjects);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

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
						transaction,
						new GlobalId (
							alertsSubject.getObjectType ().getId (),
							alertsSubject.getObjectId ()));

				htmlTableRowOpen ();

				htmlTableCellWrite (
					alertsSubject.getObjectType ().getCode ());

				objectManager.writeTdForObjectMiniLink (
					transaction,
					object);

				htmlTableCellWrite (
					booleanToYesNo (
						alertsSubject.getInclude ()));

				htmlTableRowClose ();

			}

			htmlTableClose ();

		}

	}

}
