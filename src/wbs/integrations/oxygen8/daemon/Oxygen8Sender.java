package wbs.integrations.oxygen8.daemon;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.joinWithSemicolonAndSpace;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatObsolete;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.apache.commons.io.IOUtils;

import org.json.simple.JSONObject;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.object.ObjectManager;

import wbs.integrations.oxygen8.model.Oxygen8NetworkObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8NetworkRec;
import wbs.integrations.oxygen8.model.Oxygen8RouteOutObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8RouteOutRec;

import wbs.sms.gsm.GsmUtils;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender2;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteRec;

import wbs.web.utils.HtmlUtils;

@Log4j
@SingletonComponent ("oxygen8Sender")
public
class Oxygen8Sender
	extends AbstractSmsSender2 {

	// singleton dependencies

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	Oxygen8NetworkObjectHelper oxygen8NetworkHelper;

	@SingletonDependency
	Oxygen8RouteOutObjectHelper oxygen8RouteOutHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// details

	@Override
	protected
	String getThreadName () {
		return "Ox8Snd";
	}

	@Override
	public
	String senderCode () {
		return "oxygen8";
	}

	// implementation

	@Override
	protected
	SetupSendResult setupSend (
			OutboxRec outbox) {

		// get stuff

		MessageRec message =
			outbox.getMessage ();

		RouteRec route =
			message.getRoute ();

		// lookup route out

		Optional<Oxygen8RouteOutRec> oxygen8RouteOutOptional =
			oxygen8RouteOutHelper.find (
				route.getId ());

		if (
			optionalIsNotPresent (
				oxygen8RouteOutOptional)
		) {

			return new SetupSendResult ()

				.status (
					SetupSendStatus.configError)

				.message (
					stringFormat (
						"Oxygen8 outbound route not found for %s",
						route.getCode ()));

		}

		Oxygen8RouteOutRec oxygen8RouteOut =
			oxygen8RouteOutOptional.get ();

		// lookup network

		Oxygen8NetworkRec oxygen8Network =
			oxygen8NetworkHelper.find (
				oxygen8RouteOut.getOxygen8Config (),
				message.getNumber ().getNetwork ());

		if (oxygen8Network == null) {

			return new SetupSendResult ()

				.status (
					SetupSendStatus.configError)

				.message (
					stringFormatObsolete (
						"Oxygen8 network not found for %s",
						message.getNumber ().getNetwork ().getId ()));

		}

		// validate message text

		if (
			! GsmUtils.gsmStringIsValid (
				message.getText ().getText ())
		) {

			return new SetupSendResult ()

				.status (
					SetupSendStatus.validationError)

				.message (
					"The message text contains non-GSM characters");

		}

		long gsmLength =
			GsmUtils.gsmStringLength (
				message.getText ().getText ());

		boolean needMultipart =
			gsmLength > 160;

		boolean allowMultipart =
			oxygen8RouteOut.getMultipart ();

		if (
			needMultipart
			&& ! allowMultipart
		) {

			return new SetupSendResult ()

				.status (
					SetupSendStatus.validationError)

				.message (
					stringFormatObsolete (
						"Length is %s but multipart not enabled",
						gsmLength));

		}

		// load lazy stuff

		message.getText ().getText ();
		message.getTags ().size ();
		route.getCode ();

		String servicePath =
			objectManager.objectPathMini (
				message.getService ());

		// pick a handler

		if (
			stringEqualSafe (
				message.getMessageType ().getCode (),
				"sms")
		) {

			// nothing to do

		} else {

			return new SetupSendResult ()

				.status (
					SetupSendStatus.unknownError)

				.message (
					stringFormat (
						"Don't know what to do with a %s",
						message.getMessageType ().getCode ()));

		}

		// create trace

		Map<String,Object> requestTrace =
			ImmutableMap.<String,Object>builder ()

			.put (
				"url",
				oxygen8RouteOut.getRelayUrl ())

			.put (
				"method",
				"GET")

			.build ();

		// return

		PerformSendCallable performSendCallable =
			new PerformSendCallable ()

			.outbox (
				outbox)

			.message (
				message)

			.route (
				route)

			.oxygen8RouteOut (
				oxygen8RouteOut)

			.oxygen8Network (
				oxygen8Network)

			.servicePath (
				servicePath)

			.needMultipart (
				needMultipart);

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

		OutboxRec outbox;
		MessageRec message;
		RouteRec route;
		Oxygen8RouteOutRec oxygen8RouteOut;
		Oxygen8NetworkRec oxygen8Network;
		String servicePath;
		Boolean needMultipart;

		HttpURLConnection urlConnection;

		@Override
		public
		PerformSendResult call ()
			throws IOException {

			log.info (
				stringFormatObsolete (
					"Sending message %s",
					message.getId ()));

			openConnection ();

			Optional<PerformSendResult> sendRequestResult =
				sendRequest ();

			if (sendRequestResult.isPresent ()) {
				return sendRequestResult.get ();
			}

			return readResponse ();

		}

		void openConnection ()
			throws IOException {

			// create connection

			String urlString =
				oxygen8RouteOut.getRelayUrl ();

			URL url =
				new URL (
					urlString);

			urlConnection =
				(HttpURLConnection)
				url.openConnection ();

			// set basic params

			urlConnection.setDoOutput (true);
			urlConnection.setDoInput (true);
			urlConnection.setAllowUserInteraction (false);
			urlConnection.setRequestMethod ("POST");

			urlConnection.setConnectTimeout (
				toJavaIntegerRequired (
					oxygen8RouteOut.getConnectTimeout () * 1000));

			urlConnection.setReadTimeout (
				toJavaIntegerRequired (
					oxygen8RouteOut.getReadTimeout () * 1000));

			// set request params

			urlConnection.setRequestProperty (
				"Content-Type",
				joinWithSemicolonAndSpace (
					"application/x-www-form-urlencoded",
					"charset=UTF-8"));

			urlConnection.setRequestProperty (
				"User-Agent",
				wbsConfig.httpUserAgent ());

		}

		Optional<PerformSendResult> sendRequest ()
			throws IOException {

			Map<String,String> params =
				new LinkedHashMap<String,String> ();

			params.put (
				"Reference",
				message.getId ().toString ());

			if (
				isNotNull (
					oxygen8RouteOut.getCampaignId ())
			) {

				params.put (
					"CampaignID",
					oxygen8RouteOut.getCampaignId ());

			}

			params.put (
				"Username",
				oxygen8RouteOut.getUsername ());

			params.put (
				"Password",
				oxygen8RouteOut.getPassword ());

			// set multipart

			params.put (
				"Multipart",
				needMultipart
					? "1"
					: "0");

			// set shortcode and channel

			if (oxygen8RouteOut.getPremium ()) {

				params.put (
					"Shortcode",
					oxygen8RouteOut.getShortcode ());

				params.put (
					"Channel",
					oxygen8Network.getChannel ());

			} else {

				params.put (
					"Mask",
					message.getNumFrom ());

				params.put (
					"Channel",
					"BULK");

			}

			params.put (
				"MSISDN",
				message.getNumTo ());

			params.put (
				"Content",
				message.getText ().getText ());

			params.put (
				"Premium",
				route.getOutCharge () > 0
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
					HtmlUtils.urlQueryParameterEncode (
						paramEntry.getValue ()));

			}

			OutputStream out =
				urlConnection.getOutputStream ();

			IOUtils.write (
				paramsString.toString (),
				out);

			return Optional.<PerformSendResult>absent ();

		}

		public
		PerformSendResult readResponse ()
			throws IOException {

			String responseString =
				IOUtils.toString (
					urlConnection.getInputStream ());

			log.debug (
				stringFormatObsolete (
					"Message %s code %s response: [%s]",
					message.getId (),
					urlConnection.getResponseCode (),
					responseString));

			Map<String,Object> responseTrace =
				ImmutableMap.<String,Object>builder ()

					.put (
						"status",
						urlConnection.getResponseCode ())

					.put (
						"message",
						urlConnection.getResponseMessage ())

					.put (
						"body",
						responseString)

					.build ();

			if (urlConnection.getResponseCode () == 200) {

				String responseLines[] =
					responseString.split ("\n");

				if (responseLines.length != 3) {

					return new PerformSendResult ()

						.status (
							PerformSendStatus.unknownError)

						.message (
							stringFormat (
								"Invalid response: %s",
								responseString))

						.responseTrace (
							new JSONObject (
								responseTrace));

				} else if (
					stringEqualSafe (
						responseLines [0],
						"101")
				) {

					List<String> otherIds =
						ImmutableList.<String>copyOf (
							responseLines [2].split (","));

					return new PerformSendResult ()

						.status (
							PerformSendStatus.success)

						.otherIds (
							otherIds)

						.responseTrace (
							new JSONObject (
								responseTrace));

				} else {

					return new PerformSendResult ()

						.status (
							PerformSendStatus.remoteError)

						.message (
							stringFormat (
								"Error %s: %s",
								responseLines [0],
								responseLines [1]))

						.responseTrace (
							new JSONObject (
								responseTrace));

				}

			}

			return new PerformSendResult ()

				.status (
					PerformSendStatus.unknownError)

				.message (
					stringFormatObsolete (
						"Server returned %s",
						urlConnection.getResponseCode ()))

				.responseTrace (
					new JSONObject (
						responseTrace));

		}

	}

}
