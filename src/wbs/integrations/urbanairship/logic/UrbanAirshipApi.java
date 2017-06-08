package wbs.integrations.urbanairship.logic;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.utf8ToString;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.apache.commons.io.IOUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("urbanAirshipApi")
public
class UrbanAirshipApi {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	String baseUrl;

	@Getter @Setter
	Map<String,String> accounts;

	public static
	class PushRequest {

		public
		List<String> tokens =
			new ArrayList<String> ();

		public
		Integer apsBadge;

		public
		String apsSound;

		public
		String apsAlert;

		public
		Map<String,Object> customProperties =
			new LinkedHashMap<String,Object> ();;

	}

	public
	void post (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String accountKey,
			@NonNull String typeKey,
			@NonNull String path,
			@NonNull JSONObject request) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"post");

		) {

			String url =
				baseUrl + "/" + path + "/";

			taskLogger.debugFormat (
				"Preparing request to %s",
				url);

			// encode json

			Writer stringWriter =
				new StringWriter ();

			request.writeJSONString (stringWriter);

			String json =
				stringWriter.toString ();

			// lookup account

			String accountValue =
				accounts.get (accountKey + "/" + typeKey);

			if (accountValue == null)
				accountValue =
					accounts.get ("default/" + typeKey);

			if (accountValue == null) {

				throw new RuntimeException (
					stringFormat (
						"No account with name %s and type %s",
						accountKey,
						typeKey));

			}

			String[] accountValueSplit =
				accountValue.split (":");

			String username =
				accountValueSplit [0];

			String password =
				accountValueSplit [1];

			// make call

			try (

				CloseableHttpClient httpClient =
					HttpClientBuilder.create ()
						.build ();

			) {

				Credentials credentials =
					new UsernamePasswordCredentials (
						username,
						password);

				HttpPost post =
					new HttpPost (url);

				HttpContext httpContext =
					new HttpClientContext ();

				post.addHeader (
					new BasicScheme ().authenticate (
						credentials,
						post,
						httpContext));

				StringEntity postEntity =
					new StringEntity (
						json,
						"utf-8");

				postEntity.setContentType (
					"application/json");

				post.setEntity (
					postEntity);

				// output request for debugging

				if (taskLogger.debugEnabled ()) {

					taskLogger.debugFormat (
						"Request entity: %s",
						json);

					for (
						Header header
							: post.getAllHeaders ()
					) {

						taskLogger.debugFormat (
							"Request header %s: %s",
							header.getName (),
							header.getValue ());

					}

				}

				try (

					CloseableHttpResponse response =
						httpClient.execute (
							post);

				) {

					// output response (for debugging)

					if (taskLogger.debugEnabled ()) {

						HttpEntity responseEntity =
							response.getEntity ();

						byte[] responseBytes =
							IOUtils.toByteArray (
								responseEntity.getContent ());

						taskLogger.debugFormat (
							"Response entity %s",
							utf8ToString (
								responseBytes));

						for (
							Header header
								: response.getAllHeaders ()
						) {

							taskLogger.debugFormat (
								"Response header %s: %s",
								header.getName (),
								header.getValue ());

						}

					}

					// check status

					int status =
						response.getStatusLine ().getStatusCode ();

					if (status != 200) {

						taskLogger.debugFormat (
							"Urban Airship API call failed with status code %s: %s",
							integerToDecimalString (
								status),
							response.getStatusLine ().getReasonPhrase ());

						throw new RuntimeException (
							stringFormat (
								"Urban Airship API call failed with status code %s",
								integerToDecimalString (
									status)));

					}

				}

			}

		} catch (Exception exception) {

			throw new RuntimeException (
				"Unable to contact Urban Airship server",
				exception);

		}

	}

	@SuppressWarnings ("unchecked")
	public
	void push (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String accountKey,
			@NonNull String typeKey,
			@NonNull PushRequest request) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"push");

		) {

			// prepare json

			JSONObject obj =
				new JSONObject ();

			JSONArray deviceTokens = new JSONArray ();

			for (String token : request.tokens)
				deviceTokens.add (token);

			obj.put ("device_tokens", deviceTokens);

			JSONObject aps =
				new JSONObject ();

			if (request.apsBadge != null)
				aps.put ("badge", request.apsBadge);

			if (request.apsSound != null)
				aps.put ("sound", request.apsSound);

			if (request.apsAlert != null)
				aps.put ("alert", request.apsAlert);

			obj.put ("aps", aps);

			if (! request.customProperties.isEmpty ()) {
				JSONObject customProperties = new JSONObject ();
				customProperties.putAll (request.customProperties);
				obj.put ("customProperties", customProperties);
			}

			// make call

			try {

				post (
					taskLogger,
					accountKey,
					typeKey,
					"push",
					obj);

				for (
					String token
						: request.tokens
				) {

					taskLogger.noticeFormat (
						"Urban Airship push notification sent to %s",
						token);

				}

			} catch (Exception exception) {

				for (
					String token
						: request.tokens
				) {

					taskLogger.warningFormatException (
						exception,
						"Urban Airship push notification failed to %s",
						token);

				}

				throw new RuntimeException (
					"Unable to contact Urban airship server",
					exception);

			}

		}

	}

}