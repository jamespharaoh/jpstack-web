package wbs.integrations.clockworksms.api;

import static wbs.framework.utils.etc.LogicUtils.not;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalGetRequired;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.api.mvc.ApiLoggingAction;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.web.PageNotFoundException;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.integrations.clockworksms.model.ClockworkSmsInboundLogObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsInboundLogType;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteInObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteInRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.web.TextResponder;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("clockworkSmsRouteInAction")
public
class ClockworkSmsRouteInAction
	extends ApiLoggingAction {

	// dependencies

	@Inject
	ClockworkSmsInboundLogObjectHelper clockworkSmsInboundLogHelper;

	@Inject
	ClockworkSmsRouteInObjectHelper clockworkSmsRouteInHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	RequestContext requestContext;

	@Inject
	SmsInboxLogic smsInboxLogic;

	@Inject
	RouteObjectHelper smsRouteHelper;

	@Inject
	TextObjectHelper textHelper;

	// prototype dependencies

	@Inject
	Provider<TextResponder> textResponderProvider;

	// state

	ClockworkSmsRouteInRequest request;

	// implementation

	@Override
	protected
	void processRequest (
			@NonNull FormatWriter debugWriter) {

		// decode request

		DataFromXml dataFromXml =
			new DataFromXmlBuilder ()

			.registerBuilderClasses (
				ClockworkSmsRouteInRequest.class)

			.build ();

		request =
			(ClockworkSmsRouteInRequest)
			dataFromXml.readInputStream (
				requestContext.inputStream (),
				"clockwork-sms-route-in.xml",
				ImmutableList.of ());

	}

	@Override
	protected
	void updateDatabase () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ClockworkSmsRouteInAction.handle ()",
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

			|| smsRouteOptional.get ().getDeleted ()

			|| not (
				smsRouteOptional.get ().getCanReceive ())

		) {
			throw new PageNotFoundException ();
		}

		RouteRec smsRoute =
			optionalGetRequired (
				smsRouteOptional);

		// lookup clockwork sms route in

		Optional <ClockworkSmsRouteInRec> clockworkSmsRouteInOptional =
			clockworkSmsRouteInHelper.find (
				smsRoute.getId ());

		if (

			isNotPresent (
				clockworkSmsRouteInOptional)

			|| clockworkSmsRouteInOptional.get ().getDeleted ()

		) {
			throw new PageNotFoundException ();
		}

		@SuppressWarnings ("unused")
		ClockworkSmsRouteInRec clockworkSmsRouteIn =
			optionalGetRequired (
				clockworkSmsRouteInOptional);

		// lookup network

		if (
			isNotNull (
				request.network ())
		) {

			throw new RuntimeException ("TODO");

		}

		// insert message

		smsInboxLogic.inboxInsert (
			Optional.of (
				request.id ()),
			textHelper.findOrCreate (
				request.content ()),
			request.from (),
			request.to (),
			smsRoute,
			Optional.absent (),
			Optional.absent (),
			ImmutableList.of (),
			Optional.absent (),
			Optional.absent ());

		// commit and return

		transaction.commit ();

	}

	@Override
	protected
	Responder createResponse (
			@NonNull FormatWriter debugWriter) {

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
				"ClockworkSmsRouteInAction.storeLog ()",
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
