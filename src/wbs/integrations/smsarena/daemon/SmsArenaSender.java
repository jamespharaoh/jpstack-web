package wbs.integrations.smsarena.daemon;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.stringToUrl;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.etc.Html;
import wbs.integrations.smsarena.model.SmsArenaRouteOutObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaRouteOutRec;
import wbs.platform.exception.logic.ExceptionLogLogic;
import wbs.sms.gsm.GsmUtils;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender2;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@SingletonComponent ("smsArenaSender")
public
class SmsArenaSender
	extends AbstractSmsSender2 {

	// dependencies

	@Inject
	SmsArenaRouteOutObjectHelper smsArenaRouteOutHelper;

	@Inject
	ExceptionLogLogic exceptionLogic;

	@Inject
	ObjectManager objectManager;

	@Inject
	WbsConfig wbsConfig;

	// details

	@Override
	protected
	String getThreadName () {
		return "SmsASndr";
	}

	@Override
	public
	String senderCode () {
		return "sms_arena";
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

		Optional<SmsArenaRouteOutRec> smsArenaRouteOutOptional =
				smsArenaRouteOutHelper.find (
				route.getId ());

		if (
			isNotPresent (
				smsArenaRouteOutOptional)
		) {

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

		SmsArenaRouteOutRec smsArenaRouteOut =
			smsArenaRouteOutOptional.get ();

		// check message type

		if (
			stringNotEqualSafe (
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

		// create params map for the request query string

		Map<String,String> params =
				new LinkedHashMap<String,String> ();

		params.put (
			"auth_key",
			smsArenaRouteOut.getAuthKey ());

		if (
			isNotNull (
				smsArenaRouteOut.getCid ())
		) {

			params.put (
				"cid",
				smsArenaRouteOut.getCid ());

		}

		if (
			isNotNull (
				smsArenaRouteOut.getPid ())
		) {

			params.put (
				"cid",
				smsArenaRouteOut.getPid ());

		}

		if (
			isNotNull (
				smsArenaRouteOut.getSid ())
		) {

			params.put (
				"cid",
				smsArenaRouteOut.getSid ());

		}

		params.put (
			"id",
			message.getId ().toString ());

		params.put (
			"from",
			message.getNumFrom ());

		params.put (
			"to",
			message.getNumTo ());

		params.put (
			"text",
			message.getText ().getText ());

		if (
			isNotNull (
				smsArenaRouteOut.getMclass ())
		) {

			params.put (
				"mclass",
				smsArenaRouteOut.getMclass ().toString ());

		}

		if (
			isNotNull (
				smsArenaRouteOut.getCoding ())
		) {

			params.put (
				"coding",
				smsArenaRouteOut.getCoding ().toString ());

		}

		if (
			isNotNull (
				smsArenaRouteOut.getValidity ())
		) {

			params.put (
				"validity",
				smsArenaRouteOut.getValidity ().toString ());

		}

		if (
			isNotNull (
				smsArenaRouteOut.getDate ())
		) {

			params.put (
				"date",
				smsArenaRouteOut.getDate ().toString ());

		}

		// set multipart

		if (
			! GsmUtils.gsmStringIsValid (
				params.get("text"))
		) {

			throw new RuntimeException (
					"Text contains non-GSM characters");

		}

		// build the request query string

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
				Html.urlQueryParameterEncode (
					paramEntry.getValue ()));

		}

		// create connection with the url and the query string

		String urlString =
			stringFormat (
				"%s?%s",
				smsArenaRouteOut.getRelayUrl (),
				paramsString);

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

		Long messageId;
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

				String ack;
				String id;
				//String charge;

				String[] parts = responseString.split(";");

				ack = parts[0];
				id = parts[1];
				//charge = parts[2];

				// check if correct sms id

				if (!id.isEmpty() && messageId == Integer.parseInt(id)) {

					return new PerformSendResult ()

						.status (
							PerformSendStatus.success)

						.otherIds (
							Collections.<String>singletonList (
								(String)
								id))

						.responseTrace (
							new JSONObject (
								responseTrace));

				}

				// TODO don't like this fuzzy matching...

				if (ack.contains ("ERROR")) {

					return new PerformSendResult ()

						.status (
							PerformSendStatus.remoteError)

						.message (
							stringFormat (
								"Error from remote system: %s",
								httpUrlConnection.getResponseCode (),
								ack))

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
