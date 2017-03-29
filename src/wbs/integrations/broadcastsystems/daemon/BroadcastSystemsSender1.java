package wbs.integrations.broadcastsystems.daemon;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import org.apache.commons.io.IOUtils;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.integrations.broadcastsystems.model.BroadcastSystemsRouteOutObjectHelper;
import wbs.integrations.broadcastsystems.model.BroadcastSystemsRouteOutRec;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender1;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteRec;

import wbs.web.utils.HtmlUtils;

@SingletonComponent ("broadcastSystemsSender1")
public
class BroadcastSystemsSender1
	extends AbstractSmsSender1 <BroadcastSystemsSender1.State> {

	// singleton dependencies

	@SingletonDependency
	BroadcastSystemsRouteOutObjectHelper broadcastSystemsRouteOutHelper;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull OutboxRec outbox) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getMessage");

		State state =
			new State ();

		// get stuff

		state.messageId = outbox.getId ();
		state.message = outbox.getMessage ();
		state.route = state.message.getRoute ();

		// lookup route out

		state.bsRouteOut =
			broadcastSystemsRouteOutHelper.findOrThrow (
				state.route.getId (),
				() -> tempFailure (
					stringFormat (
						"Broadcast systems outbound route not found for %s",
						state.route.getCode ())));

		// load lazy stuff

		state.message.getText ().getText ();
		state.message.getTags ().size ();

		state.servicePath =
			objectManager.objectPathMini (
				state.message.getService ());

		// pick a handler

		if (
			stringNotEqualSafe (
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
	Optional <List <String>> sendMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull State state) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendMessage");

		taskLogger.noticeFormat (
			"Sending message %s",
			integerToDecimalString (
				state.messageId));

		try {

			openConnection (
				state);

			return readResponse (
				taskLogger,
				state);

		} catch (IOException exception) {

			throw tempFailure (
				"IO error " + exception.getMessage ());

		}

	}

	public static
	class State {
		Long messageId;
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
				HtmlUtils.urlQueryParameterEncode (state.message.getText ().getText ())
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
	Optional <List <String>> readResponse (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull State state)
		throws
			IOException,
			SendFailureException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"readResponse");

		String responseString =
			IOUtils.toString (
				state.urlConn.getInputStream ());

		taskLogger.debugFormat (
			"Message %s response: %s",
			integerToDecimalString (
				state.messageId),
			responseString);

		if (state.urlConn.getResponseCode () == 200) {

			Matcher matcher =
				successPattern.matcher (
					responseString);

			if (! matcher.matches ()) {

				taskLogger.warningFormat (
					"Success response did not match: [%s]",
					responseString);

				exceptionLogger.logSimple (
					taskLogger,
					"unknown",
					stringFormat (
						"message %s",
						integerToDecimalString (
							state.messageId)),
					"Success response did not match",
					responseString,
					optionalAbsent (),
					GenericExceptionResolution.ignoreWithLoggedWarning);

				return null;

			}

			return Optional.of (
				ImmutableList.of (
					matcher.group (1)));

		} else {

			Matcher matcher =
				failurePattern.matcher (
					responseString);

			if (! matcher.matches ()) {

				taskLogger.warningFormat (
					"Failure response did not match: [%s]",
					responseString);

				exceptionLogger.logSimple (
					taskLogger,
					"unknown",
					stringFormat (
						"message %s",
						integerToDecimalString (
							state.messageId)),
					"Failure response did not match",
					responseString,
					optionalAbsent (),
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
