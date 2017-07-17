package wbs.imchat.api;

import static wbs.utils.etc.LogicUtils.booleanEqual;
import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.hyphenToUnderscore;

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

import wbs.imchat.model.ImChatConversationObjectHelper;
import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerObjectHelper;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatProfileObjectHelper;
import wbs.imchat.model.ImChatProfileRec;
import wbs.imchat.model.ImChatRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("imChatConversationStartApiAction")
public
class ImChatConversationStartApiAction
	implements ApiAction {

	// dependencies

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

			ImChatConversationStartRequest startRequest =
				dataFromJson.fromJson (
					ImChatConversationStartRequest.class,
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
					startRequest.sessionSecret ());

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

			ImChatCustomerRec customer =
				session.getImChatCustomer ();

			// lookup profile

			Optional <ImChatProfileRec> profileOptional =
				imChatProfileHelper.findByCode (
					transaction,
					imChat,
					hyphenToUnderscore (
						startRequest.profileCode ()));

			if (

				optionalIsNotPresent (
					profileOptional)

				|| booleanEqual (
					profileOptional.get ().getDeleted (),
					true)

				|| referenceNotEqualWithClass (
					ImChatRec.class,
					profileOptional.get ().getImChat (),
					imChat)

			) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"profile-invalid",
						"The profile id is invalid"));

			}

			ImChatProfileRec profile =
				profileOptional.get ();

			// check state

			if (
				isNotNull (
					customer.getCurrentConversation ())
			) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"conversation-already",
						"There is already a conversation in progres"));

			}

			// create conversation

			ImChatConversationRec conversation =
				imChatConversationHelper.insert (
					transaction,
					imChatConversationHelper.createInstance ()

				.setImChatCustomer (
					customer)

				.setImChatProfile (
					profile)

				.setIndex (
					customer.getNumConversations ())

				.setStartTime (
					transaction.now ())

				.setPendingReply (
					false)

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
						transaction,
						customer))

				.profile (
					imChatApiLogic.profileData (
						transaction,
						profile))

				.conversation (
					imChatApiLogic.conversationData (
						transaction,
						conversation));

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
