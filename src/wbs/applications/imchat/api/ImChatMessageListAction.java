package wbs.applications.imchat.api;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.collect.ImmutableList;

import wbs.applications.imchat.model.ImChatConversationObjectHelper;
import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageObjectHelper;
import wbs.applications.imchat.model.ImChatMessageRec;
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

@PrototypeComponent ("imChatMessageListAction")
public
class ImChatMessageListAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatApiLogic imChatApiLogic;

	@Inject
	ImChatConversationObjectHelper imChatConversationHelper;

	@Inject
	ImChatSessionObjectHelper imChatSessionHelper;

	@Inject
	ImChatMessageObjectHelper imChatMessageHelper;

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

		ImChatMessageListRequest request =
			dataFromJson.fromJson (
				ImChatMessageListRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		// lookup session

		ImChatSessionRec session =
			imChatSessionHelper.findBySecret (
				request.sessionSecret ());

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

		ImChatCustomerRec customer =
			session.getImChatCustomer ();

		// find conversation

		ImChatConversationRec conversation =
			imChatConversationHelper.findByIndex (
				customer,
				request.conversationIndex ());

		// retrieve messages

		List<ImChatMessageRec> allMessages =
			ImmutableList.copyOf (
				conversation.getMessages ());

		List<ImChatMessageRec> newMessages =
			ImmutableList.copyOf (
				allMessages.subList (
					(int) (long)
					request.messageIndex (),
					allMessages.size ()));

		// create response

		ImChatMessageListSuccess messageListSuccessResponse =
			new ImChatMessageListSuccess ()

			.customer (
				imChatApiLogic.customerData (
					customer))

			.conversation (
				imChatApiLogic.conversationData (
					conversation));

		for (
			ImChatMessageRec message
				: newMessages
		) {

			messageListSuccessResponse.messages.add (
				imChatApiLogic.messageData (
					message));

		}

		return jsonResponderProvider.get ()

			.value (
				messageListSuccessResponse);

	}

}