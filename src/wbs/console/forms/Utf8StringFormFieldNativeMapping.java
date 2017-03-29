package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringToUtf8;
import static wbs.utils.string.StringUtils.utf8ToString;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("utf8StringFormFieldNativeMapping")
public
class Utf8StringFormFieldNativeMapping<Container>
	implements FormFieldNativeMapping<Container,String,byte[]> {

	@Override
	public
	Optional<String> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional<byte[]> nativeValue) {

		if (
			optionalIsNotPresent (
				nativeValue)
		) {
			return Optional.absent ();
		}

		return Optional.of (
			utf8ToString (
				nativeValue.get ()));

	}

	@Override
	public
	Optional <byte[]> genericToNative (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Optional <String> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {
			return Optional.absent ();
		}

		return Optional.of (
			stringToUtf8 (
				genericValue.get ()));

	}

}
