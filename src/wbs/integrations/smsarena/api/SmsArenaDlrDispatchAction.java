package wbs.integrations.smsarena.api;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import org.joda.time.DateTime;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.integrations.smsarena.model.SmsArenaInboundLogObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaInboundLogRec;
import wbs.integrations.smsarena.model.SmsArenaInboundLogType;
import wbs.integrations.smsarena.model.SmsArenaReportCodeObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaReportCodeRec;
import wbs.integrations.smsarena.model.SmsArenaRouteInObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaRouteInRec;
import wbs.platform.exception.logic.ExceptionLogLogic;
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
	ExceptionLogLogic exceptionLogic;

	@Inject
	ReportLogic reportLogic;

	@Inject
	SmsArenaInboundLogObjectHelper smsArenaInboundLogHelper;

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
			
			routeId =
				requestContext.requestInt ("routeId");
	
			id =
				requestContext.parameter ("id");
	
			dlr =				
				requestContext.parameter ("dlr");
			
			desc =
				requestContext.parameter ("desc");
			
			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					this);
	
			RouteRec route =
				routeHelper.find (
					routeId);
	
			SmsArenaRouteInRec smsArenaRouteIn =
				smsArenaRouteInHelper.find (
					route.getId ());
	
			if (smsArenaRouteIn == null)
				throw new RuntimeException ();
			
			SmsArenaReportCodeRec reportCode =
					smsArenaReportCodeHelper.findByCode (
						smsArenaRouteIn.getSmsArenaConfig (),
						dlr);
	
			reportLogic.deliveryReport (
					route,
					id,
					reportCode.getMessageStatus (),
					DateTime.now().toDate(),
					null);
	
			transaction.commit ();
				
			return textResponderProvider.get ()
					.text ("success");
			
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

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					this);

			smsArenaInboundLogHelper.insert (
				new SmsArenaInboundLogRec ()

				.setRoute (
					routeHelper.find (
						requestContext.requestInt (
							"routeId")))

				.setType (
					SmsArenaInboundLogType.smsDelivery)

				.setTimestamp (
					transaction.now ())

				.setDetails (
					debugLog.toString ())

			);

			transaction.commit ();
	
		}

	}
	
}
