package wbs.integrations.oxygenate.api;

import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.oxygenate.model.OxygenateRouteInObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateRouteInRec;

import wbs.web.context.RequestContext;
import wbs.web.mvc.WebAction;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("oxygenateRouteInApiAction")
public
class OxygenateRouteInApiAction
	implements ApiAction {

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
	@NamedDependency ("oxygenateRouteInMmsNewAction")
	Provider <WebAction> routeInMmsNewActionProvider;

	@PrototypeDependency
	@NamedDependency ("oxygenateRouteInMmsOldAction")
	Provider <WebAction> routeInMmsOldActionProvider;

	@PrototypeDependency
	@NamedDependency ("oxygenateRouteInSmsAction")
	Provider <WebAction> routeInSmsActionProvider;

	// public implementation

	@Override
	public
	Optional <WebResponder> handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handle");

		) {

			WebAction action =
				chooseAction (
					taskLogger);

			return optionalOf (
				action.handle (
					taskLogger));

		}

	}

	// private implementation

	private
	WebAction chooseAction (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"chooseAction");

		) {

			OxygenateRouteInRec oxygen8RouteIn =
				oxygenateRouteInHelper.findRequired (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"smsRouteId")));

			switch (oxygen8RouteIn.getType ()) {

			case mms1:

				return routeInMmsOldActionProvider.get ();

			case mms2:

				return routeInMmsNewActionProvider.get ();

			case sms:

				return routeInSmsActionProvider.get ();

			default:

				throw shouldNeverHappen ();

			}

		}

	}

}
