package wbs.integrations.smsarena.api;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.smsarena.model.SmsArenaDlrReportLogObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaDlrReportLogType;
import wbs.integrations.smsarena.model.SmsArenaReportCodeObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaReportCodeRec;
import wbs.integrations.smsarena.model.SmsArenaRouteInObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaRouteInRec;

import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.web.TextResponder;

import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

@PrototypeComponent ("smsArenaDlrDispatchAction")
public
class SmsArenaDlrDispatchAction
	implements Action {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionUtils exceptionLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SmsDeliveryReportLogic reportLogic;

	@SingletonDependency
	SmsArenaDlrReportLogObjectHelper smsArenaDlrReportLogHelper;

	@SingletonDependency
	SmsArenaRouteInObjectHelper smsArenaRouteInHelper;

	@SingletonDependency
	SmsArenaReportCodeObjectHelper smsArenaReportCodeHelper;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextResponder> textResponderProvider;

	// state

	StringBuilder debugLog =
		new StringBuilder ();

	int routeId;

	String id;
	String dlr;
	String desc;

	// implementation

	@Override
	public
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handle");

		try {

			return handleDispatch (
				taskLogger);

		} catch (RuntimeException exception) {

			debugLog.append (
				stringFormat (
					"*** THREW EXCEPTION ***\n",
					"\n"));

			debugLog.append (
				stringFormat (
					"%s\n",
					exceptionLogic.throwableDump (
						taskLogger,
						exception)));

			throw exception;

		} finally {

			writeLog (
				taskLogger);

		}

	}

	private
	Responder handleDispatch (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handleDispatch");

		// output

		debugLog.append (
			stringFormat (
				"%s %s\n",
				requestContext.method (),
				requestContext.requestUri ()));

		// output headers

		for (
			Map.Entry<String,List<String>> headerEntry
				: requestContext.headerMap ().entrySet ()
		) {

			for (
				String headerValue
					: headerEntry.getValue ()
			) {

				debugLog.append (
					stringFormat (
						"%s = %s\n",
						headerEntry.getKey (),
						headerValue));

			}

		}

		debugLog.append (
			stringFormat (
				"\n"));

		// output params

		for (
			Map.Entry<String,List<String>> parameterEntry
				: requestContext.parameterMap ().entrySet ()
		) {

			for (
				String parameterValue
					: parameterEntry.getValue ()
			) {

				debugLog.append (
					stringFormat (
						"%s = %s\n",
						parameterEntry.getKey (),
						parameterValue));

			}

		}

		debugLog.append (
			stringFormat (
				"\n"));

		// get request parameters

		id =
			requestContext.parameterOrNull ("id");

		dlr =
			requestContext.parameterOrNull ("dlr");

		desc =
			requestContext.parameterOrNull ("desc");

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"SmsArenaDlrDispatchAction.handleDispatch ()",
					this);

		) {

			// find the route

			RouteRec route =
				routeHelper.findRequired (
					requestContext.requestIntegerRequired (
						"routeId"));

			SmsArenaRouteInRec smsArenaRouteIn =
				smsArenaRouteInHelper.findRequired (
					route.getId ());

			// get the code and create the report

			SmsArenaReportCodeRec reportCode =
				smsArenaReportCodeHelper.findByCodeRequired (
					smsArenaRouteIn.getSmsArenaConfig (),
					dlr);

			reportLogic.deliveryReport (
				taskLogger,
				route,
				id,
				reportCode.getMessageStatus (),
				Optional.of (
					dlr),
				Optional.absent (),
				Optional.absent (),
				Optional.absent ());

			transaction.commit ();

			return textResponderProvider.get ()
					.text ("success");

		}

	}

	private
	void writeLog (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"writeLog");

		// create the log for the delivery report

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"SmsArenaDlrDispatchAction.writeLog ()",
					this);

		) {

			RouteRec route =
				routeHelper.findRequired (
					requestContext.requestIntegerRequired (
						"routeId"));

			smsArenaDlrReportLogHelper.insert (
				taskLogger,
				smsArenaDlrReportLogHelper.createInstance ()

				.setRoute (
					route)

				.setType (
					SmsArenaDlrReportLogType.smsDelivery)

				.setTimestamp (
					transaction.now ())

				.setDetails (
					debugLog.toString ())

			);

			transaction.commit ();

		}

	}

}
