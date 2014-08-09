package wbs.platform.console.forms;

import javax.inject.Inject;

import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;

@Accessors (fluent = true)
@PrototypeComponent ("textFormFieldNativeMapping")
public
class TextFormFieldNativeMapping
	implements FormFieldNativeMapping<String,TextRec> {

	// dependencies

	@Inject
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	TextRec genericToNative (
			String genericValue) {

		if (genericValue == null)
			return null;

		return textHelper.findOrCreate (
			genericValue);

	}

	@Override
	public
	String nativeToGeneric (
			TextRec nativeValue) {

		if (nativeValue == null)
			return null;

		return nativeValue.getText ();

	}

}
