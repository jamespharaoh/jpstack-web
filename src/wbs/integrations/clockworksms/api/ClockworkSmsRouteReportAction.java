package wbs.integrations.clockworksms.api;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalRequired;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import wbs.api.mvc.ApiLoggingAction;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.web.PageNotFoundException;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.integrations.clockworksms.model.ClockworkSmsInboundLogObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsInboundLogType;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteOutObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteOutRec;
import wbs.platform.text.web.TextResponder;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("clockworkSmsRouteReportAction")
public
class ClockworkSmsRouteReportAction
	extends ApiLoggingAction {

	// dependencies

	@Inject
	ClockworkSmsInboundLogObjectHelper clockworkSmsInboundLogHelper;

	@Inject
	ClockworkSmsRouteOutObjectHelper clockworkSmsRouteOutHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	RequestContext requestContext;

	@Inject
	RouteObjectHelper smsRouteHelper;

	// prototype dependencies

	@Inject
	Provider<TextResponder> textResponderProvider;

	// state

	ClockworkSmsRouteReportRequest request;

	// implementation

	@Override
	protected
	void processRequest () {

		DataFromXml dataFromXml =
			new DataFromXml ()

			.registerBuilderClasses (
				ClockworkSmsRouteReportRequest.class);

		// decode request

		request =
			(ClockworkSmsRouteReportRequest)
			dataFromXml.readInputStream (
				requestContext.inputStream (),
				"clockwork-sms-route-report.xml",
				ImmutableList.of ());

	}

	@Override
	protected
	void updateDatabase () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ClockworkSmsRouteOutAction.handle ()",
				this);

		// lookup route

		Optional<RouteRec> smsRouteOptional =
			smsRouteHelper.find (
				Long.parseLong (
					requestContext.requestStringRequired (
						"smsRouteId")));

		if (

			isNotPresent (
				smsRouteOptional)

			|| equal (
				smsRouteOptional.get ().getDeleted (),
				true)

			|| equal (
				smsRouteOptional.get ().getCanReceive (),
				false)

		) {
			throw new PageNotFoundException ();
		}

		RouteRec smsRoute =
			optionalRequired (
				smsRouteOptional);

		// lookup clockwork sms route in

		Optional<ClockworkSmsRouteOutRec> clockworkSmsRouteOutOptional =
			clockworkSmsRouteOutHelper.find (
				smsRoute.getId ());

		if (

			isNotPresent (
				clockworkSmsRouteOutOptional)

			|| equal (
				clockworkSmsRouteOutOptional.get ().getDeleted (),
				true)

		) {
			throw new PageNotFoundException ();
		}

		ClockworkSmsRouteOutRec clockworkSmsRouteOut =
			optionalRequired (
				clockworkSmsRouteOutOptional);

		// TODO

		throw new RuntimeException ("TODO");

		// commit and return

		/*
		transaction.commit ();
		*/

	}

	@Override
	protected
	Responder createResponse () {

		return textResponderProvider.get ()

			.text (
				"OK");

	}

	@Override
	protected
	void storeLog (
			@NonNull String debugLog) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ClockworkSmsRouteReportAction.storeLog ()",
				this);

		clockworkSmsInboundLogHelper.insert (
			clockworkSmsInboundLogHelper.createInstance ()

			.setRoute (
				smsRouteHelper.findRequired (
					Long.parseLong (
						requestContext.requestStringRequired (
							"smsRouteId"))))

			.setType (
				ClockworkSmsInboundLogType.smsMessage)

			.setTimestamp (
				transaction.now ())

			.setDetails (
				debugLog)

		);

		transaction.commit ();

	}

}
