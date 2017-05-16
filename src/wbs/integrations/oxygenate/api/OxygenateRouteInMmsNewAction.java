package wbs.integrations.oxygenate.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.IterableUtils.iterableFilter;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.BinaryUtils.bytesFromBase64;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIsEmpty;
import static wbs.utils.string.StringUtils.stringIsNotEmpty;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.string.StringUtils.stringNotInSafe;
import static wbs.utils.string.StringUtils.stringSplitSlash;
import static wbs.utils.string.StringUtils.utf8ToString;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiLoggingAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.oxygenate.model.OxygenateInboundLogObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateInboundLogType;
import wbs.integrations.oxygenate.model.OxygenateRouteInObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateRouteInRec;

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

@PrototypeComponent ("oxygenateRouteInMmsNewAction")
public
class OxygenateRouteInMmsNewAction
	extends ApiLoggingAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	OxygenateInboundLogObjectHelper oxygenateInboundLogHelper;

	@SingletonDependency
	OxygenateRouteInObjectHelper oxygenateRouteInHelper;

	@SingletonDependency
	OxygenateRouteInMmsNewRequestBuilder requestBuilder;

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

	OxygenateRouteInMmsNewRequest request;

	Boolean success = false;

	// abstract implementation

	@Override
	protected
	void processRequest (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter debugWriter) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"processRequest");

		) {

			// read request

			byte[] requestBytes =
				requestContext.requestBodyRaw ();

			String requestString =
				utf8ToString (
					requestBytes);

			debugWriter.writeLineFormat (
				"===== REQUEST BODY =====");

			debugWriter.writeNewline ();

			debugWriter.writeString (
				stringTrim (
					requestString));

			debugWriter.writeNewline ();
			debugWriter.writeNewline ();

			// decode request

			Optional <OxygenateRouteInMmsNewRequest> requestOptional =
				requestBuilder.readInputStream (
					taskLogger,
					new ByteArrayInputStream (
						requestBytes));

			if (
				optionalIsNotPresent (
					requestOptional)
			) {

				throw new HttpUnprocessableEntityException (
					"Unable to interpret MMS request",
					emptyList ());

			}

			request =
				optionalGetRequired (
					requestOptional);

			// simple verification

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
				OxygenateRouteInMmsNewRequest.Attachment attachment
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

			// check for errors

			taskLogger.makeException (
				() -> new HttpUnprocessableEntityException (
					stringFormat (
						"Unable to process request due to %s errors",
						integerToDecimalString (
							taskLogger.errorCount ())),
					emptyList ()));

		}

	}

	@Override
	protected
	void updateDatabase (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"updateDatabase");

		) {

			OxygenateRouteInRec oxygenateRouteIn =
				oxygenateRouteInHelper.findRequired (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"smsRouteId")));

			RouteRec smsRoute =
				oxygenateRouteIn.getRoute ();

			List <MediaRec> medias =
				iterableMapToList (
					request.attachments (),
					attachment ->
						processAttachment (
							transaction,
							attachment));

			// combine text parts

			StringBuilder stringBuilder =
				new StringBuilder ();

			if (
				stringIsNotEmpty (
					request.subject ())
			) {

				stringBuilder.append (
					stringTrim (
						request.subject ()));

				stringBuilder.append (
					":\n");

			}

			for (
				MediaRec textMedia
					: iterableFilter (
						mediaLogic::isText,
						medias)
			) {

				String mediaString =
					utf8ToString (
						textMedia.getContent ().getData ());

				if (
					stringIsEmpty (
						mediaString)
				) {
					continue;
				}

				stringBuilder.append (
					stringTrim (
						mediaString));

				stringBuilder.append (
					"\n");

			}

			TextRec messageBodyText =
				textHelper.findOrCreate (
					transaction,
					stringBuilder.toString ());

			// lookup number, discarding extra info

			NumberRec number =
				smsNumberHelper.findOrCreate (
					transaction,
					listFirstElementRequired (
						stringSplitSlash (
							request.source ())));

			// insert message

			smsInboxLogic.inboxInsert (
				transaction,
				optionalOf (
					request.oxygenateReference ()),
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

			success = true;

		}

	}

	@Override
	protected
	Responder createResponse (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter debugWriter) {

		debugWriter.writeLineFormat (
			"===== RESPONSE =====");

		debugWriter.writeNewline ();

		debugWriter.writeLineFormat (
			"SUCCESS");

		debugWriter.writeNewline ();

		return textResponderProvider.get ()

			.text (
				"SUCCESS\n");

	}

	@Override
	protected
	void storeLog (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String debugLog) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"storeLog");

		) {

			oxygenateInboundLogHelper.insert (
				transaction,
				oxygenateInboundLogHelper.createInstance ()

				.setRoute (
					smsRouteHelper.findRequired (
						transaction,
						Long.parseLong (
							requestContext.requestStringRequired (
								"smsRouteId"))))

				.setType (
					OxygenateInboundLogType.mmsMessage)

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

	// private implementation

	private
	MediaRec processAttachment (
			@NonNull Transaction parentTransaction,
			@NonNull OxygenateRouteInMmsNewRequest.Attachment attachment) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processAttachment");

		) {

			if (
				stringEqualSafe (
					attachment.encoding (),
					"text")
			) {

				return mediaLogic.createTextMedia (
					transaction,
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
					transaction,
					attachmentContent,
					attachment.contentType (),
					attachment.fileName (),
					optionalOf (
						"utf8"));

			} else {

				throw shouldNeverHappen ();

			}

		}

	}

}
