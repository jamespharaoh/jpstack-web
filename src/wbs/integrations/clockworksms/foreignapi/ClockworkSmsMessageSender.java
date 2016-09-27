package wbs.integrations.clockworksms.foreignapi;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.utils.string.StringUtils.stringToUtf8;
import static wbs.utils.string.StringUtils.utf8ToString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.inject.Named;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.io.IOUtils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataToXml;
import wbs.utils.io.RuntimeIoException;

@Accessors (fluent = true)
@PrototypeComponent ("clockworkSmsMessageSender")
public
class ClockworkSmsMessageSender {

	// singleton dependencies

	@SingletonDependency
	@Named
	DataFromXml clockworkSmsForeignApiDataFromXml;

	@SingletonDependency
	WbsConfig wbsConfig;

	// properties

	@Getter @Setter
	String url;

	@Getter @Setter
	Boolean simulateMultipart;

	@Getter @Setter
	ClockworkSmsMessageRequest request;

	// state

	State state =
		State.init;

	@Getter
	String xmlRequest;

	@Getter
	String xmlResponse;

	@Getter
	ClockworkSmsMessageResponse clockworkResponse;

	@Getter
	JSONObject requestTrace;

	@Getter
	JSONObject responseTrace;

	CloseableHttpClient httpClient;
	HttpPost httpPost;
	HttpResponse httpResponse;

	// public implementation

	public
	ClockworkSmsMessageSender encode () {

		// check state

		if (
			enumNotEqualSafe (
				state,
				State.init)
		) {
			throw new IllegalStateException ();
		}

		try {

			// encode xml

			DataToXml dataToXml =
				new DataToXml ();

			xmlRequest =
				dataToXml.writeToString (
					request);

			httpPost =
				new HttpPost (
					url);

			httpPost.setHeader (
				"User-Agent",
				wbsConfig.httpUserAgent ());

			httpPost.setHeader (
				"Content-Type",
				"application/xml; charset=utf-8");

			httpPost.setEntity (
				new InputStreamEntity (
					new ByteArrayInputStream (
						stringToUtf8 (
							xmlRequest)),
					ContentType.APPLICATION_XML));

			requestTrace =
				new JSONObject (
					ImmutableMap.<String,Object>builder ()

				.put (
					"url",
					httpPost.getURI ().toString ())

				.put (
					"method",
					httpPost.getMethod ())

				.put (
					"headers",
					Arrays.asList (
						httpPost.getAllHeaders ()
					).stream ().collect (
						Collectors.toMap (
							Header::getName,
							Header::getValue)))

				.put (
					"body",
					xmlRequest)

				.build ()

			);

			state =
				State.encoded;

			return this;

		} catch (RuntimeException exception) {

			state =
				State.error;

			throw exception;

		}

	}

	public
	ClockworkSmsMessageSender send () {

		// check state

		if (
			enumNotEqualSafe (
				state,
				State.encoded)
		) {
			throw new IllegalStateException ();
		}

		try {

			// perform api call

			httpClient =
				HttpClientBuilder.create ()
					.build ();

			try {

				httpResponse =
					httpClient.execute (
						httpPost);

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

			state =
				State.sent;

			return this;

		} catch (RuntimeException exception) {

			state =
				State.error;

			throw exception;

		}

	}

	public
	ClockworkSmsMessageSender receive () {

		// check state

		if (
			enumNotEqualSafe (
				state,
				State.sent)
		) {
			throw new IllegalStateException ();
		}

		try {

			// receive responsea

			try {

				xmlResponse =
					utf8ToString (
						IOUtils.toByteArray (
							httpResponse.getEntity ().getContent ()));

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

			// store raw response

			responseTrace =
				new JSONObject (
					ImmutableMap.<String, Object> builder ()

				.put (
					"statusCode",
					httpResponse.getStatusLine ().getStatusCode ())

				.put (
					"statusMessage",
					httpResponse.getStatusLine ().getReasonPhrase ())

				.put (
					"headers",
					Arrays.asList (
						httpResponse.getAllHeaders ()
					).stream ().collect (
						Collectors.toMap (
							Header::getName,
							Header::getValue)))

				.put (
					"body",
					xmlResponse)

				.build ()

			);

			// check response

			if (
				integerNotEqualSafe (
					httpResponse.getStatusLine ().getStatusCode (),
					200l)
			) {

				throw new RuntimeException ();

			}

			state =
				State.received;

			return this;

		} catch (RuntimeException exception) {

			state =
				State.error;

			throw exception;

		}

	}

	public
	ClockworkSmsMessageSender decode () {

		// check state

		if (
			enumNotEqualSafe (
				state,
				State.received)
		) {
			throw new IllegalStateException ();
		}

		try {

			// decode response

			clockworkResponse =
				(ClockworkSmsMessageResponse)
				clockworkSmsForeignApiDataFromXml.readInputStream (
					new ByteArrayInputStream (
						stringToUtf8 (
							xmlResponse)),
					url,
					ImmutableList.of ());

			state =
				State.decoded;

			return this;

		} catch (RuntimeException exception) {

			state =
				State.error;

			throw exception;

		}

	}

	public
	void close () {

		if (
			enumEqualSafe (
				state,
				State.closed)
		) {
			return;
		}

		if (
			isNotNull (
				httpClient)
		) {

			try {

				httpClient.close ();

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

		}

		state =
			State.closed;

	}

	// inner classes

	public static
	enum State {
		init,
		encoded,
		sent,
		received,
		decoded,
		error,
		closed;
	}

}
