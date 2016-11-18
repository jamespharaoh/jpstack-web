package wbs.services.messagetemplate.api;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.hyphenToUnderscore;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.underscoreToHyphen;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.TaskLogger;

import wbs.platform.scaffold.model.SliceObjectHelper;

import wbs.services.messagetemplate.model.MessageTemplateDatabaseObjectHelper;
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

@PrototypeComponent ("messageTemplateMessagesGetAction")
public
class MessageTemplateMessagesGetAction
	implements Action {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	MessageTemplateDatabaseObjectHelper messageTemplateDatabaseHelper;

	@SingletonDependency
	MessageTemplateSetObjectHelper messageTemplateSetHelper;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"MessageTemplateMessagesGetAction.handle ()",
				this);

		// lookup message template stuff

		String sliceCode =
			hyphenToUnderscore (
				requestContext.requestStringRequired (
					"sliceCode"));

		String messageTemplateDatabaseCode =
			hyphenToUnderscore (
				requestContext.requestStringRequired (
					"messageTemplateDatabaseCode"));

		Optional<MessageTemplateDatabaseRec> messageTemplateDatabaseOptional =
			messageTemplateDatabaseHelper.findByCode (
				GlobalId.root,
				sliceCode,
				messageTemplateDatabaseCode);

		if (
			optionalIsNotPresent (
				messageTemplateDatabaseOptional)
		) {

			throw new RuntimeException (
				stringFormat (
					"Message template database not found: %s.%s",
					sliceCode,
					messageTemplateDatabaseCode));

		}

		MessageTemplateDatabaseRec messageTemplateDatabase =
			messageTemplateDatabaseOptional.get ();

		MessageTemplateSetRec messageTemplateSet =
			messageTemplateSetHelper.findByCodeRequired (
				messageTemplateDatabase,
				hyphenToUnderscore (
					requestContext.requestStringRequired (
						"messageTemplateSetCode")));

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

		MessageTemplateSetGetSuccess successResponse =
			new MessageTemplateSetGetSuccess ()

			.messages (
				messagesBuilder.build ());

		return jsonResponderProvider.get ()

			.value (
				successResponse);

	}

}
