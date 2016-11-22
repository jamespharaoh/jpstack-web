package wbs.integrations.oxygen8.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.IterableUtils.iterableFindFirst;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.BinaryUtils.bytesFromBase64;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequiredOrDefault;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.string.StringUtils.stringNotInSafe;
import static wbs.utils.string.StringUtils.utf8ToString;

import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.api.mvc.ApiLoggingAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.oxygen8.model.Oxygen8InboundLogObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8InboundLogType;
import wbs.integrations.oxygen8.model.Oxygen8RouteInObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8RouteInRec;

import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.text.web.TextResponder;

import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.string.FormatWriter;

import wbs.web.exceptions.HttpUnprocessableEntityException;
import wbs.web.responder.Responder;

@PrototypeComponent ("oxygen8RouteInMmsNewAction")
public
class Oxygen8RouteInMmsNewAction
	extends ApiLoggingAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaLogic mediaLogic;

	/*
	@SingletonDependency
	MessageTypeObjectHelper messageTypeHelper;

	@SingletonDependency
	NetworkObjectHelper networkHelper;
	*/

	@SingletonDependency
	Oxygen8InboundLogObjectHelper oxygen8InboundLogHelper;

	/*
	@SingletonDependency
	Oxygen8NetworkObjectHelper oxygen8NetworkHelper;
	*/

	@SingletonDependency
	Oxygen8RouteInObjectHelper oxygen8RouteInHelper;

	/*
	@SingletonDependency
	RequestContext requestContext;
	*/

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	NumberObjectHelper smsNumberHelper;

	@SingletonDependency
	RouteObjectHelper smsRouteHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextResponder> textResponderProvider;

	// state

	Oxygen8RouteInMmsNewRequest request;

	/*
	Long routeId;

	String mmsMessageId;
	String mmsMessageType;
	String mmsSenderAddress;
	String mmsRecipientAddress;
	Optional<String> mmsSubject;
	Instant mmsDate;
	String mmsNetwork;

	String messageString;
	TextRec messageText;

	List <MediaRec> medias =
		new ArrayList<MediaRec> ();
	*/

	Boolean success = false;

	// abstract implementation

	@Override
	protected
	void processRequest (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter debugWriter) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"processRequest");

		request =
			genericCastUnchecked (
				requestFromXml.readInputStream (
					taskLogger,
					requestContext.inputStream (),
					"oxygen8-route-in-mms-new.xml"));

		if (
			stringNotEqualSafe (
				request.type (),
				"MMS")
		) {

			taskLogger.errorFormat (
				"Invalid value for type attribute: %s",
				request.type ());

		}

		for (
			Oxygen8RouteInMmsNewRequest.Attachment attachment
				: request.attachments ()
		) {

			if (
				stringNotInSafe (
					attachment.encoding (),
					"base64",
					"text")
			) {

				taskLogger.errorFormat (
					"Invalid value for 'Encoding' attribute: %s",
					attachment.encoding ());

			}

		}

		taskLogger.makeException (
			() -> new HttpUnprocessableEntityException (
				stringFormat (
					"Unable to process request due to %s errors",
					integerToDecimalString (
						taskLogger.errorCount ())),
				emptyList ()));

	}

	@Override
	protected
	void updateDatabase (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"updateDatabase");

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadWrite (
					stringFormat (
						"%s.%s ()",
						getClass ().getSimpleName (),
						"updateDatabase"),
					this);

		) {

			Oxygen8RouteInRec oxygen8RouteIn =
				oxygen8RouteInHelper.findRequired (
					requestContext.requestIntegerRequired (
						"smsRouteId"));

			RouteRec smsRoute =
				oxygen8RouteIn.getRoute ();

			List <MediaRec> medias =
				iterableMapToList (
					attachment ->
						processAttachment (
							taskLogger,
							attachment),
					request.attachments ());

			Optional <MediaRec> textMediaOptional =
				iterableFindFirst (
					mediaLogic::isText,
					medias);

			TextRec messageBodyText =
				textHelper.findOrCreate (
					optionalMapRequiredOrDefault (
						media ->
							utf8ToString (
								media.getContent ().getData ()),
						textMediaOptional,
						request.subject ()));

			NumberRec number =
				smsNumberHelper.findOrCreate (
					request.source ());

			smsInboxLogic.inboxInsert (
				optionalOf (
					request.messageId ()),
				messageBodyText,
				number,
				request.destination (),
				smsRoute,
				optionalAbsent (),
				optionalAbsent (),
				medias,
				optionalAbsent (),
				optionalOf (
					request.subject ()));

			transaction.commit ();

		}

	}

	@Override
	protected
	Responder createResponse (
			@NonNull TaskLogger taskLogger,
			@NonNull FormatWriter debugWriter) {

		return textResponderProvider.get ()

			.text (
				"SUCCESS");

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

		oxygen8InboundLogHelper.insert (
			oxygen8InboundLogHelper.createInstance ()

			.setRoute (
				smsRouteHelper.findRequired (
					Long.parseLong (
						requestContext.requestStringRequired (
							"smsRouteId"))))

			.setType (
				Oxygen8InboundLogType.mmsMessage)

			.setTimestamp (
				transaction.now ())

			.setDetails (
				debugLog)

			.setSuccess (
				success)

		);

		transaction.commit ();

	}

	// private implementation

	private
	MediaRec processAttachment (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Oxygen8RouteInMmsNewRequest.Attachment attachment) {

		if (
			stringEqualSafe (
				attachment.encoding (),
				"text")
		) {

			return mediaLogic.createTextMedia (
				attachment.content (),
				attachment.contentType (),
				attachment.fileName ());

		} else if (
			stringEqualSafe (
				attachment.encoding (),
				"base64")
		) {

			byte[] attachmentContent =
				bytesFromBase64 (
					attachment.content ());

			return mediaLogic.createMediaRequired (
				attachmentContent,
				attachment.contentType (),
				attachment.fileName (),
				optionalAbsent ());

		} else {

			throw shouldNeverHappen ();

		}

	}

	// misc

	private final static
	DataFromXml requestFromXml =
		new DataFromXmlBuilder ()

		.registerBuilderClasses (
			Oxygen8RouteInMmsNewRequest.class,
			Oxygen8RouteInMmsNewRequest.Attachment.class)

		.build ();

}
