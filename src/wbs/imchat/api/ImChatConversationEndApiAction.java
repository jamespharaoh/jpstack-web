package wbs.imchat.api;

import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.lessThanZero;
import static wbs.utils.etc.NumberUtils.notLessThan;
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
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("imChatConversationEndApiAction")
public
class ImChatConversationEndApiAction
	implements ApiAction {

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

			ImChatConversationEndRequest endRequest =
				dataFromJson.fromJson (
					ImChatConversationEndRequest.class,
					requestContext.requestBodyString ());

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

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"session-invalid",
						"The session secret is invalid or the session is no ",
						"longer active"));

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
