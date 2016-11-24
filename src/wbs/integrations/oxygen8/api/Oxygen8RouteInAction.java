package wbs.integrations.oxygen8.api;

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

import wbs.integrations.oxygen8.model.Oxygen8RouteInObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8RouteInRec;

import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

@SingletonComponent ("oxygen8RouteInAction")
public
class Oxygen8RouteInAction
	extends ApiAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	Oxygen8RouteInObjectHelper oxygen8RouteInHelper;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <Oxygen8RouteInMmsNewAction> oxygen8RouteInMmsNewActionProvider;

	@PrototypeDependency
	Provider <Oxygen8RouteInMmsOldAction> oxygen8RouteInMmsOldActionProvider;

	@PrototypeDependency
	Provider <Oxygen8RouteInSmsAction> oxygen8RouteInSmsActionProvider;

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
			chooseAction ();

		return action.handle (
			taskLogger);

	}

	// private implementation

	private
	Action chooseAction () {

		try (

			Transaction transaction =
				database.beginReadOnly (
					"Oxygen8RouteInAction.goApi (...)",
					this);

		) {

			Oxygen8RouteInRec oxygen8RouteIn =
				oxygen8RouteInHelper.findRequired (
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"smsRouteId")));

			switch (oxygen8RouteIn.getType ()) {

			case mms1:

				return oxygen8RouteInMmsOldActionProvider.get ();

			case mms2:

				return oxygen8RouteInMmsNewActionProvider.get ();

			case sms:

				return oxygen8RouteInSmsActionProvider.get ();

			default:

				throw shouldNeverHappen ();

			}

		}

	}

}
