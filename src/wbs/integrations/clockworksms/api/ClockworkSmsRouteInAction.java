package wbs.integrations.clockworksms.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.LogicUtils.not;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.min;
import static wbs.utils.etc.Misc.toHex;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.utf8ToStringSafe;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.api.mvc.ApiLoggingAction;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.logging.DefaultLogContext;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.clockworksms.model.ClockworkSmsInboundLogObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsInboundLogType;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteInObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteInRec;

import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.web.TextResponder;

import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.exceptions.HttpNotFoundException;
import wbs.web.responder.Responder;

@PrototypeComponent ("clockworkSmsRouteInAction")
public
class ClockworkSmsRouteInAction
	extends ApiLoggingAction {

	private final static
	LogContext logContext =
		DefaultLogContext.forClass (
			ClockworkSmsRouteInAction.class);

	// singleton dependencies

	@SingletonDependency
	ClockworkSmsInboundLogObjectHelper clockworkSmsInboundLogHelper;

	@SingletonDependency
	ClockworkSmsRouteInObjectHelper clockworkSmsRouteInHelper;

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

	ClockworkSmsRouteInRequest request;
	Boolean success = false;

	// implementation

	@Override
	protected
	void processRequest (
			@NonNull TaskLogger taskLogger,
			@NonNull FormatWriter debugWriter) {

		taskLogger =
			logContext.nestTaskLogger (
				taskLogger,
				"processRequest");

		// convert request to string

		byte[] requestBytes =
			requestContext.requestBodyRaw ();

		String requestString;

		try {

			requestString =
				utf8ToStringSafe (
					requestBytes);

		} catch (IllegalArgumentException exception) {

			debugWriter.writeLineFormat (
				"=== DECODE ERROR ===");

			debugWriter.writeNewline ();

			debugWriter.writeLineFormat (
				"Error decoding unicode data: %s",
				exception.getMessage ());

			debugWriter.writeNewline ();

			debugWriter.writeLineFormat (
				"=== RAW REQUEST DATA ===");

			debugWriter.writeNewline ();

			for (
				int position = 0;
				position < requestBytes.length;
				position += 32
			) {

				byte[] requestBytesChunk =
					Arrays.copyOfRange (
						requestBytes,
						position,
						min (
							position + 32,
							requestBytes.length));

				debugWriter.writeLineFormat (
					"%s %s",
					String.format (
						"%06x",
						position),
					toHex (
						requestBytesChunk));

			}

			debugWriter.writeLineFormat ();

			throw exception;

		}

		debugWriter.writeLineFormat (
			"=== REQUEST DATA ===");

		debugWriter.writeNewline ();

		debugWriter.writeString (
			requestString);

		debugWriter.writeNewline ();

		debugWriter.writeNewline ();

		// decode request

		DataFromXml dataFromXml =
			new DataFromXmlBuilder ()

			.taskLogger (
				taskLogger)

			.registerBuilderClasses (
				ClockworkSmsRouteInRequest.class)

			.build ();

		taskLogger.makeException ();

		request =
			(ClockworkSmsRouteInRequest)
			dataFromXml.readInputStream (
				taskLogger,
				new ByteArrayInputStream (
					requestBytes),
				"clockwork-sms-route-in.xml",
				emptyList ());

	}

	@Override
	protected
	void updateDatabase (
			@NonNull TaskLogger taskLogger) {

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

			optionalIsNotPresent (
				smsRouteOptional)

			|| smsRouteOptional.get ().getDeleted ()

			|| not (
				smsRouteOptional.get ().getCanReceive ())

		) {

			throw new HttpNotFoundException (
				optionalAbsent (),
				emptyList ());

		}

		RouteRec smsRoute =
			optionalGetRequired (
				smsRouteOptional);

		// lookup clockwork sms route in

		Optional <ClockworkSmsRouteInRec> clockworkSmsRouteInOptional =
			clockworkSmsRouteInHelper.find (
				smsRoute.getId ());

		if (

			optionalIsNotPresent (
				clockworkSmsRouteInOptional)

			|| clockworkSmsRouteInOptional.get ().getDeleted ()

		) {

			throw new HttpNotFoundException (
				optionalAbsent (),
				emptyList ());

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

		success = true;

	}

	@Override
	protected
	Responder createResponse (
			@NonNull TaskLogger taskLogger,
			@NonNull FormatWriter debugWriter) {

		return textResponderProvider.get ()

			.text (
				"OK");

	}

	@Override
	protected
	void storeLog (
			@NonNull TaskLogger taskLogger,
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

			.setSuccess (
				success)

		);

		transaction.commit ();

	}

}
