package wbs.imchat.api;

import static wbs.utils.collection.MapUtils.mapIsNotEmpty;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Provider;

import lombok.NonNull;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.text.model.TextObjectHelper;

import wbs.utils.random.RandomLogic;

import wbs.imchat.model.ImChatCustomerObjectHelper;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.Responder;

@PrototypeComponent ("imChatCustomerCreateAction")
public
class ImChatCustomerCreateAction
	implements Action {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	ImChatSessionObjectHelper imChatSessionHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handle");

		DataFromJson dataFromJson =
			new DataFromJson ();

		// decode request

		JSONObject jsonValue =
			(JSONObject)
			JSONValue.parse (
				requestContext.reader ());

		ImChatCustomerCreateRequest createRequest =
			dataFromJson.fromJson (
				ImChatCustomerCreateRequest.class,
				jsonValue);

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ImChatCustomerCreateAction.handle ()",
					this);

		) {

			ImChatRec imChat =
				imChatHelper.findRequired (
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"imChatId")));

			// check for existing

			ImChatCustomerRec existingCustomer =
				imChatCustomerHelper.findByEmail (
					imChat,
					createRequest.email ());

			if (existingCustomer != null) {

				ImChatFailure failureResponse =
					new ImChatFailure ()

					.reason (
						"email-already-exists")

					.message (
						"A customer with that email address already exists");

				return jsonResponderProvider.get ()
					.value (failureResponse);

			}

			// check email looks ok

			Matcher emailMatcher =
				emailPattern.matcher (
					createRequest.email ());

			if (! emailMatcher.matches ()) {

				ImChatFailure failureResponse =
					new ImChatFailure ()

					.reason (
						"email-invalid")

					.message (
						"Please enter a valid email address");

				return jsonResponderProvider.get ()
					.value (failureResponse);

			}

			// check password looks ok

			if (createRequest.password ().length () < 6) {

				ImChatFailure failureResponse =
					new ImChatFailure ()

					.reason (
						"password-invalid")

					.message (
						"Please enter a longer password");

				return jsonResponderProvider.get ()
					.value (failureResponse);

			}

			// create new

			ImChatCustomerRec newCustomer =
				imChatCustomerHelper.insert (
					taskLogger,
					imChatCustomerHelper.createInstance ()

				.setImChat (
					imChat)

				.setCode (
					randomLogic.generateNumericNoZero (8))

				.setEmail (
					createRequest.email ())

				.setPassword (
					createRequest.password ())

				.setFirstSession (
					transaction.now ())

				.setLastSession (
					transaction.now ())

			);

			// update details

			Map <String, String> detailErrors =
				imChatApiLogic.updateCustomerDetails (
					taskLogger,
					newCustomer,
					createRequest.details ());

			if (
				mapIsNotEmpty (
					detailErrors)
			) {

				ImChatFailure failureResponse =
					new ImChatFailure ()

					.reason (
						"details-invalid")

					.message (
						"One or more of the details provided are invalid")

					.details (
						detailErrors);

				return jsonResponderProvider.get ()

					.value (
						failureResponse);

			}

			// create session

			ImChatSessionRec session =
				imChatSessionHelper.insert (
					taskLogger,
					imChatSessionHelper.createInstance ()

				.setImChatCustomer (
					newCustomer)

				.setSecret (
					randomLogic.generateLowercase (20))

				.setActive (
					true)

				.setStartTime (
					transaction.now ())

				.setUpdateTime (
					transaction.now ())

				.setUserAgentText (
					optionalOrNull (
						textHelper.findOrCreate (
							taskLogger,
							optionalFromNullable (
								createRequest.userAgent ()))))

				.setIpAddress (
					optionalOrNull (
						requestContext.realIp ()))

			);

			newCustomer

				.setActiveSession (
					session);

			// create response

			ImChatCustomerCreateSuccess successResponse =
				new ImChatCustomerCreateSuccess ()

				.sessionSecret (
					session.getSecret ())

				.customer (
					imChatApiLogic.customerData (
						newCustomer));

			// commit and return

			transaction.commit ();

			return jsonResponderProvider.get ()
				.value (successResponse);

		}

	}

	final static
	Pattern emailPattern =
		Pattern.compile (
			"[^@]+@[^@]+");

}
