package wbs.integrations.digitalselect.daemon;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.integrations.digitalselect.daemon.DigitalSelectSender.State;
import wbs.integrations.digitalselect.model.DigitalSelectRouteOutObjectHelper;
import wbs.integrations.digitalselect.model.DigitalSelectRouteOutRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender1;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@SingletonComponent ("digitalSelectSender")
public
class DigitalSelectSender
	extends AbstractSmsSender1<State> {

	@Inject
	Database database;

	@Inject
	DigitalSelectRouteOutObjectHelper digitalSelectRouteOutHelper;

	@Override
	protected
	String getThreadName () {
		return "DigSelSndr";
	}


	@Override
	protected
	String getSenderCode () {
		return "digital_select";
	}

	@Override
	protected
	State getMessage (
			OutboxRec outbox)
		throws SendFailureException {

		Transaction transaction =
			database.currentTransaction ();

		DigitalSelectRouteOutRec digitalSelectRouteOut =
			digitalSelectRouteOutHelper.findOrThrow (
				outbox.getRoute ().getId (),
				() -> new RuntimeException (
					stringFormat (
						"No digital select route out %s",
						outbox.getRoute ().getId ())));

		State state =
			new State ()

			.outbox (
				outbox)

			.message (
				outbox.getMessage ())

			.route (
				outbox.getRoute ())

			.digitalSelectRouteOut (
				digitalSelectRouteOut);

		transaction.fetch (
			state.outbox (),
			state.message (),
			state.message ().getText (),
			state.route (),
			state.digitalSelectRouteOut ());

		return state;

	}

	@Override
	protected
	Optional<List<String>> sendMessage (
			@NonNull State state)
		throws SendFailureException {

		log.info (
			stringFormat (
				"Sending message %s",
				state.message ().getId ()));

		try {

			openConnection (
				state);

			return readResponse (
				state);

		} catch (IOException exception) {

			throw tempFailure (
				"IO error " + exception.getMessage ());

		} finally {

			try {

				if (state.httpClient () != null)
					state.httpClient ().close ();

			} catch (IOException exception) {

				log.warn (
					"Got IO exception closing http client",
					exception);

			}

		}

	}

	private static
	void openConnection (
			State state)
		throws IOException {

		state.httpClient (
			HttpClientBuilder.create ()
				.build ());

		HttpPost post =
			new HttpPost (
				state.digitalSelectRouteOut ().getUrl ());

		UrlEncodedFormEntity formEntity =
			new UrlEncodedFormEntity (
				ImmutableList.<NameValuePair>of (

					new BasicNameValuePair (
						"username",
						state.digitalSelectRouteOut ().getUsername ()),

					new BasicNameValuePair (
						"password",
						state.digitalSelectRouteOut ().getPassword ()),

					new BasicNameValuePair (
						"receiver",
						"+" + state.message ().getNumTo ()),

					new BasicNameValuePair (
						"content",
						state.message ().getText ().getText ()),

					new BasicNameValuePair (
						"reference",
						Long.toString (
							state.message ().getId ())),

					new BasicNameValuePair (
						"sender",
						internationalNumberPattern
								.matcher (state.message ().getNumFrom ())
								.matches ()
							? "+" + state.message ().getNumFrom ()
							: state.message ().getNumFrom ())),

				"utf-8");

		post.setEntity (
			formEntity);

		log.debug (
			stringFormat (
				"Making request to %s with %s",
				state.digitalSelectRouteOut ().getUrl (),
				IOUtils.toString (
					formEntity.getContent ())));

		state.httpResponse (
			state
				.httpClient ()
				.execute (post));

	}

	final static
	Pattern internationalNumberPattern =
		Pattern.compile (
			"^44[0-9]{10}$");

	public
	Optional<List<String>> readResponse (
			@NonNull State state)
		throws
			IOException,
			SendFailureException {

		String responseString =
			IOUtils.toString (
				state.httpResponse ().getEntity ().getContent ());

		log.debug (
			stringFormat (
				"Message %s response: %s",
				state.message ().getId (),
				responseString));

		StatusLine statusLine =
			state.httpResponse ().getStatusLine ();

		if (statusLine.getStatusCode () != 200) {

			String errorMessage =
				stringFormat (
					"Got error %s from remote system: %s",
					statusLine.getStatusCode (),
					responseString);

			log.error (
				errorMessage);

			throw tempFailure (
				errorMessage);

		}

		Matcher matcher =
			responsePattern.matcher (
				responseString);

		if (! matcher.matches ()) {

			String errorMessage =
				stringFormat (
					"Got invalid response from remote system: %s",
					responseString);

			log.error (
				errorMessage);

			throw tempFailure (
				errorMessage);

		}

		String otherId =
			matcher.group (1);

		return Optional.of (
			ImmutableList.of (
				otherId));

	}

	static final
	Pattern responsePattern =
		Pattern.compile (
			"OK MessageID\\(s\\): (.+)");

	@Accessors (fluent = true)
	@Data
	static
	class State {

		OutboxRec outbox;
		MessageRec message;
		RouteRec route;
		DigitalSelectRouteOutRec digitalSelectRouteOut;

		CloseableHttpClient httpClient;
		HttpResponse httpResponse;

	}

}
