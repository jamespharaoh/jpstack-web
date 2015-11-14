package wbs.integrations.mig.daemon;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;

import wbs.framework.application.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.integrations.mig.model.MigRouteOutObjectHelper;
import wbs.integrations.mig.model.MigRouteOutRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender1;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.message.wap.model.WapPushMessageObjectHelper;
import wbs.sms.message.wap.model.WapPushMessageRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;

/**
 * Daemon service to process outbox items for mig routes.
 */
@Log4j
public
class MigSender
	extends AbstractSmsSender1<MigSender.MIGOutbox> {

	@Inject
	Database database;

	@Inject
	MigRouteOutObjectHelper migRouteOutHelper;

	@Inject
	WapPushMessageObjectHelper wapPushMessageHelper;

	@Inject
	WbsConfig wbsConfig;

	/**
	 * Class to hold all the DB information we need while sending a message.
	 */
	public static
	class MIGOutbox {
		int messageId;
		OutboxRec outbox;
		MessageRec message;
		RouteRec route;
		NetworkRec network;
		MigRouteOutRec migRouteOut;
		WapPushMessageRec wapPushMessage;
	}

	@Override
	protected String getThreadName () {
		return "MIGSndr";
	}

	@Override
	protected
	MIGOutbox getMessage (
			OutboxRec outbox)
		throws SendFailureException {

		MIGOutbox migOutbox =
			new MIGOutbox ();

		// get stuff

		migOutbox.messageId =
			outbox.getId ();

		migOutbox.message =
			outbox.getMessage ();

		migOutbox.route =
			migOutbox.message.getRoute ();

		migOutbox.network =
			migOutbox.message.getNetwork ();

		// lookup mig route

		migOutbox.migRouteOut =
			migRouteOutHelper.find (
				migOutbox.route.getId ());

		if (migOutbox.migRouteOut == null) {

			throw tempFailure (
				"MIG outbound route not found for " + migOutbox.route.getCode ());

		}

		// load lazy stuff

		migOutbox.message.getText ().getText ();

		migOutbox.route.getExpirySecs ();

		// pick a handler

		if (equal (
				migOutbox.message.getMessageType ().getCode (),
				"sms")) {

			// nothing to do

		} else if (equal (
				migOutbox.message.getMessageType ().getCode (),
				"wap_push")) {

			migOutbox.wapPushMessage =
				wapPushMessageHelper.find (
					outbox.getId ());

			if (migOutbox.wapPushMessage == null) {

				throw tempFailure (
					stringFormat (
						"Wap push message not found for message %d",
						outbox.getId ()));

			}

			migOutbox.wapPushMessage.getUrlText ().getText ();
			migOutbox.wapPushMessage.getTextText ().getText ();

		} else {

			// unrecognised message type

			throw tempFailure (
				stringFormat (
					"Don't know what to do with a %s",
					migOutbox.message.getMessageType ().getCode ()));

		}

		return migOutbox;

	}

	@Override
	protected
	String getSenderCode () {
		return "mig";
	}

	@Override
	protected
	String sendMessage (
			MIGOutbox migOutbox)
		throws SendFailureException {

		log.info ("Sending message " + migOutbox.messageId);

		try {

			// get params

			String params =
				getParams (migOutbox);

			log.debug ("Sending: " + params);

			// do the request

			HttpURLConnection urlConn =
				openConnection (
					migOutbox,
					params);

			// read the response

			String response =
				readResponse (urlConn);

			log.debug ("Response: " + response);

			// check the response

			String otherId =
				checkResponse (
					migOutbox,
					response);

			log.debug ("GUID: " + otherId);

			// and return

			return otherId;

		} catch (IOException exception) {

			String exceptionString =
				"IO error " + exception.getMessage () + " " + migOutbox.migRouteOut.getUrl ();

			log.error (
				exceptionString);

			throw tempFailure (
				exceptionString);

		}

	}

	/**
	 * Opens the connection, setting all appropriate parameters.
	 */
	HttpURLConnection openConnection (
			MIGOutbox migOutbox,
			String params)
		throws IOException {

		boolean isPost =
			equal (
				migOutbox.migRouteOut.getMethod (),
				"post");

		boolean isGet =
			equal (
				migOutbox.migRouteOut.getMethod (),
				"get");

		// work out the url

		String urlString =
			migOutbox.migRouteOut.getUrl ();

		if (isGet)
			urlString += "?" + params;

		URL urlObj =
			new URL (urlString);

		// create and open http connection

		HttpURLConnection urlConn =
			(HttpURLConnection)
			urlObj.openConnection ();

		urlConn.setConnectTimeout (10 * 1000);
		urlConn.setReadTimeout (10 * 1000);
		urlConn.setDoInput (
			true);

		urlConn.setDoOutput (
			isPost);

		urlConn.setAllowUserInteraction (
			false);

		urlConn.setRequestMethod (
			migOutbox.migRouteOut.getMethod ().toUpperCase ());

		urlConn.setRequestProperty (
			"User-Agent",
			wbsConfig.httpUserAgent ());

		if (isPost) {

			urlConn.setRequestProperty (
				"Content-Type",
				joinWithSeparator (
					"; ",
					"application/x-www-form-urlencoded",
					"charset=\"iso-8859-1\""));

			urlConn.setRequestProperty (
				"Content-Length",
				Integer.toString (params.length ()));

		}

		// send request

		if (isPost) {

			Writer out =
				new OutputStreamWriter (
					urlConn.getOutputStream (),
					"iso-8859-1");

			out.write (
				params);

			out.flush ();

		}

		return urlConn;

	}

	/**
	 * Reads the response from the connection into into a string (assumes utf-8
	 * encoding).
	 */
	String readResponse (
			HttpURLConnection urlConn)
		throws
			IOException,
			UnsupportedEncodingException {

		Reader in =
			new InputStreamReader (
				urlConn.getInputStream (),
				"utf-8");

		StringBuilder responseBuffer =
			new StringBuilder ();

		int numread;

		char buffer[] = new char [1024];

		while ((numread = in.read (buffer, 0, 1024)) > 0) {

			responseBuffer.append (
				buffer,
				0,
				numread);

		}

		return responseBuffer.toString ();

	}

	void checkErrorCode (
			String errorCode,
			String response) {

		log.info ("Check error code " + errorCode);

		if (! errorCode.equals ("000")) {

			if (errorCode.startsWith ("1")) {

				String fullError =
					"Server returned permanent failure: " + response;

				log.error (
					fullError);

				throw permFailure (
					fullError);

			} else if (errorCode.startsWith ("4")) {

				String fullError =
					"Server returned temporary failure: " + response;

				log.warn (
					fullError);

				throw tempFailure (
					fullError);

			} else if (errorCode.startsWith ("5")) {

				String fullError =
					"Server returned permanent failure: " + response;

				log.error (
					fullError);

				throw permFailure (
					fullError);

			} else {

				String fullError =
					"Server returned temporary failure: " + response;

				log.error (
					fullError);

				throw tempFailure (
					fullError);

			}

		}

	}

	String checkResponse (
			MIGOutbox httpOutbox,
			String response)
		throws SendFailureException {

		String guid = null;

		String errorCode =
			response.substring (0, 3);

		checkErrorCode (
			errorCode,
			response);

		int startIndex =
			response.indexOf ("I=");

		int endIndex1 =
			response.indexOf (" ", startIndex);

		int endIndex2 =
			response.indexOf ("}", startIndex);

		if (startIndex != -1 && (endIndex1 != -1 || endIndex2 != -1)) {

			guid =
				response.substring (
					startIndex + 2,
					endIndex1 != -1 ? endIndex1 : endIndex2);

		}

		return guid;

	}

	String formatNumberTo (
			String number) {

		if (number.startsWith ("0044") || number.startsWith ("+44")) {
			return number;
		} else if (number.startsWith ("44")) {
			return "00" + number;
		} else if (number.startsWith ("07")) {
			return "0044" + number.substring (1, number.length ());
		}

		return number;

	}

	String formatNumberFrom (
			String number) {

		if (number.startsWith ("07")) {
			return number;
		} else if (number.startsWith ("0044")) {
			return "0" + number.substring (4, number.length ());
		} else if (number.startsWith ("+44")) {
			return "0" + number.substring (3, number.length ());
		} else if (number.startsWith ("44")) {
			return "0" + number.substring (2, number.length ());
		}

		return number;

	}

	String getParams (
			MIGOutbox migOutbox)
		throws UnsupportedEncodingException {

		String encoding = "iso-8859-1";

		// get network connection

		String connection = null;

		int networkID =
			migOutbox.network.getId ();

		if (networkID == 1) {
			connection = "MIG01OU";
		} else if (networkID == 2) {
			connection = "MIG00VU";
		} else if (networkID == 3 || networkID == 5) {
			connection = "MIG01TU";
		} else if (networkID == 4) {
			connection = "MIG01XU";
		} else if (networkID == 6) {
			connection = "MIG01HU";
		}

		String numFrom =
			migOutbox.message.getNumFrom ();

		String numTo =
			migOutbox.message.getNumTo ();

		// free

		if (migOutbox.route.getOutCharge () == 0) {

			connection = null;

			numTo =
				formatNumberTo (numTo);

			numFrom =
				formatNumberFrom (numFrom);

		} else {

			numFrom =
				migOutbox.route.getNumber ();

			numTo =
				formatNumberTo (numTo);

		}

		// params

		StringBuilder stringBuilder =
			new StringBuilder ();

		if (migOutbox.wapPushMessage == null) {

			stringBuilder.append (
				"REQUESTTYPE=" + "0");

		} else {

			stringBuilder.append (
				"REQUESTTYPE=" + "16");

		}

		stringBuilder.append ("&OADC=" + URLEncoder.encode (numFrom, encoding));
		stringBuilder.append ("&OADCTYPE=" + Integer.toString (migOutbox.migRouteOut.getOadctype ()));
		stringBuilder.append ("&MESSAGEID=" + Integer.toString (migOutbox.messageId));
		stringBuilder.append ("&TIMETOLIVE=" + (migOutbox.route.getExpirySecs () / 60));
		stringBuilder.append ("&NUMBERS=" + URLEncoder.encode ((connection == null ? "" : (connection + ".")) + numTo, encoding));
		stringBuilder.append ("&PRIORITY=" + "N");

		if (migOutbox.wapPushMessage == null) {

			stringBuilder.append ("&BODY=" + URLEncoder.encode (migOutbox.message.getText ().getText (), encoding));

		} else {

			stringBuilder.append ("&TITLE=" + URLEncoder.encode (migOutbox.wapPushMessage.getTextText ().getText(), encoding));
			stringBuilder.append ("&BODY=" + URLEncoder.encode (migOutbox.wapPushMessage.getUrlText ().getText (), encoding));

		}

		// sb.append("&MCLASS="+?);
		// sb.append("&TARIFF="+?);
		// sb.append("&PID="+?);
		// sb.append("&BILLINGTEXT="+?);
		// sb.append("&DCS="+?);
		// sb.append("&CONNECTION="+?);

		return stringBuilder.toString ();

	}

}
