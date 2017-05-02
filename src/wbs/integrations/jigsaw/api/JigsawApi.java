package wbs.integrations.jigsaw.api;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.utf8ToString;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.commons.io.IOUtils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeIoException;

@PrototypeComponent ("jigsawApi")
public
class JigsawApi {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PushRequest request) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"pushServer");

		) {

			taskLogger.debugFormat (
				"Preparing request to %s",
				url);

			// prepare json

			JSONObject obj =
				new JSONObject ();

			obj.put (
				"applicationIdentifier",
				request.applicationIdentifier);

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

			Writer stringWriter =
				new StringWriter ();

			try {

				obj.writeJSONString (
					stringWriter);

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

			String json = stringWriter.toString ();

			// output json (for debugging)

			if (taskLogger.debugEnabled ()) {

				taskLogger.debugFormat (
					"Request entity: %s",
					json);

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
					new StringEntity (
						json,
						"utf-8");

				postEntity.setContentType (
					"application/json");

				post.setEntity (
					postEntity);

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

					}

					// check status

					int status =
						response.getStatusLine ().getStatusCode ();

					if (status != 200) {

						throw new RuntimeException (
							stringFormat (
								"Jigsaw API call failed with status code %s",
								integerToDecimalString (
									status)));

					}

					for (
						String token
							: request.tokens
					) {

						taskLogger.noticeFormat (
							"Jigsaw push notification sent to %s: %s",
							request.applicationIdentifier,
							token);

					}

				}

			} catch (Exception e) {

				for (
					String token
						: request.tokens
				) {

					taskLogger.warningFormat (
						"Jigsaw push notification failed to %s: %s",
						request.applicationIdentifier,
						token);

				}

				throw new RuntimeException (
					"Unable to contact Jigsaw server",
					e);

			}

		}

	}

}