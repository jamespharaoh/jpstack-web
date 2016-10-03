package wbs.integrations.fonix.api;

import static wbs.utils.etc.LogicUtils.not;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.utf8ToString;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.api.mvc.ApiLoggingAction;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromGeneric;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.web.PageNotFoundException;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.integrations.fonix.logic.FonixLogic;
import wbs.integrations.fonix.model.FonixInboundLogObjectHelper;
import wbs.integrations.fonix.model.FonixInboundLogType;
import wbs.integrations.fonix.model.FonixRouteInObjectHelper;
import wbs.integrations.fonix.model.FonixRouteInRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.web.TextResponder;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.utils.string.FormatWriter;

@PrototypeComponent ("fonixRouteInAction")
public
class FonixRouteInAction
	extends ApiLoggingAction {

	// singleton dependencies

	@SingletonDependency
	FonixInboundLogObjectHelper fonixInboundLogHelper;

	@SingletonDependency
	FonixLogic fonixLogic;

	@SingletonDependency
	FonixRouteInObjectHelper fonixRouteInHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	RouteObjectHelper smsRouteHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextResponder> textResponderProvider;

	// state

	FonixRouteInRequest request;

	// implementation

	@Override
	protected
	void processRequest (
			@NonNull FormatWriter debugWriter) {

		// read and log request

		byte[] requestBytes =
			requestContext.requestBodyRaw ();

		debugWriter.writeString (
			"== REQUEST BODY ==\n\n");

		debugWriter.writeString (
			utf8ToString (
				requestBytes));

		debugWriter.writeString (
			"\n\n");

		// decode request

		request =
			new DataFromGeneric ()

			.fromMap (
				FonixRouteInRequest.class,
				requestContext.parameterMapSimple ());

	}

	@Override
	protected
	void updateDatabase () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				stringFormat (
					"%s.%s ()",
					getClass ().getSimpleName (),
					"updateDatabase"),
				this);

		// lookup route

		Optional <RouteRec> smsRouteOptional =
			smsRouteHelper.find (
				Long.parseLong (
					requestContext.requestStringRequired (
						"smsRouteId")));

		if (

			optionalIsNotPresent (
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

		// lookup fonix route in

		Optional <FonixRouteInRec> fonixRouteInOptional =
			fonixRouteInHelper.find (
				smsRoute.getId ());

		if (

			optionalIsNotPresent (
				fonixRouteInOptional)

			|| fonixRouteInOptional.get ().getDeleted ()

		) {
			throw new PageNotFoundException ();
		}

		@SuppressWarnings ("unused")
		FonixRouteInRec fonixRouteIn =
			optionalGetRequired (
				fonixRouteInOptional);

		// insert message

		smsInboxLogic.inboxInsert (
			optionalOf (
				request.guid ()),
			textHelper.findOrCreate (
				request.body ()),
			request.moNumber (),
			request.destination (),
			smsRoute,
			optionalAbsent (),
			optionalOf (
				fonixLogic.stringToInstant (
					request.receiveTime ())),
			ImmutableList.of (),
			optionalAbsent (),
			optionalAbsent ());

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

		fonixInboundLogHelper.insert (
			fonixInboundLogHelper.createInstance ()

			.setRoute (
				smsRouteHelper.findRequired (
					Long.parseLong (
						requestContext.requestStringRequired (
							"smsRouteId"))))

			.setType (
				FonixInboundLogType.smsMessage)

			.setTimestamp (
				transaction.now ())

			.setDetails (
				debugLog)

		);

		transaction.commit ();

	}

}
