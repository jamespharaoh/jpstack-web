package wbs.services.messagetemplate.console;

import lombok.NonNull;

import wbs.console.forms.FormFieldDataProvider;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.services.messagetemplate.model.MessageTemplateEntryValueRec;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;

@PrototypeComponent ("messageTemplateEntryValueFormFieldDataProvider")
public
class MessageTemplateEntryValueFormFieldDataProvider
	implements FormFieldDataProvider<
		MessageTemplateEntryValueRec,
		MessageTemplateSetRec
	> {

	// dependencies

	String mode;

	// implementation

	@Override
	public
	String getFormFieldDataForParent (
			@NonNull MessageTemplateSetRec messageTemplateSet) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	String getFormFieldDataForObject (
			@NonNull MessageTemplateEntryValueRec entryValue) {

		String formFieldData = "";

		/*
		MessageTemplateTypeRec messageTemplateType =
			(MessageTemplateTypeRec)
			parent;

		for (
			MessageTemplateParameterRec messageTemplateParameter
				: messageTemplateType.getMessageTemplateParameters ()
		) {

			if (messageTemplateParameter.getLength() != null) {

				formFieldData +=
					messageTemplateParameter.getName () +
					"=" +
					messageTemplateParameter.getLength ().toString () +
					"&";

			} else {

				formFieldData += messageTemplateParameter.getName()+"="+0+"&";

			}

		}

		formFieldData += "minimumTemplateLength="+messageTemplateType.getMinLength()+"&";
		formFieldData += "maximumTemplateLength="+messageTemplateType.getMaxLength()+"&";
		formFieldData += "charset="+messageTemplateType.getCharset().toString();

		*/

		return formFieldData;

	}

	public
	MessageTemplateEntryValueFormFieldDataProvider setMode (
			@NonNull String mode) {

		this.mode =
			mode;

		return this;

	}

}
