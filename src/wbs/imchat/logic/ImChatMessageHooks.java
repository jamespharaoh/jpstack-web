package wbs.imchat.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;

import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatRec;

public
class ImChatMessageHooks
	implements ObjectHooks <ImChatMessageRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	void beforeInsert (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatMessageRec message) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeInsert");

		) {

			ImChatConversationRec conversation =
				message.getImChatConversation ();

			ImChatCustomerRec customer =
				conversation.getImChatCustomer ();

			ImChatRec imChat =
				customer.getImChat ();

			// set identity cache

			message

				.setImChat (
					imChat)

			;

		}

	}

}
