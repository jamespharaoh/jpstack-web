package wbs.integrations.broadcastsystems.daemon;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.etc.Html;
import wbs.integrations.broadcastsystems.model.BroadcastSystemsRouteOutObjectHelper;
import wbs.integrations.broadcastsystems.model.BroadcastSystemsRouteOutRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender1;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@SingletonComponent ("broadcastSystemsSender1")
public
class BroadcastSystemsSender1
	extends AbstractSmsSender1<BroadcastSystemsSender1.State> {

	@Inject
	BroadcastSystemsRouteOutObjectHelper broadcastSystemsRouteOutHelper;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	ObjectManager objectManager;

	@Inject
	WbsConfig wbsConfig;

	// details

	@Override
	protected
	String
	getThreadName () {
		return "BrsSndr";
	}

	@Override
	protected
	String
	getSenderCode () {
		return "broadcast_systems_1";
	}

	@Override
	protected
	State getMessage (
			OutboxRec outbox) {

		State state =
			new State ();

		// get stuff

		state.messageId = outbox.getId ();
		state.message = outbox.getMessage ();
		state.route = state.message.getRoute ();

		// lookup route out

		state.bsRouteOut =
			broadcastSystemsRouteOutHelper.find (
				state.route.getId ());

		if (state.bsRouteOut == null) {

			throw tempFailure (
				stringFormat (
					"Broadcast systems outbound route not found for %s",
					state.route.getCode ()));

		}

		// load lazy stuff

		state.message.getText ().getText ();
		state.message.getTags ().size ();

		state.servicePath =
			objectManager.objectPathMini (
				state.message.getService ());

		// pick a handler

		if (equal (
				state.message.getMessageType ().getCode (),
				"sms")) {

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
	String sendMessage (
			State state) {

		log.info (
			stringFormat (
				"Sending message %s",
				state.messageId));

		try {

			openConnection (state);

			return readResponse (state);

		} catch (IOException exception) {

			throw tempFailure (
				"IO error " + exception.getMessage ());

		}

	}

	public static
	class State {
		int messageId;
		OutboxRec outbox;
		MessageRec message;
		RouteRec route;
		BroadcastSystemsRouteOutRec bsRouteOut;
		String servicePath;
		HttpURLConnection urlConn;
	}

	void openConnection (
			State state)
		throws IOException {

		// create connection

		String urlString =
			stringFormat (
				"%s/%u/GBP0.00/%u/%u/%s",
				state.bsRouteOut.getBaseUrl (),
				state.bsRouteOut.getToken (),
				state.message.getNumFrom (),
				state.message.getNumTo (),
				Html.urlEncode (state.message.getText ().getText ())
					.replace ("+", "%20"));

		URL url =
			new URL (urlString);

		state.urlConn =
			(HttpURLConnection) url.openConnection ();

		// set basic params

		state.urlConn.setDoInput (true);
		state.urlConn.setDoOutput (false);
		state.urlConn.setAllowUserInteraction (false);
		state.urlConn.setRequestMethod ("GET");

		// set request params

		state.urlConn.setRequestProperty (
			"User-Agent",
			wbsConfig.httpUserAgent ());

	}

	final static
	Pattern successPattern =
		Pattern.compile (
			"^transaction_id=(.+)$");

	final static
	Pattern failurePattern =
		Pattern.compile (
			"^error=(.+)$");

	public
	String readResponse (
			@NonNull State state)
		throws
			IOException,
			SendFailureException {

		String responseString =
			IOUtils.toString (
				state.urlConn.getInputStream ());

		log.debug (
			stringFormat (
				"Message %s response: %s",
				state.messageId,
				responseString));

		if (state.urlConn.getResponseCode () == 200) {

			Matcher matcher =
				successPattern.matcher (
					responseString);

			if (! matcher.matches ()) {

				log.warn (
					stringFormat (
						"Success response did not match: [%s]",
						responseString));

				exceptionLogger.logSimple (
					"unknown",
					stringFormat (
						"message %s",
						state.messageId),
					"Success response did not match",
					responseString,
					Optional.<Integer>absent (),
					GenericExceptionResolution.ignoreWithLoggedWarning);

				return null;

			}

			return matcher.group (1);

		} else {

			Matcher matcher =
				failurePattern.matcher (
					responseString);

			if (! matcher.matches ()) {

				log.warn (
					stringFormat (
						"Failure response did not match: [%s]",
						responseString));

				exceptionLogger.logSimple (
					"unknown",
					stringFormat (
						"message %s",
						state.messageId),
					"Failure response did not match",
					responseString,
					Optional.<Integer>absent (),
					GenericExceptionResolution.ignoreWithLoggedWarning);

				throw tempFailure (
					stringFormat (
						"Failure response did not match: [%s]",
						responseString));

			}

			throw tempFailure (
				stringFormat (
					"Received error %s",
					matcher.group (1)));

		}

	}

}
