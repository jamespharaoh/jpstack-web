package wbs.imchat.api;

import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.lessThanZero;
import static wbs.utils.etc.NumberUtils.notLessThan;
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
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.imchat.logic.ImChatLogic;
import wbs.imchat.model.ImChatConversationObjectHelper;
import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerObjectHelper;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatProfileObjectHelper;
import wbs.imchat.model.ImChatRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.Responder;

@PrototypeComponent ("imChatConversationEndAction")
public
class ImChatConversationEndAction
	implements Action {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatConversationObjectHelper imChatConversationHelper;

	@SingletonDependency
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	ImChatLogic imChatLogic;

	@SingletonDependency
	ImChatProfileObjectHelper imChatProfileHelper;

	@SingletonDependency
	ImChatSessionObjectHelper imChatSessionHelper;

	@ClassSingletonDependency
	LogContext logContext;

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

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"handle");

		) {

			// decode request

			DataFromJson dataFromJson =
				new DataFromJson ();

			JSONObject jsonValue =
				(JSONObject)
				JSONValue.parse (
					requestContext.reader ());

			ImChatConversationEndRequest endRequest =
				dataFromJson.fromJson (
					ImChatConversationEndRequest.class,
					jsonValue);

			// lookup objects

			ImChatRec imChat =
				imChatHelper.findRequired (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"imChatId")));

			// lookup session and customer

			ImChatSessionRec session =
				imChatSessionHelper.findBySecret (
					transaction,
					endRequest.sessionSecret ());

			ImChatCustomerRec customer =
				session.getImChatCustomer ();

			if (

				isNull (
					session)

				|| ! session.getActive ()

				|| referenceNotEqualWithClass (
					ImChatRec.class,
					imChat,
					customer.getImChat ())

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

			// get conversation

			if (

				lessThanZero (
					endRequest.conversationIndex ())

				|| notLessThan (
					endRequest.conversationIndex (),
					customer.getNumConversations ())

			) {

				throw new RuntimeException ();

			}

			ImChatConversationRec conversation =
				imChatConversationHelper.findByIndexRequired (
					transaction,
					customer,
					endRequest.conversationIndex ());

			// end conversation

			if (
				referenceEqualWithClass (
					ImChatConversationRec.class,
					conversation,
					customer.getCurrentConversation ())
			) {

				imChatLogic.conversationEnd (
					transaction,
					conversation);

			}

			// create response

			ImChatConversationEndSuccess successResponse =
				new ImChatConversationEndSuccess ()

				.customer (
					imChatApiLogic.customerData (
						transaction,
						customer));

			// commit and return

			transaction.commit ();

			return jsonResponderProvider.get ()

				.value (
					successResponse);

		}

	}

}
