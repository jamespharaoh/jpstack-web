package wbs.integrations.clockworksms.foreignapi;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.notEqual;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataToXml;
import wbs.framework.utils.etc.RuntimeIoException;

@Accessors (fluent = true)
@PrototypeComponent ("clockworkSmsMessageSender")
public
class ClockworkSmsMessageSender {

	// dependencies

	@Inject @Named
	DataFromXml clockworkSmsForeignApiDataFromXml;

	@Inject
	WbsConfig wbsConfig;

	// properties

	@Getter @Setter
	String url;

	@Getter @Setter
	String key;

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
			notEqual (
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

System.out.println (xmlRequest);

			httpPost =
				new HttpPost (
					url);

			httpPost.setHeader (
				"User-Agent",
				wbsConfig.httpUserAgent ());

			httpPost.setEntity (
				new StringEntity (
					xmlRequest,
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
			notEqual (
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
			notEqual (
				state,
				State.sent)
		) {
			throw new IllegalStateException ();
		}

		try {

			// check response

			if (
				notEqual (
					httpResponse.getStatusLine ().getStatusCode (),
					200)
			) {

				throw new RuntimeException ();

			}

			try {

				xmlResponse =
					IOUtils.toString (
						httpResponse.getEntity ().getContent ());

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

System.out.println (xmlResponse);

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
			notEqual (
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
					IOUtils.toInputStream (
						xmlResponse),
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
			equal (
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
