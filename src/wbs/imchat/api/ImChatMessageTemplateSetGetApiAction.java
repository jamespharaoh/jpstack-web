package wbs.imchat.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.underscoreToHyphen;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
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
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("imChatMessageTemplateSetGetApiAction")
public
class ImChatMessageTemplateSetGetApiAction
	implements ApiAction {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageTemplateSetObjectHelper messageTemplateSetHelper;

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

			// lookup message template set

			ImChatRec imChat =
				imChatHelper.findRequired (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"imChatId")));

			String messageTemplateSetCode =
				requestContext.requestStringRequired (
					"messageTemplateSetCode");

			Optional <MessageTemplateSetRec> messageTemplateSetOptional =
				messageTemplateSetHelper.findByCode (
					transaction,
					imChat.getMessageTemplateDatabase (),
					messageTemplateSetCode);

			if (
				optionalIsNotPresent (
					messageTemplateSetOptional)
			) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"code-invalid",
						"The message template set code '%s' ",
						messageTemplateSetCode,
						"is not recognised for this service"));

			}

			MessageTemplateSetRec messageTemplateSet =
				messageTemplateSetOptional.get ();

			MessageTemplateDatabaseRec messageTemplateDatabase =
				messageTemplateSet.getMessageTemplateDatabase ();

			// create response

			ImmutableMap.Builder <String, String> messagesBuilder =
				ImmutableMap.builder ();

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
