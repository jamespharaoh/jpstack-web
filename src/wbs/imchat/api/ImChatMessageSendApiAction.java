package wbs.imchat.api;

import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
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
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("imChatMessageSendApiAction")
public
class ImChatMessageSendApiAction
	implements ApiAction {

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
	ComponentProvider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Optional <WebResponder> handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"handle");

		) {

			// decode request

			DataFromJson dataFromJson =
				new DataFromJson ();

			ImChatMessageSendRequest messageSendRequest =
				dataFromJson.fromJson (
					ImChatMessageSendRequest.class,
					requestContext.requestBodyString ());

			// lookup objects

			ImChatRec imChat =
				imChatHelper.findRequired (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"imChatId")));

			// lookup session

			ImChatSessionRec session =
				imChatSessionHelper.findBySecret (
					transaction,
					messageSendRequest.sessionSecret ());

			if (
				session == null
				|| ! session.getActive ()
			) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"session-invalid",
						"The session secret is invalid or the session is no ",
						"longer active"));

			}

			// lookup customer

			ImChatCustomerRec customer =
				session.getImChatCustomer ();

			if (customer.getImChat () != imChat)
				throw new RuntimeException ();

			// lookup converation

			ImChatConversationRec conversation =
				imChatConversationHelper.findByIndexRequired (
					transaction,
					customer,
					messageSendRequest.conversationIndex ());

			if (
				isNotNull (
					conversation.getEndTime ())
			) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"conversation-ended",
						"This conversation has already ended"));

			}

			// check customer balance

			if (
				lessThan (
					customer.getBalance (),
					imChat.getMessageCost ())
			) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"credit-insufficient",
						"The customer's credit balance is not sufficient to ",
						"continue the chat conversation"));

			}

			// check conversation state

			if (conversation.getPendingReply ()) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"reply-pending",
						"This conversation already has a reply pending"));

			}

			// create chat message

			ImChatMessageRec message =
				imChatMessageHelper.insert (
					transaction,
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
					transaction,
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
						transaction,
						customer))

				.conversation (
					imChatApiLogic.conversationData (
						transaction,
						conversation))

				.message (
					imChatApiLogic.messageData (
						transaction,
						message));

			// commit and return

			transaction.commit ();

			return optionalOf (
				jsonResponderProvider.provide (
					transaction,
					jsonResponder ->
						jsonResponder

				.value (
					successResponse)

			));

		}

	}

}
