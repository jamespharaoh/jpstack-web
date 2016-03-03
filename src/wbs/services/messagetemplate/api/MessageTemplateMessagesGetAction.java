package wbs.services.messagetemplate.api;

import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.joinWithFullStop;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.underscoreToHyphen;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import com.google.common.collect.ImmutableMap;

import wbs.applications.imchat.api.ImChatMessageTemplateSetGetSuccess;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.services.messagetemplate.model.MessageTemplateDatabaseObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryValueRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldValueRec;
import wbs.services.messagetemplate.model.MessageTemplateSetObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;

@PrototypeComponent ("messageTemplateMessagesGetAction")
public
class MessageTemplateMessagesGetAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	MessageTemplateDatabaseObjectHelper messageTemplateDatabaseHelper;

	@Inject
	MessageTemplateSetObjectHelper messageTemplateSetHelper;

	@Inject
	RequestContext requestContext;

	@Inject
	SliceObjectHelper sliceHelper;

	// prototype dependencies

	@Inject
	Provider<JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		// lookup message template stuff

		SliceRec slice =
			sliceHelper.findByCode (
				GlobalId.root,
				requestContext.requestStringRequired (
					"sliceCode"));

		MessageTemplateDatabaseRec messageTemplateDatabase =
			messageTemplateDatabaseHelper.findByCode (
				slice,
				requestContext.requestStringRequired (
					"messageTemplateDatabaseCode"));

		if (
			isNull (
				messageTemplateDatabase)
		) {

			throw new RuntimeException (
				stringFormat (
					"Message template database not found: %s.%s",
					slice.getCode (),
					requestContext.requestStringRequired (
						"messageTemplateDatabaseCode")));

		}

		MessageTemplateSetRec messageTemplateSet =
			messageTemplateSetHelper.findByCode (
				messageTemplateDatabase,
				requestContext.requestStringRequired (
					"messageTemplateSetCode"));
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
