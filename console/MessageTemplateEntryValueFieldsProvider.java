package wbs.services.messagetemplate.console;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.ScriptRefFormFieldSpec;
import wbs.console.forms.TextAreaFormFieldSpec;
import wbs.console.module.ConsoleModuleBuilder;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryValueRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;

@PrototypeComponent ("messageTemplateEntryFieldsProvider")
public
class MessageTemplateEntryValueFieldsProvider
	implements FieldsProvider <
		MessageTemplateEntryValueRec,
		MessageTemplateSetRec
	> {

	// singleton dependencies

	@SingletonDependency
	ConsoleModuleBuilder consoleModuleBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageTemplateSetConsoleHelper messageTemplateSetConsoleHelper;

	// state

	FormFieldSet <MessageTemplateEntryValueRec> formFields;

	String mode;

	// implementation

	@Override
	public
	FormFieldSet <MessageTemplateEntryValueRec> getFieldsForObject (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageTemplateEntryValueRec entryValue) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getFieldsForObject");

		List <Object> formFieldSpecs =
			new ArrayList<> ();

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
			taskLogger,
			messageTemplateSetConsoleHelper,
			fieldSetName,
			formFieldSpecs);

	}

	@Override
	public
	FormFieldSet <MessageTemplateEntryValueRec> getFieldsForParent (
			@NonNull TaskLogger taskLogger,
			@NonNull MessageTemplateSetRec parent) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	FormFieldSet <MessageTemplateEntryValueRec> getStaticFields () {

		throw new UnsupportedOperationException ();

	}

	public
	MessageTemplateEntryValueFieldsProvider setFields (
			@NonNull FormFieldSet <MessageTemplateEntryValueRec> fields) {

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

		@PrototypeDependency
		Provider <MessageTemplateEntryValueFieldsProvider>
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
