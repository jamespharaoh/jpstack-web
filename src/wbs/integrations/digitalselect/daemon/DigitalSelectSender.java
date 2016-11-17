package wbs.integrations.digitalselect.daemon;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.commons.io.IOUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.digitalselect.daemon.DigitalSelectSender.State;
import wbs.integrations.digitalselect.model.DigitalSelectRouteOutObjectHelper;
import wbs.integrations.digitalselect.model.DigitalSelectRouteOutRec;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender1;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("digitalSelectSender")
public
class DigitalSelectSender
	extends AbstractSmsSender1 <State> {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DigitalSelectRouteOutObjectHelper digitalSelectRouteOutHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// details

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

	// implementation

	@Override
	protected
	State getMessage (
			OutboxRec outbox)
		throws SendFailureException {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"getMessage");

		Transaction transaction =
			database.currentTransaction ();

		DigitalSelectRouteOutRec digitalSelectRouteOut =
			digitalSelectRouteOutHelper.findOrThrow (
				outbox.getRoute ().getId (),
				() -> new RuntimeException (
					stringFormat (
						"No digital select route out %s",
						integerToDecimalString (
							outbox.getRoute ().getId ()))));

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
	Optional <List <String>> sendMessage (
			@NonNull State state)
		throws SendFailureException {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"sendMessage");

		taskLogger.noticeFormat (
			"Sending message %s",
			integerToDecimalString (
				state.message ().getId ()));

		try {

			openConnection (
				taskLogger,
				state);

			return readResponse (
				taskLogger,
				state);

		} catch (IOException exception) {

			throw tempFailure (
				"IO error " + exception.getMessage ());

		} finally {

			try {

				if (state.httpClient () != null)
					state.httpClient ().close ();

			} catch (IOException exception) {

				taskLogger.warningFormatException (
					exception,
					"Got IO exception closing http client");

			}

		}

	}

	private static
	void openConnection (
			@NonNull TaskLogger taskLogger,
			@NonNull State state)
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

		taskLogger.debugFormat (
			"Making request to %s with %s",
			state.digitalSelectRouteOut ().getUrl (),
			IOUtils.toString (
				formEntity.getContent ()));

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull State state)
		throws
			IOException,
			SendFailureException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"readResponse");

		String responseString =
			IOUtils.toString (
				state.httpResponse ().getEntity ().getContent ());

		taskLogger.debugFormat (
			"Message %s response: %s",
			integerToDecimalString (
				state.message ().getId ()),
			responseString);

		StatusLine statusLine =
			state.httpResponse ().getStatusLine ();

		if (statusLine.getStatusCode () != 200) {

			String errorMessage =
				stringFormat (
					"Got error %s from remote system: %s",
					integerToDecimalString (
						statusLine.getStatusCode ()),
					responseString);

			taskLogger.errorFormat (
				"%s",
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

			taskLogger.errorFormat (
				"%s",
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
