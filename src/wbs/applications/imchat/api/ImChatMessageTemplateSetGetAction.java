package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.StringUtils.joinWithFullStop;
import static wbs.framework.utils.etc.StringUtils.underscoreToHyphen;

import javax.inject.Inject;
import javax.inject.Provider;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
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
				"ImChatMessageTemplateSetGetAction.handle ()",
				this);

		// lookup message template set

		ImChatRec imChat =
			imChatHelper.findRequired (
				requestContext.requestIntegerRequired (
					"imChatId"));

		Optional<MessageTemplateSetRec> messageTemplateSetOptional =
			messageTemplateSetHelper.findByCode (
				imChat.getMessageTemplateDatabase (),
				request.code ());

		if (
			isNotPresent (
				messageTemplateSetOptional)
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

		MessageTemplateSetRec messageTemplateSet =
			messageTemplateSetOptional.get ();

		MessageTemplateDatabaseRec messageTemplateDatabase =
			messageTemplateSet.getMessageTemplateDatabase ();

		// create response

		ImmutableMap.Builder<String,String> messagesBuilder =
			ImmutableMap.<String,String>builder ();

		for (
			MessageTemplateEntryTypeRec entryType
				: messageTemplateDatabase.getMessageTemplateEntryTypes ()
		) {

			if (entryType.getDeleted ()) {
				continue;
			}

			MessageTemplateEntryValueRec entryValue =
				messageTemplateSet.getMessageTemplateEntryValues ().get (
					entryType.getId ());

			for (
				MessageTemplateFieldTypeRec fieldType
					: entryType.getMessageTemplateFieldTypes ()
			) {

				if (fieldType.getDeleted ()) {
					continue;
				}

				MessageTemplateFieldValueRec fieldValue =
					entryValue != null && ! entryValue.getDeleted ()
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
					fieldValue != null && ! fieldValue.getDeleted ()
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
