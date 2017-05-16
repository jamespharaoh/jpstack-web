package wbs.services.messagetemplate.console;

import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.core.CombinedFormFieldSet;
import wbs.console.forms.core.ConsoleFormBuilder;
import wbs.console.forms.core.FormFieldSet;
import wbs.console.forms.scriptref.ScriptRefFormFieldSpec;
import wbs.console.forms.text.TextAreaFormFieldSpec;
import wbs.console.forms.types.ConsoleNamedFormFieldSpec;
import wbs.console.forms.types.ObjectFieldsProvider;
import wbs.console.module.ConsoleModule;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryValueRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;

@Accessors (fluent = true)
@PrototypeComponent ("messageTemplateEntryFieldsProvider")
public
class MessageTemplateEntryValueFieldsProvider
	implements ObjectFieldsProvider <
		MessageTemplateEntryValueRec,
		MessageTemplateSetRec
	> {

	// singleton dependencies

	@SingletonDependency
	ConsoleFormBuilder consoleFormBuilder;

	@WeakSingletonDependency
	@NamedDependency ("messageTemplateEntryValueConsoleModule")
	ConsoleModule consoleModule;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageTemplateEntryValueConsoleHelper messageTemplateEntryValueHelper;

	// properties

	@Getter @Setter
	FormFieldSet <MessageTemplateEntryValueRec> fields;

	@Getter @Setter
	String mode;

	// details

	@Override
	public
	Class <MessageTemplateEntryValueRec> containerClass () {
		return MessageTemplateEntryValueRec.class;
	}

	@Override
	public
	Class <MessageTemplateSetRec> parentClass () {
		return MessageTemplateSetRec.class;
	}

	// implementation

	@Override
	public
	FormFieldSetPair <MessageTemplateEntryValueRec> getFieldsForObject (
			@NonNull Transaction parentTransaction,
			@NonNull MessageTemplateEntryValueRec entryValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getFieldsForObject");

		) {

			String formName =
				stringFormat (
					"%s.%s",
					messageTemplateEntryValueHelper.objectName (),
					mode);

			// get script ref fields

			List <ScriptRefFormFieldSpec> scriptRefFieldSpecs =
				new ArrayList<> ();

			if (
				stringEqualSafe (
					mode,
					"settings")
			) {

				scriptRefFieldSpecs.add (
					new ScriptRefFormFieldSpec ()

					.path (
						"/js/jquery-1.7.1.js")

				);

				scriptRefFieldSpecs.add (
					new ScriptRefFormFieldSpec ()

					.path (
						"/js/message-template.js")

				);

				scriptRefFieldSpecs.add (
					new ScriptRefFormFieldSpec ()

					.path (
						"/js/gsm.js")

				);

			}

			FormFieldSet <MessageTemplateEntryValueRec> scriptRefFields =
				consoleFormBuilder.buildFormFieldSet (
					transaction,
					messageTemplateEntryValueHelper,
					stringFormat (
						"%s.scriptref",
						formName),
					scriptRefFieldSpecs);

			// get static felds

			FormFieldSet <MessageTemplateEntryValueRec> staticFields =
				consoleModule.formFieldSetRequired (
					mode,
					MessageTemplateEntryValueRec.class);

			// build dynamic form fields

			MessageTemplateEntryTypeRec entryType =
				entryValue.getMessageTemplateEntryType ();

			List <ConsoleNamedFormFieldSpec> dynamicFieldSpecs =
				new ArrayList<> ();

			for (
				MessageTemplateFieldTypeRec fieldType
					: entryType.getMessageTemplateFieldTypes ()
			) {

				dynamicFieldSpecs.add (
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

			Collections.sort (
				dynamicFieldSpecs,
				Ordering.natural ().onResultOf (
					ConsoleNamedFormFieldSpec::name));

			FormFieldSet <MessageTemplateEntryValueRec> dynamicFields =
				consoleFormBuilder.buildFormFieldSet (
					transaction,
					messageTemplateEntryValueHelper,
					stringFormat (
						"%s.dynamic",
						formName),
					dynamicFieldSpecs);

			// combine and return

			return new FormFieldSetPair <MessageTemplateEntryValueRec> ()

				.columnFields (
					new CombinedFormFieldSet <MessageTemplateEntryValueRec> (
						formName,
						MessageTemplateEntryValueRec.class,
						ImmutableList.of (
							scriptRefFields,
							staticFields,
							dynamicFields)))

			;

		}

	}

	@Override
	public
	FormFieldSetPair <MessageTemplateEntryValueRec> getStaticFields (
			@NonNull Transaction parentTransaction) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	FormFieldSetPair <MessageTemplateEntryValueRec> getFieldsForParent (
			@NonNull Transaction parentTransaction,
			@NonNull MessageTemplateSetRec parent) {

		throw new UnsupportedOperationException ();

	}

}
