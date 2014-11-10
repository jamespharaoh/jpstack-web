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
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
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

		processRequest ();

		updateDatabase ();

		return createResponse ();

	}

	@SneakyThrows (FileUploadException.class)
	void processRequest () {

		routeId =
			requestContext.requestInt ("routeId");

		// message id

		mmsMessageId =
			requestContext.parameter ("mmsMessageId");

		if (mmsMessageId == null)
			throw new RuntimeException ();

		if (mmsMessageId.length () != 32)
			throw new RuntimeException ();

		// message type

		mmsMessageType =
			requestContext.parameter ("mmsMessageType");

		if (mmsMessageType == null)
			throw new RuntimeException ();

		if (! equal (mmsMessageType, "MO_MMS"))
			throw new RuntimeException ();

		// sender address

		mmsSenderAddress =
			requestContext.parameter ("mmsSenderAddress");

		if (mmsSenderAddress == null)
			throw new RuntimeException ();

		if (mmsSenderAddress.isEmpty ())
			throw new RuntimeException ();

		// recipient address

		mmsRecipientAddress =
			requestContext.parameter ("mmsRecipientAddress");

		if (mmsRecipientAddress == null)
			throw new RuntimeException ();

		if (mmsRecipientAddress.isEmpty ())
			throw new RuntimeException ();

		// subject

		mmsSubject =
			requestContext.parameter ("mmsSubject");

		// date

		String mmsDateParam =
			requestContext.parameter ("mmsDate");

		if (mmsDateParam == null)
			throw new RuntimeException ();

		mmsDate =
			Instant.parse (mmsDateParam);

		// network

		mmsNetwork =
			requestContext.parameter ("mmsNetwork");

		if (mmsNetwork == null)
			throw new RuntimeException ();

		// attachments

		ServletRequestContext fileUploadContext =
			requestContext.getFileUploadServletRequestContext ();

		if (! FileUploadBase.isMultipartContent (
				fileUploadContext))
			throw new RuntimeException ();

		DiskFileItemFactory fileItemFactory =
			new DiskFileItemFactory ();

		ServletFileUpload fileUpload =
			new ServletFileUpload (fileItemFactory);

		List<FileItem> fileItems =
			fileUpload.parseRequest (
				requestContext.request ());

		int errorCount = 0;

		for (FileItem fileItem
				: fileItems) {

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

	}

	Responder createResponse () {

		return textResponderProvider.get ()
			.text ("success");

	}

}
