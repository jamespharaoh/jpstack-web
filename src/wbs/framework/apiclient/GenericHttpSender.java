package wbs.framework.apiclient;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringToUtf8;
import static wbs.utils.string.StringUtils.utf8ToString;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.compress.utils.IOUtils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeIoException;

@Accessors (fluent = true)
@PrototypeComponent ("genericHttpSender")
public abstract
class GenericHttpSender <
	SenderType extends GenericHttpSender <
		SenderType,
		RequestType,
		ResponseType,
		HelperType
	>,
	RequestType,
	ResponseType,
	HelperType extends GenericHttpSenderHelper <
		RequestType,
		ResponseType
	>
> {

	// singleton dependencies

	@SingletonDependency
	WbsConfig wbsConfig;

	// properties

	@Setter
	HelperType helper;

	// state

	State state =
		State.init;

	CloseableHttpClient httpClient;

	HttpPost httpPost;

	HttpResponse httpResponse;
	String responseBody;

	@Getter
	JSONObject requestTrace;

	@Getter
	JSONObject responseTrace;

	@Getter
	Optional <String> errorMessage =
		optionalAbsent ();

	// property accessors

	public
	SenderType request (
			@NonNull RequestType request) {

		helper.request (
			request);

		@SuppressWarnings ("unchecked")
		SenderType senderThis =
			(SenderType)
			this;

		return senderThis;

	}

	public
	ResponseType response () {

		return helper.response ();

	}

	// public implementation

	public
	SenderType encode () {

		// check and set temporary state

		if (
			enumNotEqualSafe (
				state,
				State.init)
		) {
			throw new IllegalStateException ();
		}

		// ask helper to verify

		state =
			State.verifyError;

		helper.verify ();

		// delegate to helper

		state =
			State.encodeError;

		helper.encode ();

		// create post

		httpPost =
			new HttpPost (
				helper.url ());

		// convert to binary representation

		byte[] requestData =
			stringToUtf8 (
				helper.requestBody ());

		// set default headers

		httpPost.setHeader (
			"User-Agent",
			wbsConfig.httpUserAgent ());

		// set headers from helper

		for (
			Map.Entry <String, String> requestHeaderEntry
				: helper.requestHeaders ().entrySet ()
		) {

			httpPost.setHeader (
				requestHeaderEntry.getKey (),
				requestHeaderEntry.getValue ());

		}

		// set body

		httpPost.setEntity (
			new ByteArrayEntity (
				requestData));

		// create debug trace

		requestTrace =
			new JSONObject (
				ImmutableMap.<String, Object> builder ()

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
				helper.requestBody ())

			.build ()

		);

		// update state and return

		state =
			State.encoded;

		@SuppressWarnings ("unchecked")
		SenderType senderThis =
			(SenderType)
			this;

		return senderThis;

	}

	public
	SenderType send () {

		// check and set temporary state

		if (
			enumNotEqualSafe (
				state,
				State.encoded)
		) {
			throw new IllegalStateException ();
		}

		state =
			State.sendError;

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

		@SuppressWarnings ("unchecked")
		SenderType senderThis =
			(SenderType)
			this;

		return senderThis;

	}

	public
	GenericHttpSender <
		SenderType,
		RequestType,
		ResponseType,
		HelperType
	> receive () {

		// check and set temporary state

		if (
			enumNotEqualSafe (
				state,
				State.sent)
		) {
			throw new IllegalStateException ();
		}

		state =
			State.receiveError;

		// receive responsea

		try {

			responseBody =
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
				responseBody)

			.build ()

		);

		// update state and return

		state =
			State.received;

		return this;

	}

	public
	GenericHttpSender <
		SenderType,
		RequestType,
		ResponseType,
		HelperType
	> decode (
			@NonNull TaskLogger parentTaskLogger) {

		// check and set temporary state

		if (
			enumNotEqualSafe (
				state,
				State.received)
		) {
			throw new IllegalStateException ();
		}

		state =
			State.receiveError;

		// check response

		if (
			doesNotContain (
				helper.validStatusCodes (),
				fromJavaInteger (
					httpResponse.getStatusLine ().getStatusCode ()))
		) {

			// invalid status code

			errorMessage =
				optionalOf (
					stringFormat (
						"Server returned %s: %s",
						integerToDecimalString (
							httpResponse.getStatusLine ().getStatusCode ()),
						httpResponse.getStatusLine ().getReasonPhrase ()));

		} else {

			// decode response

			helper

				.responseStatusCode (
					fromJavaInteger (
						httpResponse.getStatusLine ().getStatusCode ()))

				.responseStatusReason (
					httpResponse.getStatusLine ().getReasonPhrase ())

				.responseHeaders (
					ImmutableMap.copyOf (
						Arrays.stream (
							httpResponse.getAllHeaders ())

					.collect (
						Collectors.toMap (
							Header::getName,
							Header::getValue))

				))

				.responseBody (
					responseBody)

				.decode ();

		}

		// update state and return

		state =
			State.decoded;

		return this;

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
		verifyError,
		encodeError,
		encoded,
		sendError,
		sent,
		receiveError,
		received,
		decoded,
		error,
		closed;
	}

}
