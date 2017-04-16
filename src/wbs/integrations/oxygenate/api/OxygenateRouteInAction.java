package wbs.integrations.oxygenate.api;

import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.oxygenate.model.OxygenateRouteInObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateRouteInRec;

import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

@SingletonComponent ("oxygenateRouteInAction")
public
class OxygenateRouteInAction
	extends ApiAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	OxygenateRouteInObjectHelper oxygenateRouteInHelper;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <OxygenateRouteInMmsNewAction>
		oxygenateRouteInMmsNewActionProvider;

	@PrototypeDependency
	Provider <OxygenateRouteInMmsOldAction>
		oxygenateRouteInMmsOldActionProvider;

	@PrototypeDependency
	Provider <OxygenateRouteInSmsAction> oxygenateRouteInSmsActionProvider;

	// public implementation

	@Override
	protected
	Responder goApi (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goApi");

		Action action =
			chooseAction (
				taskLogger);

		return action.handle (
			taskLogger);

	}

	// private implementation

	private
	Action chooseAction (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"chooseAction");

		try (

			Transaction transaction =
				database.beginReadOnly (
					taskLogger,
					"OxygenateRouteInAction.chooseAction (...)",
					this);

		) {

			OxygenateRouteInRec oxygen8RouteIn =
				oxygenateRouteInHelper.findRequired (
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"smsRouteId")));

			switch (oxygen8RouteIn.getType ()) {

			case mms1:

				return oxygenateRouteInMmsOldActionProvider.get ();

			case mms2:

				return oxygenateRouteInMmsNewActionProvider.get ();

			case sms:

				return oxygenateRouteInSmsActionProvider.get ();

			default:

				throw shouldNeverHappen ();

			}

		}

	}

}
