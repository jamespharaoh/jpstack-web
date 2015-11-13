package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.isNotNull;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.applications.imchat.model.ImChatConversationObjectHelper;
import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerObjectHelper;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatProfileObjectHelper;
import wbs.applications.imchat.model.ImChatProfileRec;
import wbs.applications.imchat.model.ImChatRec;
import wbs.applications.imchat.model.ImChatSessionObjectHelper;
import wbs.applications.imchat.model.ImChatSessionRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

@PrototypeComponent ("imChatConversationStartAction")
public
class ImChatConversationStartAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatApiLogic imChatApiLogic;

	@Inject
	ImChatConversationObjectHelper imChatConversationHelper;

	@Inject
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	ImChatProfileObjectHelper imChatProfileHelper;

	@Inject
	ImChatSessionObjectHelper imChatSessionHelper;

	@Inject
	RequestContext requestContext;

	// prototype dependencies

	@Inject
	Provider<JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	@SneakyThrows (IOException.class)
	public
	Responder handle () {

		DataFromJson dataFromJson =
			new DataFromJson ();

		// decode request

		JSONObject jsonValue =
			(JSONObject)
			JSONValue.parse (
				requestContext.reader ());

		ImChatConversationStartRequest startRequest =
			dataFromJson.fromJson (
				ImChatConversationStartRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ImChatRec imChat =
			imChatHelper.find (
				Integer.parseInt (
					(String)
					requestContext.request (
						"imChatId")));

		// lookup session and customer

		ImChatSessionRec session =
			imChatSessionHelper.findBySecret (
				startRequest.sessionSecret ());

		if (
			session == null
			|| ! session.getActive ()
		) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"session-invalid")

				.message (
					"The session secret is invalid or the session is no " +
					"longer active");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		ImChatCustomerRec customer =
			session.getImChatCustomer ();

		// lookup profile

		ImChatProfileRec profile =
			imChatProfileHelper.findByCode (
				imChat,
				startRequest.profileCode ());

		if (
			profile == null
			|| profile.getDeleted ()
			|| profile.getImChat () != imChat
		) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"profile-invalid")

				.message (
					"The profile id is invalid");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		// check state

		if (
			isNotNull (
				customer.getCurrentConversation ())
		) {

			throw new RuntimeException ();

		}

		// create conversation

		ImChatConversationRec conversation =
			imChatConversationHelper.insert (
				imChatConversationHelper.createInstance ()

			.setImChatCustomer (
				customer)

			.setImChatProfile (
				profile)

			.setIndex (
				customer.getNumConversations ())

			.setStartTime (
				transaction.now ())

		);

		// update customer

		customer

			.setNumConversations (
				customer.getNumConversations () + 1)

			.setCurrentConversation (
				conversation);

		// create response

		ImChatConversationStartSuccess successResponse =
			new ImChatConversationStartSuccess ()

			.customer (
				imChatApiLogic.customerData (
					customer))

			.profile (
				imChatApiLogic.profileData (
					profile))

			.conversation (
				imChatApiLogic.conversationData (
					conversation));

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

}
