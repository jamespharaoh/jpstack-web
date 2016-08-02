package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.bytesToString;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.stringToBytes;
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
			isNotPresent (
				nativeValue)
		) {
			return Optional.absent ();
		}

		return Optional.of (
			bytesToString (
				nativeValue.get (),
				"utf-8"));

	}

	@Override
	public
	Optional<byte[]> genericToNative (
			@NonNull Container container,
			@NonNull Optional<String> genericValue) {

		if (
			isNotPresent (
				genericValue)
		) {
			return Optional.absent ();
		}

		return Optional.of (
			stringToBytes (
				genericValue.get (),
				"utf-8"));

	}

}
