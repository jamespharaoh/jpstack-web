package wbs.integrations.oxygenate.api;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringIsEmpty;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.fileupload.FileItem;

import org.joda.time.Instant;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.oxygenate.model.OxygenateNetworkObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateRouteInObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateRouteInRec;

import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;

import wbs.sms.message.core.model.MessageTypeObjectHelper;
import wbs.sms.message.core.model.MessageTypeRec;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.web.context.RequestContext;
import wbs.web.responder.TextResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("oxygenateRouteInMmsOldApiAction")
public
class OxygenateRouteInMmsOldApiAction
	implements ApiAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	MessageTypeObjectHelper messageTypeHelper;

	@SingletonDependency
	NetworkObjectHelper networkHelper;

	@SingletonDependency
	OxygenateNetworkObjectHelper oxygenateNetworkHelper;

	@SingletonDependency
	OxygenateRouteInObjectHelper oxygenateRouteInHelper;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	NumberObjectHelper smsNumberHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <TextResponder> textResponderProvider;

	// state

	Long routeId;

	String mmsMessageId;
	String mmsMessageType;
	String mmsSenderAddress;
	String mmsRecipientAddress;
	Optional <String> mmsSubject;
	Instant mmsDate;
	String mmsNetwork;

	String messageString;
	TextRec messageText;

	List <MediaRec> medias =
		new ArrayList<> ();

	// implementation

	@Override
	public
	Optional <WebResponder> handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goApi");

		) {

			processRequestHeaders (
				transaction);

			processRequestBody (
				transaction);

			updateDatabase (
				transaction);

			transaction.commit ();

			return optionalOf (
				createResponse (
					transaction));

		}

	}

	void processRequestHeaders (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"processRequestHeaders");

		) {

			// route id

			routeId =
				requestContext.requestIntegerRequired (
					"routeId");

			// message id

			mmsMessageId =
				requestContext.headerRequired (
					"X-Mms-Message-Id");

			if (mmsMessageId.length () != 32) {

				taskLogger.errorFormat (
					"Header expected to be 32 characters but was %s: ",
					integerToDecimalString (
						mmsMessageId.length ()),
					"X-Mms-Message-Id");

			}

			// message type

			mmsMessageType =
				requestContext.headerRequired (
					"X-Mms-Message-Type");

			if (
				stringNotEqualSafe (
					mmsMessageType,
					"MO_MMS")
			) {

				taskLogger.errorFormat (
					"Header expected to equal 'MO_MMS' but was '%s': ",
					mmsMessageType,
					"X-Mms-Message-Type");

			}

			// sender address

			mmsSenderAddress =
				requestContext.headerRequired (
					"X-Mms-Sender-Address");

			if (
				stringIsEmpty (
					mmsSenderAddress)
			) {

				taskLogger.errorFormat (
					"Required header empty: X-Mms-Sender-Address");

			}

			// recipient address

			mmsRecipientAddress =
				requestContext.headerRequired (
					"X-Mms-Recipient-Address");

			if (
				stringIsEmpty (
					mmsRecipientAddress)
			) {

				taskLogger.errorFormat (
					"Required header empty: X-Mms-Recipient-Address");

			}

			// subject

			mmsSubject =
				optionalFromNullable (
					requestContext.headerRequired (
						"X-Mms-Subject"));

			// date

			String mmsDateParam =
				requestContext.headerRequired (
					"X-Mms-Date");

			try {

				mmsDate =
					Instant.parse (
						mmsDateParam);

			} catch (Exception exception) {

				taskLogger.errorFormat (
					"Error parsing header: X-Mms-Date");

			}

			// network

			mmsNetwork =
				requestContext.headerRequired (
					"X-Mms-Network");

			// errors

			taskLogger.makeException ();

		}

	}

	private
	void processRequestBody (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processRequestBody");

		) {

			for (
				FileItem fileItem
					: requestContext.fileItems ()
			) {

				Matcher matcher =
					contentTypePattern.matcher (
						fileItem.getContentType ());

				if (! matcher.matches ()) {

					transaction.errorFormat (
						"Invalid content type: %s",
						fileItem.getContentType ());

					continue;

				}

				String type =
					matcher.group (1);

				String charset =
					matcher.group (2);

				medias.add (
					mediaLogic.createMediaRequired (
						transaction,
						fileItem.get (),
						type,
						fileItem.getName (),
						optionalOf (
							charset)));

				if (

					messageString == null

					&& mediaLogic.isText (
						type)

					&& fileItem.getString () != null

					&& ! fileItem.getString ().isEmpty ()

				) {

					messageString =
						fileItem.getString ();

				}

			}

			if (messageString == null) {
				messageString = "";
			}

			transaction.makeException ();

		}

	}

	private
	void updateDatabase (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateDatabase");

		) {

			// lookup route

			OxygenateRouteInRec oxygenateRouteIn =
				oxygenateRouteInHelper.findRequired (
					transaction,
					routeId);

			RouteRec route =
				oxygenateRouteIn.getRoute ();

			// check route supports inbound mms

			if (! route.getCanReceive ())
				throw new RuntimeException ();

			MessageTypeRec mmsMessageType =
				messageTypeHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					"mms");

			if (! route.getInboundMessageTypes ().contains (
					mmsMessageType))
				throw new RuntimeException ();

			// lookup network

			/*
			Oxygen8NetworkRec oxygen8Network =
				oxygen8NetworkHelper.findByChannel (
					oxygen8RouteIn.getOxygen8Config (),
					mmsNetwork);

			if (oxygen8Network == null) {

				throw new RuntimeException (
					stringFormat (
						"No oxygen8 network for channel: %s",
						mmsNetwork));

			}

			NetworkRec network =
				oxygen8Network.getNetwork ();
			*/

			NetworkRec network =
				networkHelper.findRequired (
					transaction,
					0l);

			// insert message

			TextRec messageText =
				textHelper.findOrCreate (
					transaction,
					messageString);

			smsInboxLogic.inboxInsert (
				transaction,
				optionalOf (
					mmsMessageId),
				messageText,
				smsNumberHelper.findOrCreate (
					transaction,
					mmsSenderAddress),
				mmsRecipientAddress,
				route,
				optionalOf (
					network),
				optionalOf (
					mmsDate),
				medias,
				optionalAbsent (),
				mmsSubject);

		}

	}

	WebResponder createResponse (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createResponse");

		) {

			return textResponderProvider.provide (
				taskLogger,
				textResponder ->
					textResponder

				.text (
					"success")

			);

		}

	}

	public final static
	Pattern contentTypePattern =
		Pattern.compile (
			"(\\S+); charset=(\\S+)");

}
