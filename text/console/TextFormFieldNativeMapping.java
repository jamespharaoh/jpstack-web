package wbs.platform.text.console;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldNativeMapping;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;

@Accessors (fluent = true)
@PrototypeComponent ("textFormFieldNativeMapping")
public
class TextFormFieldNativeMapping <Container>
	implements FormFieldNativeMapping <Container, String, TextRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	Optional <TextRec> genericToNative (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Optional <String> genericValue) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"genericToNative");

		) {

			if (! genericValue.isPresent ()) {
				return optionalAbsent ();
			}

			return Optional.of (
				textHelper.findOrCreate (
					taskLogger,
					genericValue.get ()));

		}

	}

	@Override
	public
	Optional <String> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional<TextRec> nativeValue) {

		if (! nativeValue.isPresent ()) {
			return optionalAbsent ();
		}

		return optionalOf (
			nativeValue.get ().getText ());

	}

}
