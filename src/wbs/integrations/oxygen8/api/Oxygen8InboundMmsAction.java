package wbs.integrations.oxygen8.api;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.integrations.oxygen8.model.Oxygen8NetworkObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8NetworkRec;
import wbs.integrations.oxygen8.model.Oxygen8RouteInObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8RouteInRec;
import wbs.platform.api.ApiAction;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.text.web.TextResponder;
import wbs.sms.message.core.model.MessageTypeObjectHelper;
import wbs.sms.message.core.model.MessageTypeRec;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@PrototypeComponent ("oxygen8InboundMmsAction")
public
class Oxygen8InboundMmsAction
	extends ApiAction {

	// dependencies

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageTypeObjectHelper messageTypeHelper;

	@Inject
	Oxygen8NetworkObjectHelper oxygen8NetworkHelper;

	@Inject
	Oxygen8RouteInObjectHelper oxygen8RouteInHelper;

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

	Integer routeId;

	String mmsMessageId;
	String mmsMessageType;
	String mmsSenderAddress;
	String mmsRecipientAddress;
	String mmsSubject;
	Instant mmsDate;
	String mmsNetwork;

	String messageString;
	TextRec messageText;

	List<MediaRec> medias =
		new ArrayList<MediaRec> ();

	// implementation

	@Override
	protected
	Responder goApi () {

		processRequestHeaders ();
		processRequestBody ();

		updateDatabase ();

		return createResponse ();

	}

	void processRequestHeaders () {

		List<String> errors =
			new ArrayList<String> ();

		// route id

		routeId =
			requestContext.requestInt ("routeId");

		// message id

		mmsMessageId =
			requestContext.header ("X-Mms-Message-Id");

		if (mmsMessageId == null) {

			errors.add (
				"Required header missing: X-Mms-Message-Id");

		} else if (mmsMessageId.length () != 32) {

			errors.add (
				stringFormat (
					"Header expected to be 32 characters but was %s: ",
					mmsMessageId.length (),
					"X-Mms-Message-Id"));

		}

		// message type

		mmsMessageType =
			requestContext.header ("X-Mms-Message-Type");

		if (mmsMessageType == null) {

			errors.add (
				"Required header missing: X-Mms-Message-Type");

		} else if (! equal (mmsMessageType, "MO_MMS")) {

			errors.add (
				stringFormat (
					"Header expected to equal 'MO_MMS' but was '%s': ",
					mmsMessageType,
					"X-Mms-Message-Type"));

		}

		// sender address

		mmsSenderAddress =
			requestContext.header ("X-Mms-Sender-Address");

		if (mmsSenderAddress == null) {

			errors.add (
				"Required header missing: X-Mms-Sender-Address");

		} else if (mmsSenderAddress.isEmpty ()) {

			errors.add (
				"Required header empty: X-Mms-Sender-Address");

		}

		// recipient address

		mmsRecipientAddress =
			requestContext.header ("X-Mms-Recipient-Address");

		if (mmsRecipientAddress == null) {

			errors.add (
				"Required header missing: X-Mms-Recipient-Address");

		} else if (mmsRecipientAddress.isEmpty ()) {

			errors.add (
				"Required header empty: X-Mms-Recipient-Address");

		}

		// subject

		mmsSubject =
			requestContext.header ("X-Mms-Subject");

		// date

		String mmsDateParam =
			requestContext.header ("X-Mms-Date");

		if (mmsDateParam == null) {

			errors.add (
				"Required header missing: X-Mms-Date");

		} else {

			try {

				mmsDate =
					Instant.parse (mmsDateParam);

			} catch (Exception exception) {

				errors.add (
					"Error parsing header: X-Mms-Date");

			}

		}

		// network

		mmsNetwork =
			requestContext.header ("X-Mms-Network");

		if (mmsNetwork == null) {

			errors.add (
				"Required header missing: X-Mms-Network");

		}

		// errors

		if (! errors.isEmpty ()) {

			for (String error : errors) {
				log.error (error);
			}

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s errors logged parsing request headers",
					errors.size ()));

		}

	}

	@SneakyThrows (FileUploadException.class)
	void processRequestBody () {

		int errorCount = 0;

		for (FileItem fileItem
				: requestContext.fileItems ()) {

			log.error (
				stringFormat (
					"Attachment with name %s ",
					fileItem.getName (),
					"had content type %s",
					fileItem.getContentType ()));

			errorCount ++;

		}

		if (messageString == null)
			messageString = "";

		if (errorCount > 0)
			throw new RuntimeException ();

	}

	void updateDatabase () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// lookup route

		RouteRec route =
			routeHelper.find (
				routeId);

		if (route == null)
			throw new RuntimeException ();

		// lookup oxygen8 route

		Oxygen8RouteInRec oxygen8RouteIn =
			oxygen8RouteInHelper.find (
				route.getId ());

		if (oxygen8RouteIn == null)
			throw new RuntimeException ();

		// check route supports inbound mms

		if (! route.getCanReceive ())
			throw new RuntimeException ();

		MessageTypeRec mmsMessageType =
			messageTypeHelper.findByCode (
				GlobalId.root,
				"mms");

		if (! route.getInboundMessageTypes ().contains (
				mmsMessageType))
			throw new RuntimeException ();

		// lookup network

		Oxygen8NetworkRec oxygen8Network =
			oxygen8NetworkHelper.findByChannel (
				oxygen8RouteIn.getOxygen8Config (),
				mmsNetwork);

		if (oxygen8Network == null)
			throw new RuntimeException ();

		NetworkRec network =
			oxygen8Network.getNetwork ();

		// insert message

		TextRec messageText =
			textHelper.findOrCreate (
				messageString);

		inboxLogic.inboxInsert (
			mmsMessageId,
			messageText,
			mmsSenderAddress,
			mmsRecipientAddress,
			route,
			network,
			instantToDate (
				mmsDate),
			medias,
			null,
			mmsSubject);

		// commit transaction

		transaction.commit ();

	}

	Responder createResponse () {

		return textResponderProvider.get ()
			.text ("success");

	}

}
