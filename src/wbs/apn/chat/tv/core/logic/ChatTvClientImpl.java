package wbs.apn.chat.tv.core.logic;

import static wbs.framework.utils.etc.Misc.pluralise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import wbs.apn.chat.tv.core.model.ChatTvMessageRec;
import wbs.apn.chat.tv.core.model.ChatTvRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.framework.application.config.WbsConfig;
import wbs.platform.media.model.MediaRec;

@Log4j
public
class ChatTvClientImpl
	implements ChatTvClient {

	@Inject
	WbsConfig wbsConfig;

	@Override
	public
	void sendMessages (
			ChatTvRec chatTv,
			List<ChatTvMessageRec> messages,
			Mode mode) {

		try {

			// create xml

			String requestXml =
				createRequest (
					chatTv,
					messages,
					mode);

			log.info (
				stringFormat (
					"Sending %s to %s",
					pluralise (messages.size (), "message"),
					chatTv.getApiUrl ()));

			// perform request

			sendRequest (
				chatTv,
				requestXml);

		} catch (IOException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	@Override
	@SneakyThrows (IOException.class)
	public
	void uploadPicture (
			ChatTvRec chatTv,
			MediaRec media) {

		@Cleanup
		CloseableHttpClient httpClient =
			HttpClientBuilder.create ()
				.build ();

		HttpPost httpPost =
			new HttpPost (chatTv.getImageUrl ());

		httpPost.setEntity (
			MultipartEntityBuilder.create ()

			.setMode (
				HttpMultipartMode.BROWSER_COMPATIBLE)

			.addPart (
				"file",
				new ByteArrayBody (
					media.getContent ().getData (),
					ContentType.create (
						media.getMediaType ().getMimeType ()),
					stringFormat (
						"%s.jpg",
						media.getId ())))

			.build ());

		HttpResponse response =
			httpClient.execute (httpPost);

		if (response.getStatusLine ().getStatusCode () != 200) {

			throw new RuntimeException (
				stringFormat (
					"Picture uploaded returned %d (%s)",
					response.getStatusLine ().getStatusCode (),
					response.getStatusLine ().getReasonPhrase ()));

		}

	}

	public
	String createRequest (
			ChatTvRec chatTv,
			List<ChatTvMessageRec> messages,
			Mode mode) {

		Document document =
			DocumentHelper.createDocument ();

		// root <Messages> element

		Element root =
			document.addElement ("Messages");

		// iterate outboxes

		for (ChatTvMessageRec message : messages) {

			ChatUserRec chatUser =
				message.getChatUser ();

			MediaRec media =
				message.getMedia ();

			ChatUserImageRec chatUserImage =
				chatUser.getMainChatUserImage ();

			if (media == null && chatUserImage != null)
				media = chatUserImage.getMedia ();

			// add a <message> element

			Element messageElem =
				root.addElement (
					mode == Mode.carousel ? "mms" : "message");

			messageElem.addElement ("channelid")
				.addText (chatTv.getChannelId ());

			messageElem.addElement ("regionid")
				.addText ("nat");

			messageElem.addElement ("msgID")
				.addText (message.getId ().toString ());

			messageElem.addElement ("msg")
				.addText (message.getEditedText ().getText ());

			if (chatUser.getName () != null)

				messageElem.addElement ("nickname")
					.addText (chatUser.getName ());

			messageElem.addElement ("boxNum")
				.addText (chatUser.getCode ());

			messageElem.addElement ("msgType")
				.addText (message.getTextJockey () ? "1" : "0");

			if (media != null)

				messageElem.addElement (
						mode == Mode.carousel ? "imageRef" : "url")
					.addText (Integer.toString (
						media.getId ()));
		}

		// convert to string

		document.setXMLEncoding ("utf-8");

		return document.asXML ();

	}

	public
	String sendRequest (
			ChatTvRec chatTv,
			String requestXml)
			throws IOException {

		byte[] bytesOut =
			requestXml.getBytes ("utf-8");

		// create connection

		URL url =
			new URL (
				chatTv.getApiUrl ());

		HttpURLConnection httpConnection =
			(HttpURLConnection)
			url.openConnection ();

		// set parameters

		httpConnection.setConnectTimeout (
			5000);

		httpConnection.setReadTimeout (
			5000);

		httpConnection.setDoInput (
			true);

		httpConnection.setDoOutput (
			true);

		httpConnection.setAllowUserInteraction (
			false);

		httpConnection.setRequestMethod (
			"POST");

		httpConnection.setRequestProperty (
			"User-Agent",
			wbsConfig.httpUserAgent ());

		httpConnection.setRequestProperty (
			"Content-Type",
			"text/xml; charset=\"utf-8\"");

		httpConnection.setRequestProperty (
			"Content-Length",
			Integer.toString (
				bytesOut.length));

		log.debug (
			stringFormat (
				"Request url: %s",
				url.toString ()));

		log.debug (
			stringFormat (
				"Request body: %s",
				requestXml));

		// send request

		IOUtils.write (
			bytesOut,
			httpConnection.getOutputStream ());

		log.debug (
			stringFormat (
				"Response code: %s",
				httpConnection.getResponseCode ()));

		log.debug (
			stringFormat (
				"Response message: %s",
				httpConnection.getResponseMessage ()));

		// check status

		if (httpConnection.getResponseCode () != 200) {

			throw new RuntimeException (
				stringFormat (
					"Got response %d from %s",
					httpConnection.getResponseCode (),
					chatTv.getApiUrl ()));

		}

		// read response

		String responseXml =
			IOUtils.toString (
				httpConnection.getInputStream (),
				httpConnection.getContentEncoding ());

		log.debug (
			stringFormat (
				"Response body: %s",
				responseXml));

		return responseXml;

	}

}
