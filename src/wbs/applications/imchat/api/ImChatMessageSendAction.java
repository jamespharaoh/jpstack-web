package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.lessThan;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.applications.imchat.model.ImChatConversationObjectHelper;
import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageObjectHelper;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.applications.imchat.model.ImChatObjectHelper;
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
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;

@PrototypeComponent ("imChatMessageSendAction")
public
class ImChatMessageSendAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatApiLogic imChatApiLogic;

	@Inject
	ImChatConversationObjectHelper imChatConversationHelper;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	ImChatMessageObjectHelper imChatMessageHelper;

	@Inject
	ImChatSessionObjectHelper imChatSessionHelper;

	@Inject
	QueueLogic queueLogic;

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

		ImChatMessageSendRequest messageSendRequest =
			dataFromJson.fromJson (
				ImChatMessageSendRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ImChatRec imChat =
			imChatHelper.find (
				Integer.parseInt (
					requestContext.requestStringRequired (
						"imChatId")));

		// lookup session

		ImChatSessionRec session =
			imChatSessionHelper.findBySecret (
				messageSendRequest.sessionSecret ());

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

				.value (
					failureResponse);

		}

		// lookup customer

		ImChatCustomerRec customer =
			session.getImChatCustomer ();

		if (customer.getImChat () != imChat)
			throw new RuntimeException ();

		// lookup converation

		ImChatConversationRec conversation =
			imChatConversationHelper.findByIndex (
				customer,
				messageSendRequest.conversationIndex ());

		// check customer balance

		if (
			lessThan (
				customer.getBalance (),
				imChat.getMessageCost ())
		) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"credit-insufficient")

				.message (
					"The customer's credit balance is not sufficient to " +
					"continue the chat conversation");

			return jsonResponderProvider.get ()

				.value (
					failureResponse);

		}

		// check conversation state

		if (conversation.getPendingReply ()) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"reply-pending")

				.message (
					"This conversation already has a reply pending.");

			return jsonResponderProvider.get ()

				.value (
					failureResponse);

		}

		// create chat message

		ImChatMessageRec message =
			imChatMessageHelper.insert (
				imChatMessageHelper.createInstance ()

			.setImChatConversation (
				conversation)

			.setIndex (
				(int) (long)
				conversation.getNumMessages ())

			.setTimestamp (
				transaction.now ())

			.setMessageText (
				messageSendRequest.messageText ())

		);

		conversation

			.setNumMessages (
				conversation.getNumMessages () + 1)

			.setFreeMessages (
				0l)

			.setPendingReply (
				true);

		// create queue item

		QueueItemRec queueItem =
			queueLogic.createQueueItem (
				queueLogic.findQueue (
					imChat,
					"reply"),
				conversation,
				message,
				customer.getCode (),
				message.getMessageText ());

		// add queue item to message

		message

			.setQueueItem (
				queueItem);

		// update customer

		customer

			.setLastSession (
				transaction.now ());

		// create response

		ImChatMessageSendSuccess successResponse =
			new ImChatMessageSendSuccess ()

			.customer (
				imChatApiLogic.customerData (
					customer))

			.conversation (
				imChatApiLogic.conversationData (
					conversation))

			.message (
				imChatApiLogic.messageData (
					message));

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

}
