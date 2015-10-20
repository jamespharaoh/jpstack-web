package wbs.smsapps.photograbber.daemon;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.RandomLogic;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.messageset.logic.MessageSetLogic;
import wbs.smsapps.photograbber.model.PhotoGrabberRec;
import wbs.smsapps.photograbber.model.PhotoGrabberRequestObjectHelper;
import wbs.smsapps.photograbber.model.PhotoGrabberRequestRec;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@Log4j
@PrototypeComponent ("photoGrabberCommand")
public
class PhotoGrabberCommand
	implements CommandHandler {

	// dependencies

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	Provider<MessageSender> messageSender;

	@Inject
	MessageSetLogic messageSetLogic;

	@Inject
	ObjectManager objectManager;

	@Inject
	PhotoGrabberRequestObjectHelper photoGrabberRequestHelper;

	@Inject
	RandomLogic randomLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Integer> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"photo_grabber.photo_grabber"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		Transaction transaction =
			database.currentTransaction ();

		PhotoGrabberRec photoGrabber =
			(PhotoGrabberRec) (Object)
			objectManager.getParent (
				command);

		String mediaRef =
			rest.trim ();

		MessageRec message =
			inbox.getMessage ();

		ServiceRec defaultService =
			serviceHelper.findByCode (
				photoGrabber,
				"default");

		PhotoGrabberRequestRec photoGrabberRequest =
			new PhotoGrabberRequestRec ()

			.setPhotoGrabber (
				photoGrabber)

			.setNumber (
				message.getNumber ())

			.setThreadId (
				message.getThreadId ())

			.setMediaRef (
				mediaRef)

			.setRequestTime (
				instantToDate (
					transaction.now ()));

		String mediaUrl;

		try {

			mediaUrl =
				lookupMediaUrl (
					photoGrabber.getUrl (),
					mediaRef);

		} catch (IOException exception) {

			throw new RuntimeException (
				exception);

		}

		if (mediaUrl == null) {

			log.warn("Media url lookup failed (message_id = "
					+ message.getId() + ")");

			photoGrabberRequest

				.setFound (
					false);

			photoGrabberRequestHelper.insert (
				photoGrabberRequest);

			messageSetLogic.sendMessageSet (
				messageSetLogic.findMessageSet (
					photoGrabber,
					"photo_grabber_not_found"),
				message.getThreadId (),
				message.getNumber (),
				defaultService);

			return inboxLogic.inboxProcessed (
				inbox,
				Optional.of (defaultService),
				Optional.<AffiliateRec>absent (),
				command);

		}

		MediaRec media =
			fetchMedia (
				mediaUrl,
				photoGrabber.getJpeg (),
				photoGrabber.getJpegWidth (),
				photoGrabber.getJpegHeight ());

		if (media == null) {

			log.warn ("Image download failed (message_id = "
					+ message.getId() + ")");

			photoGrabberRequest.setFound (
				false);

			photoGrabberRequestHelper.insert (
				photoGrabberRequest);

			messageSetLogic.sendMessageSet (
				messageSetLogic.findMessageSet (
					photoGrabber,
					"photo_grabber_not_found"),
				message.getThreadId (),
				message.getNumber (),
				defaultService);

			return inboxLogic.inboxProcessed (
				inbox,
				Optional.of (defaultService),
				Optional.<AffiliateRec>absent (),
				command);

		}

		photoGrabberRequest

			.setFound (
				true)

			.setMediaUrl (
				mediaUrl)

			.setMedia (
				media)

			.setCode (
				randomLogic.generateUppercase (8));

		String text =
			photoGrabber.getBillTemplate ().replaceAll (
				"\\{code\\}",
				photoGrabberRequest.getCode ());

		MessageRec billedMessage =
			messageSender.get ()

			.threadId (
				message.getThreadId ())

			.number (
				message.getNumber ())

			.messageString (
				text)

			.numFrom (
				photoGrabber.getBillNumber ())

			.route (
				photoGrabber.getBillRoute ())

			.service (
				defaultService)

			.deliveryTypeCode (
				"photo_grabber")

			.send ();

		photoGrabberRequest

			.setBilledMessage (
				billedMessage);

		photoGrabberRequestHelper.insert (
			photoGrabberRequest);

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.<AffiliateRec>absent (),
			command);

	}

	@SneakyThrows (IOException.class)
	MediaRec fetchMedia (
			String url,
			boolean jpeg,
			int jpegWidth,
			int jpegHeight) {

		byte[] data =
			fetchUrlData (url);

		return mediaLogic.createMediaFromImage (
			data,
			"image/jpeg",
			null);

	}

	enum HandlerState {

		start,
		inResponse,
		inFound,
		inUrl,
		end;

	};

	static
	class MyHandler
		extends DefaultHandler {

		HandlerState state =
			HandlerState.start;

		String found;
		String url;

		StringBuilder stringBuilder;

		@Override
		public
		void startElement (
				String uri,
				String localName,
				String qualifiedName,
				Attributes attributes)
			throws SAXException {

			switch (state) {

			case start:

				if (
					equal (
						qualifiedName,
						"photo-grabber-response")
				) {

					state =
						HandlerState.inResponse;

					return;

				} else {

					throw new SAXException (
						"First tag must be photograbber-response");

				}

			case inResponse:

				if (
					equal (
						qualifiedName,
						"found")
				) {

					if (found != null) {

						throw new SAXException (
							"More than one found tag");

					}

					stringBuilder =
						new StringBuilder ();

					state =
						HandlerState.inFound;

					return;

				} else if (qualifiedName.equals ("url")) {

					if (url != null)
						throw new SAXException ("More than one URL tag");

					stringBuilder =
						new StringBuilder ();

					state = HandlerState.inUrl;

					return;

				} else {

					throw new SAXException (
						stringFormat (
							"Invalid tag <%s> inside <photograbber-response>",
							qualifiedName));

				}

			default:

				throw new SAXException (
					"Unexpected tag " + qualifiedName);

			}

		}

		@Override
		public
		void endElement (
				String uri,
				String localName,
				String qName)
			throws SAXException {

			switch (state) {

			case inResponse:

				state =
					HandlerState.end;

				return;

			case inFound:

				found =
					stringBuilder.toString ();

				stringBuilder =
					null;

				state =
					HandlerState.inResponse;

				return;

			case inUrl:

				url =
					stringBuilder.toString ();

				stringBuilder =
					null;

				state =
					HandlerState.inResponse;

				return;

			default:

				throw new RuntimeException (
					"Logic error");

			}

		}

		@Override
		public
		void characters (
				char[] character,
				int start, int length)
			throws SAXException {

			switch (state) {

			case inFound:
			case inUrl:

				stringBuilder.append (
					character,
					start,
					length);

				return;

			default:

				String string =
					new String (
							character,
							start,
							length)
						.trim ();

				if (string.length () != 0) {

					throw new SAXException (
						"Extra characters found");

				}

			}

		}

	}

	public static
	String lookupMediaUrl (
			String baseUrl,
			String ref)
		throws IOException {

		// open the url

		URL url;

		try {

			url =
				new URL (
					stringFormat (
						"%s",
						baseUrl,
						"?ref=%u",
						ref));

		} catch (MalformedURLException exception) {

			throw new RuntimeException (
				exception);

		}

		URLConnection urlConnection =
			url.openConnection ();

		InputStream inputStream =
			urlConnection.getInputStream ();

		// parse the xml

		MyHandler handler =
			new MyHandler ();

		try {

			SAXParserFactory factory =
				SAXParserFactory.newInstance ();

			SAXParser saxParser =
				factory.newSAXParser ();

			saxParser.parse (
				inputStream,
				handler);

		} catch (ParserConfigurationException exception) {

			throw new RuntimeException (
				exception);

		} catch (SAXException exception) {

			log.error (
				stringFormat (
					"Error parsing response from %s",
					baseUrl),
				exception);

			return null;

		}

		// check the result

		if (handler.found == null) {

			log.error (
				stringFormat (
					"Response from %s contained no <found> element",
					baseUrl));

			return null;

		}

		if (
			notEqual (
				handler.found.toLowerCase (),
				"true")
		) {

			log.warn("Got not found from " + baseUrl + ", ref was " + ref);

			return null;

		}

		if (handler.url == null) {

			log.error("Response from " + baseUrl
					+ " contained found=true but no url.");

			return null;

		}

		return handler.url;

	}

	byte[] fetchUrlData (
			String url)
		throws IOException {

		try {

			// open the url

			URLConnection urlConnection =
				new URL (url).openConnection ();

			InputStream inputStream =
				urlConnection.getInputStream ();

			// load the data

			ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream ();

			byte[] bytes =
				new byte[8192];

			int numRead;

			while ((numRead = inputStream.read (bytes)) > 0) {

				byteArrayOutputStream.write (
					bytes,
					0,
					numRead);

			}

			// return

			return byteArrayOutputStream.toByteArray ();

		} catch (MalformedURLException exception) {

			throw new RuntimeException (
				exception);

		}

	}

}
