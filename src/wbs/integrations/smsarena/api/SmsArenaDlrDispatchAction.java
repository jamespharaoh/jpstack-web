package wbs.integrations.smsarena.api;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.web.Action;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.integrations.smsarena.model.SmsArenaDlrReportLogObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaDlrReportLogType;
import wbs.integrations.smsarena.model.SmsArenaReportCodeObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaReportCodeRec;
import wbs.integrations.smsarena.model.SmsArenaRouteInObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaRouteInRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.web.TextResponder;
import wbs.sms.message.report.logic.ReportLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("smsArenaDlrDispatchAction")
public
class SmsArenaDlrDispatchAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ExceptionUtils exceptionLogic;

	@Inject
	ReportLogic reportLogic;

	@Inject
	SmsArenaDlrReportLogObjectHelper smsArenaDlrReportLogHelper;

	@Inject
	SmsArenaRouteInObjectHelper smsArenaRouteInHelper;

	@Inject
	SmsArenaReportCodeObjectHelper smsArenaReportCodeHelper;

	@Inject
	RequestContext requestContext;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	TextObjectHelper textHelper;

	// prototype dependencies

	@Inject
	Provider<TextResponder> textResponderProvider;

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
	Responder handle () {

		try {

			return handleDispatch ();

		} catch (RuntimeException exception) {

			debugLog.append (
				stringFormat (
					"*** THREW EXCEPTION ***\n",
					"\n"));

			debugLog.append (
				stringFormat (
					"%s\n",
					exceptionLogic.throwableDump (
						exception)));

			throw exception;

		} finally {

			writeLog ();

		}

	}

	private
	Responder handleDispatch () {

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
			requestContext.parameter ("id");

		dlr =
			requestContext.parameter ("dlr");

		desc =
			requestContext.parameter ("desc");

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"SmsArenaDlrDispatchAction.handleDispatch ()",
				this);

		// find the route

		RouteRec route =
			routeHelper.findRequired (
				Integer.parseInt (
					requestContext.requestStringRequired (
						"routeId")));

		SmsArenaRouteInRec smsArenaRouteIn =
			smsArenaRouteInHelper.findRequired (
				route.getId ());

		// get the code and create the report

		SmsArenaReportCodeRec reportCode =
			smsArenaReportCodeHelper.findByCodeRequired (
				smsArenaRouteIn.getSmsArenaConfig (),
				dlr);

		reportLogic.deliveryReport (
			route,
			id,
			reportCode.getMessageStatus (),
			transaction.now (),
			null);

		transaction.commit ();

		return textResponderProvider.get ()
				.text ("success");

	}

	private
	void writeLog () {

		// create the log for the delivery report

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"SmsArenaDlrDispatchAction.writeLog ()",
				this);

		RouteRec route =
			routeHelper.findRequired (
				Integer.parseInt (
					requestContext.requestStringRequired (
						"routeId")));

		smsArenaDlrReportLogHelper.insert (
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
