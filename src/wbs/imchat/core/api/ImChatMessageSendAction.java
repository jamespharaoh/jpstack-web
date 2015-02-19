package wbs.imchat.core.api;

import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.imchat.core.model.ImChatConversationObjectHelper;
import wbs.imchat.core.model.ImChatConversationRec;
import wbs.imchat.core.model.ImChatCustomerRec;
import wbs.imchat.core.model.ImChatMessageObjectHelper;
import wbs.imchat.core.model.ImChatMessageRec;
import wbs.imchat.core.model.ImChatObjectHelper;
import wbs.imchat.core.model.ImChatRec;
import wbs.imchat.core.model.ImChatSessionObjectHelper;
import wbs.imchat.core.model.ImChatSessionRec;
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
			database.beginReadWrite ();

		ImChatRec imChat =
			imChatHelper.find (
				Integer.parseInt (
					(String)
					requestContext.request (
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
				.value (failureResponse);

		}

		// lookup customer

		ImChatCustomerRec customer =
			session.getImChatCustomer ();

		if (customer.getImChat () != imChat)
			throw new RuntimeException ();

		// lookup converation

		ImChatConversationRec conversation =
			imChatConversationHelper.find (
				messageSendRequest.conversationId ());

		if (conversation.getImChatCustomer () != customer)
			throw new RuntimeException ();

		// create chat message

		ImChatMessageRec message =
			imChatMessageHelper.insert (
				new ImChatMessageRec ()

			.setImChatConversation (
				conversation)

			.setIndex (
				conversation.getNumMessages ())
				
			.setSender(conversation.getImChatCustomer().getEmail())
			
			.setTime(new Date().toString())

			.setMessageText (
				messageSendRequest.messageText ())

		);

		conversation

			.setNumMessages (
				conversation.getNumMessages () + 1);

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

		// create response

		ImChatMessageSendSuccess successResponse =
			new ImChatMessageSendSuccess ()

			.message (
				imChatApiLogic.messageData (
					message));

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

}
