package wbs.integrations.broadcastsystems.daemon;

import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.stringToBytes;
import static wbs.framework.utils.etc.Misc.stringToUrl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.object.ObjectManager;
import wbs.integrations.broadcastsystems.model.BroadcastSystemsRouteOutObjectHelper;
import wbs.integrations.broadcastsystems.model.BroadcastSystemsRouteOutRec;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender2;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.collect.ImmutableMap;

@SingletonComponent ("broadcastSystemsSender2")
@Log4j
public
class BroadcastSystemsSender2
	extends AbstractSmsSender2 {

	// dependencies

	@Inject
	BroadcastSystemsRouteOutObjectHelper broadcastSystemsRouteOutHelper;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	ObjectManager objectManager;

	@Inject
	WbsConfig wbsConfig;

	// details

	@Override
	protected
	String getThreadName () {
		return "BrsSndr2";
	}

	@Override
	public
	String senderCode () {
		return "broadcast_systems_2";
	}

	// implementation

	@Override
	protected
	SetupSendResult setupSend (
			OutboxRec outbox) {

		final MessageRec message =
			outbox.getMessage ();

		RouteRec route =
			outbox.getRoute ();

		BroadcastSystemsRouteOutRec broadcastSystemsRouteOut =
			broadcastSystemsRouteOutHelper.find (
				route.getId ());

		if (broadcastSystemsRouteOut == null) {

			return new SetupSendResult ()

				.status (
					SetupSendStatus.configError)

				.message (
					stringFormat (
						"Broadcast systems outbound route not found for route ",
						"%s",
						objectManager.objectPathMini (
							route)));

		}

		// check message type

		if (
			notEqual (
				message.getMessageType ().getCode (),
				"sms")
		) {

			return new SetupSendResult ()

				.status (
					SetupSendStatus.configError)

				.message (
					stringFormat (
						"Broadcast systems sender does not support message ",
						"type %s",
						message.getMessageType ().getCode ()));

		}

		// setup send

		log.info (
			stringFormat (
				"Sending message %s",
				message.getId ()));

		// create connection

		String urlString =
			stringFormat (
				"%s/%u/SendUTF8/%u/%u/%s",
				broadcastSystemsRouteOut.getBaseUrl (),
				broadcastSystemsRouteOut.getToken (),
				message.getNumFrom (),
				message.getNumTo (),
				Base64.encodeBase64String (
					stringToBytes (
						message.getText ().getText (),
						"utf-8")));

		log.debug (
			stringFormat (
				"Making request to: %s",
				urlString));

		URL url =
			stringToUrl (
				urlString);

		// create trace

		Map<String,Object> requestTrace =
			ImmutableMap.<String,Object>builder ()

			.put (
				"url",
				urlString)

			.put (
				"method",
				"GET")

			.build ();

		// return

		PerformSendCallable performSendCallable =
			new PerformSendCallable ()

			.messageId (
				message.getId ())

			.url (
				url);

		return new SetupSendResult ()

			.status (
				SetupSendStatus.success)

			.requestTrace (
				new JSONObject (
					requestTrace))

			.performSend (
				performSendCallable);

	}

	@Accessors (fluent = true)
	@Data
	class PerformSendCallable
		implements Callable<PerformSendResult> {

		int messageId;
		URL url;

		@Override
		public
		PerformSendResult call ()
			throws Exception {

			HttpURLConnection httpUrlConnection =
				(HttpURLConnection)
				url.openConnection ();

			// set basic params

			httpUrlConnection.setDoInput (true);
			httpUrlConnection.setDoOutput (false);
			httpUrlConnection.setAllowUserInteraction (false);
			httpUrlConnection.setRequestMethod ("GET");

			// set request params

			httpUrlConnection.setRequestProperty (
				"User-Agent",
				wbsConfig.httpUserAgent ());

			// read response

			String responseString =
				IOUtils.toString (
					httpUrlConnection.getInputStream ());

			Map<String,Object> responseTrace =
				ImmutableMap.<String,Object>builder ()

					.put (
						"status",
						httpUrlConnection.getResponseCode ())

					.put (
						"message",
						httpUrlConnection.getResponseMessage ())

					.put (
						"body",
						responseString)

					.build ();

			log.debug (
				stringFormat (
					"Message %s response: %s",
					messageId,
					responseString));

			if (httpUrlConnection.getResponseCode () == 200) {

				JSONObject response;

				try {

					response =
						(JSONObject)
						JSONValue.parseWithException (responseString);

				} catch (ParseException exception) {

					return new PerformSendResult ()

						.status (
							PerformSendStatus.unknownError)

						.message (
							stringFormat (
								"Error parsing response from remote system: %s",
								responseString))

						.responseTrace (
							new JSONObject (
								responseTrace));

				}

				if (response.containsKey ("message_id")) {

					return new PerformSendResult ()

						.status (
							PerformSendStatus.success)

						.otherIds (
							Collections.<String>singletonList (
								(String)
								response.get ("message_id")))

						.responseTrace (
							new JSONObject (
								responseTrace));

				}

				if (response.containsKey ("error")) {

					return new PerformSendResult ()

						.status (
							PerformSendStatus.remoteError)

						.message (
							stringFormat (
								"Error from remote system: %s",
								httpUrlConnection.getResponseCode (),
								response.get ("error")))

						.responseTrace (
							new JSONObject (
								responseTrace));

				}

				return new PerformSendResult ()

					.status (
						PerformSendStatus.unknownError)

					.message (
						stringFormat (
							"Unrecognised response from remote system: %s",
							responseString))

					.responseTrace (
						new JSONObject (
							responseTrace));

			} else {

				return new PerformSendResult ()

					.status (
						PerformSendStatus.unknownError)

					.message (
						stringFormat (
							"HTTP error %s from remote system: %s",
							httpUrlConnection.getResponseCode (),
							responseString))

					.responseTrace (
						new JSONObject (
							responseTrace));

			}

		}

	}

}
