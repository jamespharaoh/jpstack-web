package wbs.imchat.api;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

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

import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;

import wbs.imchat.model.ImChatConversationObjectHelper;
import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageObjectHelper;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.Responder;

@PrototypeComponent ("imChatMessageSendAction")
public
class ImChatMessageSendAction
	implements Action {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatConversationObjectHelper imChatConversationHelper;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	ImChatMessageObjectHelper imChatMessageHelper;

	@SingletonDependency
	ImChatSessionObjectHelper imChatSessionHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	RequestContext requestContext;

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

		ImChatMessageSendRequest messageSendRequest =
			dataFromJson.fromJson (
				ImChatMessageSendRequest.class,
				jsonValue);

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ImChatMessageSendAction.handle ()",
					this);

		) {

			ImChatRec imChat =
				imChatHelper.findRequired (
					parseIntegerRequired (
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
				imChatConversationHelper.findByIndexRequired (
					customer,
					messageSendRequest.conversationIndex ());

			if (
				isNotNull (
					conversation.getEndTime ())
			) {

				ImChatFailure failureResponse =
					new ImChatFailure ()

					.reason (
						"conversation-ended")

					.message (
						"This conversation has already ended");

				return jsonResponderProvider.get ()

					.value (
						failureResponse);

			}

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
					taskLogger,
					imChatMessageHelper.createInstance ()

				.setImChatConversation (
					conversation)

				.setIndex (
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
					taskLogger,
					imChat,
					"reply",
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

}
