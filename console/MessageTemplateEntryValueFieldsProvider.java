package wbs.services.messagetemplate.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.forms.FormFieldSet;
import wbs.console.forms.ScriptRefFormFieldSpec;
import wbs.console.forms.TextAreaFormFieldSpec;
import wbs.console.module.ConsoleModuleBuilder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryValueRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;
import wbs.services.ticket.core.console.FieldsProvider;

@PrototypeComponent ("messageTemplateEntryFieldsProvider")
public
class MessageTemplateEntryValueFieldsProvider
	implements FieldsProvider<
		MessageTemplateEntryValueRec,
		MessageTemplateSetRec
	> {

	// dependencies

	@Inject
	ConsoleModuleBuilder consoleModuleBuilder;

	@Inject
	MessageTemplateSetConsoleHelper messageTemplateSetConsoleHelper;

	// state

	FormFieldSet formFields;

	String mode;

	// implementation

	@Override
	public
	FormFieldSet getFieldsForObject (
			@NonNull MessageTemplateEntryValueRec entryValue) {

		List<Object> formFieldSpecs =
			new ArrayList<Object> ();

		// retrieve existing message template types

		MessageTemplateEntryTypeRec entryType =
			entryValue.getMessageTemplateEntryType ();

		if (mode != "list") {

			formFieldSpecs.add (
				new ScriptRefFormFieldSpec ()

				.path (
					"/js/jquery-1.7.1.js")

			);

			formFieldSpecs.add (
				new ScriptRefFormFieldSpec ()

				.path (
					"/js/message-template.js")

			);

			formFieldSpecs.add (
				new ScriptRefFormFieldSpec ()

				.path (
					"/js/gsm.js")

			);

			// build dynamic form fields

			for (
				MessageTemplateFieldTypeRec fieldType
					: entryType.getMessageTemplateFieldTypes ()
			) {

				formFieldSpecs.add (
					new TextAreaFormFieldSpec ()

					.name (
						fieldType.getCode ())

					.label (
						fieldType.getName ())

					.dataProvider (
						"messageTemplateEntryValueFormFieldDataProvider")

					.parent (
						fieldType)

					.dynamic (
						true)

				);

			}

		}

		// build field set

		String fieldSetName =
			stringFormat (
				"%s.%s",
				messageTemplateSetConsoleHelper.objectName (),
				mode);

		return consoleModuleBuilder.buildFormFieldSet (
			messageTemplateSetConsoleHelper,
			fieldSetName,
			formFieldSpecs);

	}

	@Override
	public
	FormFieldSet getFieldsForParent (
			MessageTemplateSetRec parent) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	FormFieldSet getStaticFields () {

		throw new UnsupportedOperationException ();

	}

	public
	MessageTemplateEntryValueFieldsProvider setFields (
			@NonNull FormFieldSet fields) {

		formFields =
			fields;

		return this;

	}

	public
	MessageTemplateEntryValueFieldsProvider setMode (
			@NonNull String mode) {

		this.mode =
			mode;

		return this;

	}

	@SingletonComponent ("messageTemplateSetFieldsProviderConfig")
	public static
	class Config {

		@Inject
		Provider<MessageTemplateEntryValueFieldsProvider>
		messageTemplateEntryValueFieldsProvider;

		@PrototypeComponent ("messageTemplateEntryValueListFieldsProvider")
		@Named
		public
		MessageTemplateEntryValueFieldsProvider
		messageTemplateEntryValueListFieldsProvider () {

			return messageTemplateEntryValueFieldsProvider.get ()

				.setMode (
					"list");

		}

		@PrototypeComponent ("messageTemplateEntryValueCreateFieldsProvider")
		@Named
		public
		MessageTemplateEntryValueFieldsProvider
		messageTemplateEntryValueCreateFieldsProvider () {

			return messageTemplateEntryValueFieldsProvider.get ()

				.setMode (
					"create");

		}

		@PrototypeComponent ("messageTemplateEntryValueSettingsFieldsProvider")
		@Named
		public
		MessageTemplateEntryValueFieldsProvider
		messageTemplateEntryValueSettingsFieldsProvider () {

			return messageTemplateEntryValueFieldsProvider.get ()

				.setMode (
					"settings");

		}

		@PrototypeComponent ("messageTemplateEntryValueSummaryFieldsProvider")
		@Named
		public
		MessageTemplateEntryValueFieldsProvider
		messageTemplateEntryValueSummaryFieldsProvider () {

			return messageTemplateEntryValueFieldsProvider.get ()

				.setMode (
					"summary");

		}

	}

}
