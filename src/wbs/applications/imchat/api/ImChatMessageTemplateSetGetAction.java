package wbs.applications.imchat.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.services.messagetemplate.model.MessageTemplateSetObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;
import wbs.services.messagetemplate.model.MessageTemplateTypeObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateValueRec;

@PrototypeComponent ("imChatMessageTemplateSetGetAction")
public
class ImChatMessageTemplateSetGetAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatApiLogic imChatApiLogic;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	MessageTemplateSetObjectHelper messageTemplateSetHelper;

	@Inject
	MessageTemplateTypeObjectHelper messageTemplateTypeHelper;

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

		ImChatMessageTemplateSetGetRequest request =
			dataFromJson.fromJson (
				ImChatMessageTemplateSetGetRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		// lookup message template set

		ImChatRec imChat =
			imChatHelper.find (
				Integer.parseInt (
					(String)
					requestContext.request (
						"imChatId")));

		MessageTemplateSetRec messageTemplateSet =
			messageTemplateSetHelper.findByCode(
				imChat.getMessageTemplateDatabase(),
				request.code ());

		if (
			messageTemplateSet == null
		) {

			ImChatMessageTemplateSetGetFailure failureResponse =
				new ImChatMessageTemplateSetGetFailure ()

				.reason (
					"code-invalid")

				.message (
					"The set code provided is incorrect or the set is no " +
					"longer active");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		// create response

		List<ImChatMessageTemplateData> messages =
			new ArrayList<ImChatMessageTemplateData> ();

		Map<Integer, MessageTemplateValueRec> messageTemplateValues =
			messageTemplateSet.getMessageTemplateValues();

		for (
			Map.Entry<Integer, MessageTemplateValueRec> entry : messageTemplateValues.entrySet()
		) {

			MessageTemplateTypeRec messageTemplateType =
				messageTemplateTypeHelper.find (
					entry.getKey());

			String key =
				messageTemplateType
					.getName();

			String value =
				entry.getValue()
					.getStringValue();

			messages.add (
				imChatApiLogic.messageTemplateData (
					key,
					value));
		}

		ImChatMessageTemplateSetGetSuccess messageTemplateSetGetSuccessResponse =
			new ImChatMessageTemplateSetGetSuccess ()
				.messages(messages);

		return jsonResponderProvider.get ()
			.value (messageTemplateSetGetSuccessResponse);

	}

}
