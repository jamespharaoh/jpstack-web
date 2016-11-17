package wbs.imchat.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.underscoreToHyphen;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.NonNull;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatRec;
import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryValueRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldValueRec;
import wbs.services.messagetemplate.model.MessageTemplateSetObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;
import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.Responder;

@PrototypeComponent ("imChatMessageTemplateSetGetAction")
public
class ImChatMessageTemplateSetGetAction
	implements Action {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	MessageTemplateSetObjectHelper messageTemplateSetHelper;

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
				parseIntegerRequired (
					requestContext.requestStringRequired (
						"imChatId")));

		Optional<MessageTemplateSetRec> messageTemplateSetOptional =
			messageTemplateSetHelper.findByCode (
				imChat.getMessageTemplateDatabase (),
				request.code ());

		if (
			optionalIsNotPresent (
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
