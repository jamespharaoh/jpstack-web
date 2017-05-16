package wbs.services.messagetemplate.console;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.console.forms.core.ConsoleFormBuilder;
import wbs.console.forms.core.FormFieldSet;
import wbs.console.forms.scriptref.ScriptRefFormFieldSpec;
import wbs.console.forms.text.TextAreaFormFieldSpec;
import wbs.console.forms.types.FieldsProvider;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
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
	ConsoleFormBuilder consoleFormBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageTemplateEntryValueConsoleHelper messageTemplateEntryValueHelper;

	// state

	FormFieldSet <MessageTemplateEntryValueRec> formFields;

	String mode;

	// implementation

	@Override
	public
	FormFieldSet <MessageTemplateEntryValueRec> getFieldsForObject (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageTemplateEntryValueRec entryValue) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getFieldsForObject");

		) {

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
					"%s-%s",
					messageTemplateEntryValueHelper.objectName (),
					mode);

			return consoleFormBuilder.buildFormFieldSet (
				taskLogger,
				messageTemplateEntryValueHelper,
				fieldSetName,
				formFieldSpecs);

		}

	}

	@Override
	public
	FormFieldSet <MessageTemplateEntryValueRec> getFieldsForParent (
			@NonNull TaskLogger parentTaskLogger,
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

}
