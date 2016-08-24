package wbs.console.forms;

import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.framework.utils.etc.StringUtils.stringToUtf8;
import static wbs.framework.utils.etc.StringUtils.utf8ToString;
import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

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
	Optional<byte[]> genericToNative (
			@NonNull Container container,
			@NonNull Optional<String> genericValue) {

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
