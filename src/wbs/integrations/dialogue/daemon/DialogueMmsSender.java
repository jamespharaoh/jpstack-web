package wbs.integrations.dialogue.daemon;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringToUtf8;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.dialogue.model.DialogueMmsRouteObjectHelper;
import wbs.integrations.dialogue.model.DialogueMmsRouteRec;

import wbs.platform.media.model.MediaRec;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender1;
import wbs.sms.message.outbox.model.OutboxRec;

@SingletonComponent ("dialogueMmsSender")
public
class DialogueMmsSender
	extends AbstractSmsSender1 <DialogueMmsSender.DialogueMmsOutbox> {

	// singleton dependencies

	@SingletonDependency
	DialogueMmsRouteObjectHelper dialogueMmsRouteHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	WbsConfig wbsConfig;

	// details

	@Override
	protected
	String getThreadName () {
		return "DlgMmsSnd";
	}

	@Override
	protected
	String getSenderCode () {
		return "dialogue_mms";
	}

	// implementation

	@Override
	protected
	DialogueMmsOutbox getMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull OutboxRec outbox)
		throws SendFailureException {

		DialogueMmsOutbox dialogueMmsOutbox =
			new DialogueMmsOutbox ();

		dialogueMmsOutbox.outbox =
			outbox;

		dialogueMmsOutbox.dialogueMmsRoute =
			dialogueMmsRouteHelper.findOrThrow (
				outbox.getRoute ().getId (),
				() -> tempFailure (
					stringFormat (
						"No Dialogue MMS route for message %s",
						integerToDecimalString (
							outbox.getMessage ().getId ()))));

		// initialise any lazy proxies

		dialogueMmsOutbox.outbox.getMessage ().getNumTo ();

		dialogueMmsOutbox.outbox.getMessage ().getText ().getText ();

		dialogueMmsOutbox.outbox.getMessage ().getSubjectText ().getText ();

		for (MediaRec media
				: dialogueMmsOutbox.outbox.getMessage ().getMedias ()) {

			media.getMediaType ().getMimeType ();

			media.getContent ().getData ();

		}

		return dialogueMmsOutbox;

	}

	@Override
	protected
	Optional <List <String>> sendMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull DialogueMmsOutbox dialogueMmsOutbox)
		throws SendFailureException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendMessage");

		taskLogger.debugFormat (
			"Sending %s",
			integerToDecimalString (
				dialogueMmsOutbox.outbox.getMessage ().getId ()));

		try (

			CloseableHttpResponse httpResponse =
				doRequest (
					taskLogger,
					dialogueMmsOutbox);

		) {

			return doResponse (
				taskLogger,
				httpResponse);

		} catch (IOException exception) {

			throw tempFailure (
				stringFormat (
					"Got IO error: %s",
					exception.getMessage ()));

		}

	}

	private
	CloseableHttpResponse doRequest (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull DialogueMmsOutbox dialogueMmsOutbox)
		throws
			SendFailureException,
			IOException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doRequest");

		MessageRec smsMessage =
			dialogueMmsOutbox.outbox.getMessage ();

		try (

			CloseableHttpClient httpClient =
				HttpClientBuilder.create ()
					.build ();

		) {

			HttpPost post =
				new HttpPost (
					dialogueMmsOutbox.dialogueMmsRoute.getUrl ());

			MultipartEntityBuilder multipartEntityBuilder =
				MultipartEntityBuilder.create ();

			multipartEntityBuilder.addTextBody (
				"X-Mms-Account",
				dialogueMmsOutbox.dialogueMmsRoute.getAccount ());

			multipartEntityBuilder.addTextBody (
				"X-Mms-Username",
				dialogueMmsOutbox.dialogueMmsRoute.getUsername ());

			multipartEntityBuilder.addTextBody (
				"X-Mms-Password",
				dialogueMmsOutbox.dialogueMmsRoute.getPassword ());

			multipartEntityBuilder.addTextBody (
				"X-Mms-Recipient-Addresses",
				smsMessage.getNumTo ());

			multipartEntityBuilder.addTextBody (
				"X-Mms-Originating-Address",
				smsMessage.getNumFrom ());

			multipartEntityBuilder.addTextBody (
				"X-Mms-User-Tag",
				dialogueMmsOutbox.dialogueMmsRoute.getAccount ());

			multipartEntityBuilder.addTextBody (
				"X-Mms-User-Key",
				Long.toString (
					smsMessage.getId ()));

			if (isNotNull (
					smsMessage.getSubjectText ())) {

				multipartEntityBuilder.addTextBody (
					"X-Mms-Subject",
					smsMessage.getSubjectText ().getText ());

			}

			multipartEntityBuilder.addTextBody (
				"X-Mms-Delivery-Report",
				smsMessage.getRoute ().getDeliveryReports ()
					? "Y"
					: "N");

			if (smsMessage.getRoute ().getDeliveryReports ()) {

				// TODO what?

				multipartEntityBuilder.addTextBody (
					"X-Mms-Reply-Path",
					stringFormat (
						"%s",
						wbsConfig.apiUrl (),
						"/dialogueMMS/route",
						"/%u",
						integerToDecimalString (
							smsMessage.getRoute ().getId ()),
						"/report"));

			}

			String smilString =
				createSmil (
					dialogueMmsOutbox);

			taskLogger.noticeFormat (
				"SMIL: %s",
				smilString);

			multipartEntityBuilder.addBinaryBody (
				"filename",
				smilString.getBytes (),
				ContentType.create (
					"application/smil",
					"utf-8"),
				"ordering.smi");

			for (
				MediaRec media
					: dialogueMmsOutbox.outbox.getMessage ().getMedias ()
			) {

				String contentType =
					media.getMediaType ().getMimeType ();

				if (media.getEncoding() != null) {

					contentType =
						stringFormat (
							"%s; charset=%s",
							contentType,
							media.getEncoding ());

				}

				multipartEntityBuilder.addBinaryBody (
					"filename",
					media.getContent ().getData (),
					ContentType.create (
						contentType),
					ifNull (
						media.getFilename (),
						"file.jpg"));

			}

			post.setEntity (
				multipartEntityBuilder.build ());

			return httpClient.execute (
				post);

		}

	}

	Optional <List <String>> doResponse (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull HttpResponse response)
		throws
			IOException,
			SendFailureException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doResponse");

		MyHandler handler =
			new MyHandler ();

		try {

			BufferedReader bufferedReader =
				new BufferedReader (
					new InputStreamReader (
						response.getEntity ().getContent ()));

			StringBuilder stringBuilder =
				new StringBuilder ();

			String line;

			while ((line = bufferedReader.readLine ()) != null) {

				stringBuilder.append (
					line);

			}

			taskLogger.noticeFormat (
				"Response: %s",
				stringBuilder.toString ());

			ByteArrayInputStream byteArrayInputStream =
				new ByteArrayInputStream (
					stringToUtf8 (
						stringBuilder.toString ()));

			SAXParserFactory saxParserFactory =
				SAXParserFactory.newInstance ();

			SAXParser saxParser =
				saxParserFactory.newSAXParser ();

			saxParser.parse (
				byteArrayInputStream,
				handler);

		} catch (ParserConfigurationException exception) {

			throw new RuntimeException (
				exception);

		} catch (SAXException exception) {

			return null;

		}

		if (handler.statusCode == null) {

			taskLogger.errorFormat (
				"Invalid XML received: no MessageStatusCode");

			throw tempFailure (
				"Invalid XML received: no MessageStatusCode");

		}

		if (handler.statusText == null) {

			taskLogger.errorFormat (
				"Invalid XML received: no MessageStatusText");

			throw tempFailure (
				"Invalid XML received: no MessageStatusText");

		}

		if (!handler.statusCode.equals("00")
				|| !handler.messageStatusCode.equals("00")) {

			throw permFailure("Error: " + handler.statusCode + " "
					+ handler.messageStatusCode + " (" + handler.statusText
					+ ")");
		}

		if (handler.messageId == null) {

			taskLogger.errorFormat (
				"Invalid XML received: no MessageId");

			throw tempFailure (
				"Invalid XML received: no MessageId");

		}

		return Optional.of (
			ImmutableList.of (
				handler.messageId));

	}

	enum HandlerState {
		start,
		message,
		results,
		messageId,
		messageStatusCode,
		messageStatusText,
		end;
	};

	String createSmil (
			DialogueMmsOutbox dialogueMmsOutbox) {

		StringBuilder smilStringBuilder =
			new StringBuilder ();

		smilStringBuilder.append (
			"<smil><head><layout>");

		// smil+="<root-layout width=\"160\" height=\"120\"/>";

		smilStringBuilder.append (
			"<region id=\"Image\" fit=\"scroll\"/>");

		smilStringBuilder.append (
			"<region id=\"Text\" fit=\"scroll\"/>");

		smilStringBuilder.append (
			"</layout></head><body>");

		List<MediaRec> medias =
			dialogueMmsOutbox.outbox.getMessage ().getMedias ();

		for (
			int index = 0;
			index < medias.size ();
			index += 2
		) {

			MediaRec first =
				medias.get (index);

			MediaRec second = null;

			if (index + 1 < medias.size ()) {

				second =
					medias.get (
						index + 1);
			}

			smilStringBuilder.append (
				"<par dur=\"4s\">");

			if (first.getMediaType ().getId ().intValue () == 1) {

				smilStringBuilder.append (
					stringFormat (
						"<text",
						" region=\"Text\"",
						" src=\"%h\"",
						first.getFilename (),
						"/>"));

			} else {

				smilStringBuilder.append (
					stringFormat (
						"<img",
						" region=\"Image\"",
						" src=\"%h\"",
						first.getFilename (),
						"/>"));

			}

			if (second != null) {

				if (second.getMediaType ().getId ().intValue () == 1) {

					smilStringBuilder.append (
						stringFormat (
							"<text",
							" region=\"Text\"",
							" src=\"%h\"",
							second.getFilename (),
							"/>"));

				} else {

					smilStringBuilder.append (
						stringFormat (
							"<img",
							" region=\"Image\"",
							" src=\"%h\"",
							second.getFilename (),
							"/>"));

				}

			}

			smilStringBuilder.append (
				"</par>");

		}

		smilStringBuilder.append (
			"</body></smil>");

		return smilStringBuilder.toString ();

	}

	static
	class MyHandler
		extends DefaultHandler {

		HandlerState state = HandlerState.start;
		String messageId, statusCode, statusText, messageStatusCode;
		StringBuffer sb;

		@Override
		public
		void startElement (
				String uri,
				String localName,
				String qName,
				Attributes attributes)
			throws SAXException {

			switch (state) {
			case start:
				if (qName.equals("X-Mms-Submission")) {
					state = HandlerState.message;
				}
				return;
			case message:
				if (qName.equals("X-Mms-Error-Code")) {
					if (statusCode != null)
						throw new SAXException("More than one StatusCode tag");
					sb = new StringBuffer();
					state = HandlerState.messageStatusCode;
				} else if (qName.equals("X-Mms-Error-Description")) {
					if (statusText != null)
						throw new SAXException("More than one StatusText tag");
					sb = new StringBuffer();
					state = HandlerState.messageStatusText;
				} else if (qName.equals("X-Mms-Submission")) {
					throw new SAXException("More than one Message tag");
				} else if (qName.equals("X-Mms-Submission-Results")) {
					state = HandlerState.results;
				}
				return;
			case results:
				if (qName.equals("X-Mms-Result")) {
					if (messageId != null)
						throw new SAXException("More than one X-Mms-Result tag");
					messageId = attributes.getValue("X-Mms-ID");
					messageStatusCode = attributes.getValue("X-Mms-Status");
				}
				break;
			case messageId:
			case messageStatusCode:
			case messageStatusText:
				throw new SAXException("Unexpected tag " + qName);
			case end:
				if (qName.equals("X-Mms-Submission"))
					throw new SAXException("More than one Message tag");
			default:
				// just ignore
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

			case message:
				if (qName.equals("X-Mms-Submission"))
					state = HandlerState.end;
				return;

			case results:
				sb = null;
				state = HandlerState.message;
				return;

			case messageStatusCode:
				statusCode = sb.toString();
				sb = null;
				state = HandlerState.message;
				return;

			case messageStatusText:
				statusText = sb.toString();
				sb = null;
				state = HandlerState.message;
				return;

			default:
				// do nothing

			}

		}

		@Override
		public
		void characters (
				char[] ch,
				int start,
				int length)
			throws SAXException {

			switch (state) {
			case messageId:
			case messageStatusCode:
			case messageStatusText:
				sb.append(ch, start, length);
				return;
			default:
				return;
			}

		}

	}

	public static
	class DialogueMmsOutbox {
		OutboxRec outbox;
		DialogueMmsRouteRec dialogueMmsRoute;
	}

}
