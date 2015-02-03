package wbs.imchat.core.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import lombok.Cleanup;
import lombok.SneakyThrows;
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
import wbs.imchat.core.model.ImChatMessageObjectHelper;
import wbs.imchat.core.model.ImChatMessageRec;
import wbs.imchat.core.model.ImChatSessionObjectHelper;
import wbs.imchat.core.model.ImChatSessionRec;

@PrototypeComponent ("imChatMessageListAction")
public
class ImChatMessageListAction
	implements Action {

	// dependencies

	@Inject
	Database database;

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

			ImChatMessageListRequest startRequest =
				dataFromJson.fromJson (
					ImChatMessageListRequest.class,
					jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();
		
		// lookup session

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

		// find conversation
		
		ImChatConversationRec imChatConversation =
			imChatConversationHelper.find (
					startRequest.conversationId ());
		
		// retrieve messages

		Set<ImChatMessageRec> messages =
				imChatConversation.getImChatMessages();

		// create response

		ImChatMessageListSuccess messageListSuccessResponse
			= new ImChatMessageListSuccess();

		for (
				ImChatMessageRec message
				: messages
		) {

			messageListSuccessResponse.messages.add (
				new ImChatMessageData ()

				.id (
					message.getId ())

				.index (
					message.getIndex ())

				.messageText (
					message.getMessageText ())
			);

		}
		
		return jsonResponderProvider.get ()
			.value (messageListSuccessResponse);

	}

}