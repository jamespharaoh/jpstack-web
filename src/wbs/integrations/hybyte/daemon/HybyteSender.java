package wbs.integrations.hybyte.daemon;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;

import org.apache.commons.codec.binary.Base64;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.etc.Xom;
import wbs.integrations.hybyte.model.HybyteNetworkObjectHelper;
import wbs.integrations.hybyte.model.HybyteNetworkRec;
import wbs.integrations.hybyte.model.HybyteRouteOutObjectHelper;
import wbs.integrations.hybyte.model.HybyteRouteOutRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender1;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.message.wap.model.WapPushMessageObjectHelper;
import wbs.sms.message.wap.model.WapPushMessageRec;
import wbs.sms.network.logic.NetworkPrefixCache;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;

/**
 * Daemon service to process outbox items for hybyte routes.
 */
@Log4j
@SingletonComponent ("hybyteSender")
public
class HybyteSender
	extends AbstractSmsSender1<HybyteSender.HybyteOutbox> {

	@Inject
	HybyteNetworkObjectHelper hybyteNetworkHelper;

	@Inject
	HybyteRouteOutObjectHelper hybyteRouteOutHelper;

	@Inject
	NetworkPrefixCache networkPrefixCache;

	@Inject
	ObjectManager objectManager;

	@Inject
	WapPushMessageObjectHelper wapPushMessageHelper;

	@Inject
	WbsConfig wbsConfig;

	@Override
	protected
	String getThreadName () {
		return "HybSndr";
	}

	@Override
	protected
	String getSenderCode () {
		return "hybyte";
	}

	@Override
	protected
	HybyteOutbox getMessage (
			OutboxRec outbox) {

		HybyteOutbox hybyteOutbox =
			new HybyteOutbox ();

		// get stuff

		hybyteOutbox.messageId =
			outbox.getId ();

		hybyteOutbox.message =
			outbox.getMessage ();

		hybyteOutbox.route =
			hybyteOutbox.message.getRoute ();

		// lookup hybyte route

		hybyteOutbox.hybyteRouteOut =
			hybyteRouteOutHelper.find (
				hybyteOutbox.route.getId ());

		if (hybyteOutbox.hybyteRouteOut == null) {

			throw tempFailure (
				stringFormat (
					"Hybyte outbound route not found for %s",
					hybyteOutbox.route.getCode ()));

		}

		if (hybyteOutbox.route.getOutCharge () > 0) {

			// lookup network

			NetworkRec network =
				hybyteOutbox.message.getNumber ().getNetwork ();

			// lookup hybyte network

			hybyteOutbox.hybyteNetwork =
				hybyteNetworkHelper.find (
					network.getId ());

		}

		// load lazy stuff

		hybyteOutbox.message.getText ().getText ();

		hybyteOutbox.message.getTags ().size ();

		hybyteOutbox.servicePath =
			objectManager.objectPathMini (
				hybyteOutbox.message.getService ());

		// pick a handler

		if (equal (
				hybyteOutbox.message.getMessageType ().getCode (),
				"sms")) {

			// no action required

		} else if (equal (
				hybyteOutbox.message.getMessageType ().getCode (),
				"wap_push")) {

			hybyteOutbox.wapPushMessage =
				wapPushMessageHelper.find (
					outbox.getId ());

			if (hybyteOutbox.wapPushMessage == null) {

				throw tempFailure (
					stringFormat (
						"Wap push message not found for message %s",
						outbox.getId ()));

			}

			hybyteOutbox.wapPushMessage
				.getUrlText ().getText ();

			hybyteOutbox.wapPushMessage
				.getTextText ().getText ();

		} else {

			throw tempFailure (
				stringFormat (
					"Don't know what to do with a %s",
					hybyteOutbox.message.getMessageType ().getCode ()));

		}

		return hybyteOutbox;

	}

	@Override
	protected
	String sendMessage (
			HybyteOutbox hybyteOutbox) {

		log.info (
			stringFormat (
				"Sending message %s",
				hybyteOutbox.messageId));

		try {

			// open the connection

			HttpURLConnection urlConn =
				openConnection (hybyteOutbox);

			// send http request

			sendRequest (
				hybyteOutbox,
				urlConn.getOutputStream ());

			// and interpret the response

			return readResponse (
				urlConn.getInputStream ());

		} catch (IOException e) {

			throw tempFailure ("IO error " + e.getMessage());

		}

	}

	/**
	 * Class to hold all the DB information we need while sending a message.
	 */
	public static
	class HybyteOutbox {
		int messageId;
		OutboxRec outbox;
		MessageRec message;
		RouteRec route;
		HybyteRouteOutRec hybyteRouteOut;
		HybyteNetworkRec hybyteNetwork;
		WapPushMessageRec wapPushMessage;
		String servicePath;
	}

	/**
	 * Opens the connection, setting all appropriate parameters.
	 */
	HttpURLConnection openConnection (
			HybyteOutbox hybyteOutbox)
		throws IOException {

		// create connection

		URL url =
			new URL (hybyteOutbox.hybyteRouteOut.getUrl ());

		HttpURLConnection urlConnection =
			(HttpURLConnection)
			url.openConnection ();

		// set basic params

		urlConnection.setDoInput (true);
		urlConnection.setDoOutput (true);
		urlConnection.setAllowUserInteraction (false);
		urlConnection.setRequestMethod ("POST");

		// set request params

		urlConnection.setRequestProperty (
			"User-Agent",
			wbsConfig.httpUserAgent ());

		urlConnection.setRequestProperty (
			"Content-Type",
			"text/xml");

		// set authorization param

		String authString =
			stringFormat (
				"%s:%s",
				hybyteOutbox.hybyteRouteOut.getUsername (),
				hybyteOutbox.hybyteRouteOut.getPassword ());

		String authStringEncoded =
			Base64.encodeBase64String (
				authString.getBytes ("utf-8"));

		urlConnection.setRequestProperty (
			"Authorization",
			stringFormat (
				"Basic %s",
				authStringEncoded));

		// and return

		return urlConnection;

	}

	public
	void sendRequest (
			HybyteOutbox hybyteOutbox,
			OutputStream out)
		throws IOException {

		Element messageElem, bodyElem;

		Element airbyteElem =

			Xom.xomElem (
				"Airbyte",

				Xom.xomAttr (
					"xsi:noNamespaceSchemaLocation",
					"http://airbyte-dtds.airmessaging.net/airbyte.xsd",
					"http://www.w3.org/23001/XMLSchema-instance"),

				messageElem = Xom.xomElem (
					"message",

					Xom.xomAttr (
						"id",
						Integer.toString (hybyteOutbox.messageId)),

					Xom.xomAttr (
						"from",
						originatorFiddle (
							hybyteOutbox.message.getNumFrom ())),

					bodyElem = Xom.xomElem ("body"),
					Xom.xomElem (
						"to",
						"+" + hybyteOutbox.message.getNumTo ())));

		if (hybyteOutbox.wapPushMessage != null) {

			Xom.xomAppend (
				bodyElem,

				Xom.xomElem (
					"WapPush",
					hybyteOutbox.wapPushMessage.getUrlText ().getText ()),

				Xom.xomElem (
					"text",
					hybyteOutbox.wapPushMessage.getTextText ().getText ()));

		} else {

			Xom.xomAppend (
				bodyElem,

				Xom.xomElem (
					"text",
					Xom.xomAttr ("long", "1"),
					hybyteOutbox.message.getText ().getText ()));

		}

		if (hybyteOutbox.route.getOutCharge () > 0) {

			Element premiumElem;

			Xom.xomAppend (
				messageElem,

				premiumElem = Xom.xomElem (
					"premium",

					Xom.xomAttr (
						"cost",
						Integer.toString (
							hybyteOutbox.route.getOutCharge ()))));

			if (hybyteOutbox.hybyteNetwork != null) {

				Xom.xomAppend (
					premiumElem,

					Xom.xomAttr (
						"net",
						hybyteOutbox.hybyteNetwork.getText ()));

			} else {

				Xom.xomAppend (
					premiumElem,

					Xom.xomAttr (
						"lookup",
						"1"));

			}

		}

		// adult tag: adult

		if (hybyteOutbox.message.getTags ().contains ("adult")) {

			Xom.xomAppend (
				messageElem,

				Xom.xomElem (
					"application_id",
					"adult"));

		}

		// routes 84469_free: adult

		if (hybyteOutbox.message.getRoute ().getId () == 58) {

			Xom.xomAppend (
				messageElem,

				Xom.xomElem (
					"application_id",
					"adult"));

		}

		Document document =
			new Document (airbyteElem);

		log.debug (
			stringFormat (
				"Sent: %s",
				document.toXML ()));

		out.write (
			document.toXML ().getBytes ("utf-8"));

	}

	private static
	Pattern numberOrigPattern =
		Pattern.compile ("0[0-9]{9,10}");

	/**
	 * Hybyte's system actually required numeric originators to look like
	 * numbesr, so here we change any 0xxx numbers in the +44xxx format for
	 * them. Anything else is passed through as is. This is a nasty hack
	 * resulting from limitations in the original design of the WBS platform.
	 *
	 * @param orig
	 * @return
	 */
	private static
	String originatorFiddle (
			String orig) {

		if (numberOrigPattern.matcher (orig).matches ())
			return "+44" + orig.substring (1);

		return orig;

	}

	public
	String readResponse (
			InputStream inputStream)
		throws
			IOException,
			SendFailureException {

		Nodes nodes;
		Document document;

		try {

			Builder builder =
				new Builder ();

			document =
				builder.build (inputStream);

			log.debug ("Got: " + document.toXML ());

		} catch (ParsingException exception) {

			throw tempFailure("Invalid XML");

		}

		// check for an ok acknowledgement and return the uuid

		nodes =
			document.query ("/Airbyte/acknowledgement");

		if (nodes.size() == 1) {

			Element ackElem =
				(Element) nodes.get (0);

			if (equal (
					ackElem.getAttributeValue ("result"),
					"OK")) {

				return ackElem.getAttributeValue ("uuid");

			}

		}

		// check for an error message

		nodes =
			document.query ("/Airbyte/error");

		if (nodes.size () >= 1) {

			Element errorElem =
				(Element) nodes.get (0);

			String error =
				errorElem.getValue ();

			if (equal (
					error,
					"No content specified")) {

				String fullError =
					"Permanent failure: " + error;

				log.error (fullError);

				throw permFailure(fullError);

			}

			String fullError =
				"Unknown error: " + error;

			log.warn (fullError);

			throw tempFailure (fullError);

		}

		// throw a default error

		throw tempFailure (
			"Didn't understand response!");

	}

}
