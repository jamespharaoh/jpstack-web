package wbs.integrations.oxygen8.daemon;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;

import org.apache.commons.io.IOUtils;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.etc.Html;
import wbs.integrations.oxygen8.model.Oxygen8NetworkObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8NetworkRec;
import wbs.integrations.oxygen8.model.Oxygen8RouteOutObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8RouteOutRec;
import wbs.sms.gsm.Gsm;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender1;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@SingletonComponent ("oxygen8Sender")
public
class Oxygen8Sender
	extends AbstractSmsSender1<Oxygen8Sender.State> {

	// dependencies

	@Inject
	ObjectManager objectManager;

	@Inject
	Oxygen8NetworkObjectHelper oxygen8NetworkHelper;

	@Inject
	Oxygen8RouteOutObjectHelper oxygen8RouteOutHelper;

	@Inject
	WbsConfig wbsConfig;

	// details

	@Override
	protected
	String getThreadName () {
		return "Ox8Snd";
	}

	@Override
	protected
	String getSenderCode () {
		return "oxygen8";
	}

	// implementation

	@Override
	protected
	State getMessage (
			OutboxRec outbox) {

		State state =
			new State ();

		// get stuff

		state.messageId =
			outbox.getId ();

		state.message =
			outbox.getMessage ();

		state.route =
			state.message.getRoute ();

		// lookup route out

		state.oxygen8RouteOut =
			oxygen8RouteOutHelper.find (
				state.route.getId ());

		if (state.oxygen8RouteOut == null) {

			throw tempFailure (
				stringFormat (
					"Oxygen8 outbound route not found for %s",
					state.route.getCode ()));

		}

		// lookup network

		state.oxygen8Network =
			oxygen8NetworkHelper.find (
				state.oxygen8RouteOut.getOxygen8Config (),
				state.message.getNumber ().getNetwork ());

		if (state.oxygen8Network == null) {

			throw tempFailure (
				stringFormat (
					"Oxygen8 network not found for %s",
					state.message.getNumber ().getNetwork ().getId ()));

		}

		// load lazy stuff

		state.message.getText ().getText ();
		state.message.getTags ().size ();
		state.route.getCode ();

		state.servicePath =
			objectManager.objectPath (
				state.message.getService (),
				null,
				true,
				false);

		// pick a handler

		if (
			equal (
				state.message.getMessageType ().getCode (),
				"sms")
		) {

			// nothing to do

		} else {

			throw tempFailure (
				stringFormat (
					"Don't know what to do with a %s",
					state.message.getMessageType ().getCode ()));

		}

		return state;

	}

	@Override
	protected
	String[] sendMessage (
			State state) {

		log.info (
			stringFormat (
				"Sending message %s",
				state.messageId));

		try {

			openConnection (
				state);

			sendRequest (
				state);

			return readResponse (
				state);

		} catch (IOException exception) {

			throw tempFailure (
				stringFormat (
					"IO error %s",
					exception.getMessage ()));

		}

	}

	static
	class State {

		int messageId;
		OutboxRec outbox;
		MessageRec message;
		RouteRec route;
		Oxygen8RouteOutRec oxygen8RouteOut;
		Oxygen8NetworkRec oxygen8Network;
		String servicePath;
		HttpURLConnection urlConn;

	}

	void openConnection (
			State state)
		throws IOException {

		// create connection

		String urlString =
			state.oxygen8RouteOut.getRelayUrl ();

		URL url =
			new URL (
				urlString);

		state.urlConn =
			(HttpURLConnection)
			url.openConnection ();

		// set basic params

		state.urlConn.setDoOutput (true);
		state.urlConn.setDoInput (true);
		state.urlConn.setAllowUserInteraction (false);
		state.urlConn.setRequestMethod ("POST");

		// set request params

		state.urlConn.setRequestProperty (
			"Content-Type",
			joinWithSeparator (
				"; ",
				"application/x-www-form-urlencoded",
				"charset=UTF-8"));

		state.urlConn.setRequestProperty (
			"User-Agent",
			wbsConfig.httpUserAgent ());

	}

	void sendRequest (
			State state)
		throws IOException {

		Map<String,String> params =
			new LinkedHashMap<String,String> ();

		params.put (
			"Reference",
			state.message.getId ().toString ());

		if (
			isNotNull (
				state.oxygen8RouteOut.getCampaignId ())
		) {

			params.put (
				"CampaignID",
				state.oxygen8RouteOut.getCampaignId ());

		}

		params.put (
			"Username",
			state.oxygen8RouteOut.getUsername ());

		params.put (
			"Password",
			state.oxygen8RouteOut.getPassword ());

		// set multipart

		if (
			! Gsm.isGsm (
				state.message.getText ().getText ())
		) {

			throw permFailure (
				stringFormat (
					"Text contains non-GSM characters"));

		}

		int gsmLength =
			Gsm.length (
				state.message.getText ().getText ());

		boolean needMultipart =
			gsmLength > 160;

		boolean allowMultipart =
			state.oxygen8RouteOut.getMultipart ();

		if (
			needMultipart
			&& ! allowMultipart
		) {

			throw tempFailure (
				stringFormat (
					"Length is %s but multipart not enabled",
					gsmLength));

		}

		params.put (
			"Multipart",
			needMultipart
				? "1"
				: "0");

		// set shortcode and channel

		if (state.oxygen8RouteOut.getPremium ()) {

			params.put (
				"Shortcode",
				state.oxygen8RouteOut.getShortcode ());

			params.put (
				"Channel",
				state.oxygen8Network.getChannel ());

		} else {

			params.put (
				"Mask",
				state.message.getNumFrom ());

			params.put (
				"Channel",
				"BULK");

		}

		params.put (
			"MSISDN",
			state.message.getNumTo ());

		params.put (
			"Content",
			state.message.getText ().getText ());

		params.put (
			"Premium",
			state.route.getOutCharge () > 0
				? "1"
				: "0");

		StringBuilder paramsString =
			new StringBuilder ();

		for (Map.Entry<String,String> paramEntry
				: params.entrySet ()) {

			if (! params.isEmpty ())
				paramsString.append ('&');

			paramsString.append (
				paramEntry.getKey ());

			paramsString.append (
				'=');

			paramsString.append (
				Html.urlEncode (
					paramEntry.getValue ()));

		}

		OutputStream out =
			state.urlConn.getOutputStream ();

		IOUtils.write (
			paramsString.toString (),
			out);

	}

	public
	String[] readResponse (
			State state)
		throws
			IOException,
			SendFailureException {

		String responseString =
			IOUtils.toString (
				state.urlConn.getInputStream ());

		log.debug (
			stringFormat (
				"Message %s code %s response: [%s]",
				state.messageId,
				state.urlConn.getResponseCode (),
				responseString));

		if (state.urlConn.getResponseCode () == 200) {

			String responseLines[] =
				responseString.split ("\n");

			if (
				equal (
					responseLines [0],
					"101")
			) {

				return responseLines [2].split (",");

			}

			throw tempFailure (
				stringFormat (
					"Error %s: %s",
					responseLines [0],
					responseLines [1]));

		}

		throw tempFailure (
			stringFormat (
				"Server returned %s",
				state.urlConn.getResponseCode ()));

	}

}
