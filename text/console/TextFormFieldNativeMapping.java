package wbs.platform.text.console;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldNativeMapping;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;

@Accessors (fluent = true)
@PrototypeComponent ("textFormFieldNativeMapping")
public
class TextFormFieldNativeMapping <Container>
	implements FormFieldNativeMapping <Container, String, TextRec> {

	// singleton dependencies

	@SingletonDependency
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	Optional <TextRec> genericToNative (
			@NonNull Container container,
			@NonNull Optional <String> genericValue) {

		if (! genericValue.isPresent ()) {
			return optionalAbsent ();
		}

		return Optional.of (
			textHelper.findOrCreate (
				genericValue.get ()));

	}

	@Override
	public
	Optional<String> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional<TextRec> nativeValue) {

		if (! nativeValue.isPresent ()) {
			return Optional.<String>absent ();
		}

		return Optional.of (
			nativeValue.get ().getText ());

	}

}
