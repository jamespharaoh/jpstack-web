package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.joinWithFullStop;
import static wbs.framework.utils.etc.Misc.underscoreToHyphen;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.collect.ImmutableMap;

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
import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryValueRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldValueRec;
import wbs.services.messagetemplate.model.MessageTemplateSetObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;

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
			messageTemplateSetHelper.findByCode (
				imChat.getMessageTemplateDatabase (),
				request.code ());

		MessageTemplateDatabaseRec messageTemplateDatabase =
			messageTemplateSet.getMessageTemplateDatabase ();

		if (
			isNull (
				messageTemplateSet)
		) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"code-invalid")

				.message (
					"The set code provided is incorrect or the set is no " +
					"longer active");

			return jsonResponderProvider.get ()

				.value (
					failureResponse);

		}

		// create response

		ImmutableMap.Builder<String,String> messagesBuilder =
			ImmutableMap.<String,String>builder ();

		for (
			MessageTemplateEntryTypeRec entryType
				: messageTemplateDatabase.getMessageTemplateEntryTypes ()
		) {

			MessageTemplateEntryValueRec entryValue =
				messageTemplateSet.getMessageTemplateEntryValues ().get (
					entryType.getId ());

			for (
				MessageTemplateFieldTypeRec fieldType
					: entryType.getMessageTemplateFieldTypes ()
			) {

				MessageTemplateFieldValueRec fieldValue =
					entryValue != null
						? entryValue.getFields ().get (
							fieldType.getId ())
						: null;

				String key =
					joinWithFullStop (
						underscoreToHyphen (
							entryType.getCode ()),
						underscoreToHyphen (
							fieldType.getCode ()));

				String value =
					fieldValue != null
						? fieldValue.getStringValue ()
						: fieldType.getDefaultValue ();

				messagesBuilder.put (
					key,
					value);

			}

		}

		ImChatMessageTemplateSetGetSuccess successResponse =
			new ImChatMessageTemplateSetGetSuccess ()

			.messages (
				messagesBuilder.build ());

		return jsonResponderProvider.get ()

			.value (
				successResponse);

	}

}
