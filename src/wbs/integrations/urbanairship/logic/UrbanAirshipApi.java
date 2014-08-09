package wbs.integrations.urbanairship.logic;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import wbs.framework.application.annotations.SingletonComponent;

@Log4j
@SingletonComponent ("urbanAirshipApi")
public
class UrbanAirshipApi {

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
			String accountKey,
			String typeKey,
			String path,
			JSONObject request) {

		try {

			String url =
				baseUrl + "/" + path + "/";

			log.debug ("Preparing request to " + url);

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

			@Cleanup CloseableHttpClient httpClient =
				HttpClientBuilder.create ()
					.build ();

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
				new StringEntity (json, "utf-8");

			postEntity.setContentType ("application/json");

			post.setEntity (postEntity);

			// output request for debugging

			if (log.isDebugEnabled ()) {

				log.debug ("Request entity: " + json);

				for (Header header : post.getAllHeaders ())
					log.debug ("Request header " + header.getName () + ": " + header.getValue ());

			}

			HttpResponse response =
				httpClient.execute (post);

			// output response (for debugging)

			if (log.isDebugEnabled ()) {

				HttpEntity responseEntity =
					response.getEntity ();

				byte[] responseBytes =
					IOUtils.toByteArray (responseEntity.getContent ());

				log.debug ("Response entity " + new String (responseBytes, "utf-8"));

				for (Header header : response.getAllHeaders ())
					log.debug ("Response header " + header.getName () + ": " + header.getValue ());

			}

			// check status

			int status =
				response.getStatusLine ().getStatusCode ();

			if (status != 200) {

				log.debug ("Urban Airship API call failed with status code " + status + ": " +
					response.getStatusLine ().getReasonPhrase ());

				throw new RuntimeException ("Urban Airship API call failed with status code " + status);

			}

		} catch (Exception e) {
			throw new RuntimeException ("Unable to contact Urban Airship server", e);
		}

	}

	@SuppressWarnings ("unchecked")
	public
	void push (
			String accountKey,
			String typeKey,
			PushRequest request) {

		try {

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

			post (
				accountKey,
				typeKey,
				"push",
				obj);

			for (String token : request.tokens) {

				log.info (
					stringFormat (
						"Urban Airship push notification sent to %s",
						token));

			}

		} catch (Exception exception) {

			for (String token : request.tokens) {

				log.warn (
					stringFormat (
						"Urban Airship push notification failed to %s",
						token),
					exception);

			}

			throw new RuntimeException (
				"Unable to contact Urban airship server",
				exception);

		}

	}

}