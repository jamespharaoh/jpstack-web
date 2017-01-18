package wbs.imchat.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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

@PrototypeComponent ("imChatSessionStartAction")
public
class ImChatSessionStartAction
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

		DataFromJson dataFromJson =
			new DataFromJson ();

		// decode request

		JSONObject jsonValue =
			(JSONObject)
			JSONValue.parse (
				requestContext.reader ());

		ImChatSessionStartRequest startRequest =
			dataFromJson.fromJson (
				ImChatSessionStartRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ImChatSessionStartAction.handle ()",
				this);

		ImChatRec imChat =
			imChatHelper.findRequired (
				parseIntegerRequired (
					requestContext.requestStringRequired (
						"imChatId")));

		// lookup customer

		ImChatCustomerRec customer =
			imChatCustomerHelper.findByEmail (
				imChat,
				startRequest.email ());

		if (customer == null) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"email-invalid")

				.message (
					"No customer with that email address exists");

			return jsonResponderProvider.get ()

				.value (
					failureResponse);

		}

		// verify password

		if (
			stringNotEqualSafe (
				customer.getPassword (),
				startRequest.password ())
		) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"password-invalid")

				.message (
					"The supplied password is not correct");

			return jsonResponderProvider.get ()

				.value (
					failureResponse);

		}

		// create session

		ImChatSessionRec session =
			imChatSessionHelper.insert (
				imChatSessionHelper.createInstance ()

			.setImChatCustomer (
				customer)

			.setSecret (
				randomLogic.generateLowercase (20))

			.setActive (
				true)

			.setStartTime (
				transaction.now ())

			.setUpdateTime (
				transaction.now ())

			.setUserAgentText (
				textHelper.findOrCreateMapNull (
					startRequest.userAgent ()))

			.setIpAddress (
				optionalOrNull (
					requestContext.realIp ()))

		);

		customer

			.setActiveSession (
				session)

			.setLastSession (
				transaction.now ());

		// create response

		ImChatSessionStartSuccess successResponse =
			new ImChatSessionStartSuccess ()

			.sessionSecret (
				session.getSecret ())

			.customer (
				imChatApiLogic.customerData (
					customer))

			.conversation (
				customer.getCurrentConversation () != null
					? imChatApiLogic.conversationData (
						customer.getCurrentConversation ())
					: null);

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()

			.value (
				successResponse);

	}

}
