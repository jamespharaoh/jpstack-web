package wbs.console.forms.text;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;
import static wbs.utils.string.StringUtils.stringToUtf8;
import static wbs.utils.string.StringUtils.utf8ToString;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.types.ConsoleFormNativeMapping;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Transaction;

@PrototypeComponent ("utf8StringFormFieldNativeMapping")
public
class Utf8StringFormFieldNativeMapping <Container>
	implements ConsoleFormNativeMapping <Container, String, byte[]> {

	@Override
	public
	Optional <String> nativeToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <byte[]> nativeValue) {

		if (
			optionalIsNotPresent (
				nativeValue)
		) {
			return optionalAbsent ();
		}

		return optionalOfFormat (
			utf8ToString (
				nativeValue.get ()));

	}

	@Override
	public
	Optional <byte[]> genericToNative (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <String> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {
			return optionalAbsent ();
		}

		return optionalOf (
			stringToUtf8 (
				genericValue.get ()));

	}

}
