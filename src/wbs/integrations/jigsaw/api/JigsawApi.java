package wbs.integrations.jigsaw.api;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.apache.commons.io.IOUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Log4j
public
class JigsawApi {

	public
	String getUrl () {
		return url;
	}

	public
	void setUrl (
			String url) {

		this.url = url;

	}

	String url;

	@Accessors (fluent = true)
	@Data
	public static
	class PushRequest {

		String applicationIdentifier;

		List<String> tokens =
			new ArrayList<String> ();

		public
		PushRequest addToken (
				String token) {

			tokens.add (token);

			return this;

		}

		Integer messageBadge;
		String messageSound;
		String messageBody;

		Map<String,Object> messageCustomProperties =
			new LinkedHashMap<String,Object> ();;

		public
		PushRequest addMessageCustomProperty (
				String name,
				Object value) {

			messageCustomProperties.put (
				name,
				value);

			return this;

		}

	}

	@SuppressWarnings ("unchecked")
	public
	void pushServer (
			PushRequest request) {

		try {

			log.debug ("Preparing request to " + url);

			// prepare json
			JSONObject obj = new JSONObject ();
			obj.put ("applicationIdentifier", request.applicationIdentifier);
			JSONArray tokens = new JSONArray ();
			for (String token : request.tokens)
				tokens.add (token);
			obj.put ("tokens", tokens);
			JSONObject message = new JSONObject ();
			if (request.messageBadge != null)
				message.put ("badge", request.messageBadge);
			if (request.messageSound != null)
				message.put ("sound", request.messageSound);
			if (request.messageBody != null)
				message.put ("body", request.messageBody);
			if (! request.messageCustomProperties.isEmpty ()) {
				JSONObject customProperties = new JSONObject ();
				customProperties.putAll (request.messageCustomProperties);
				message.put ("customProperties", customProperties);
			}
			obj.put ("message", message);
			Writer stringWriter = new StringWriter ();
			obj.writeJSONString (stringWriter);
			String json = stringWriter.toString ();

			// output json (for debugging)
			if (log.isDebugEnabled ()) {
				log.debug ("Request entity: " + json);
			}

			// make call

			try (

				CloseableHttpClient httpClient =
					HttpClientBuilder.create ()
						.build ();

			) {

				HttpPost post =
					new HttpPost (url);

				StringEntity postEntity =
					new StringEntity (json, "utf-8");

				postEntity.setContentType ("application/json");

				post.setEntity (postEntity);

				HttpResponse response =
					httpClient.execute (post);

				// output response (for debugging)
				if (log.isDebugEnabled ()) {
					HttpEntity responseEntity = response.getEntity ();
					byte[] responseBytes = IOUtils.toByteArray (responseEntity.getContent ());
					log.debug ("Response entity " + new String (responseBytes, "utf-8"));
				}

				// check status
				int status = response.getStatusLine ().getStatusCode ();
				if (status != 200) {
					throw new RuntimeException ("Jigsaw API call failed with status code " + status);
				}

				for (String token : request.tokens) {
					log.info ("Jigsaw push notification sent to " + request.applicationIdentifier + ": " + token);
				}

			}

		} catch (Exception e) {
			for (String token : request.tokens) {
				log.warn ("Jigsaw push notification failed to " + request.applicationIdentifier + ": " + token);
			}
			throw new RuntimeException ("Unable to contact Jigsaw server", e);
		}
	}

}