package wbs.imchat.core.api;

import java.io.IOException;

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
import wbs.imchat.core.model.ImChatCustomerObjectHelper;
import wbs.imchat.core.model.ImChatMessageRec;

@PrototypeComponent ("imChatMessageCreateAction")
public
class ImChatMessageCreateAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatApiLogic imChatApiLogic;

	@Inject
	ImChatCustomerObjectHelper imChatMessageHelper;

	@Inject
	ImChatConversationObjectHelper imChatConversationHelper;

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

		ImChatMessageCreateRequest createRequest =
			dataFromJson.fromJson (
				ImChatMessageCreateRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ImChatConversationRec imChatConversation =
			imChatConversationHelper.find (
				Integer.parseInt (
					(String)
					requestContext.request (
						"imChatConversationId")));

		// new chat message

		ImChatMessageRec newMessage =
			imChatMessageHelper.insert (
				new ImChatMessageRec ()

			.setImChatConversation (
				imChatConversation)

			.setIndex (
				imChatConversation.getNumMessages ())

			.setMessageText(
				createRequest.messageText())

		);

		imChatConversation

			.setNumMessages (
				imChatConversation.getNumMessages () + 1);

		// create response

		ImChatMessageCreateSuccess successResponse =
			new ImChatMessageCreateSuccess ()

			.message (
				imChatApiLogic.messageData (
					newMessage));

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

}
