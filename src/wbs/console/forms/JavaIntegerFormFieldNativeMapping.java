package wbs.console.forms;

import static wbs.framework.utils.etc.OptionalUtils.optionalMapRequired;

import com.google.common.base.Optional;

import lombok.NonNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.NumberUtils;

@PrototypeComponent ("javaIintegerFormFieldNativeMapping")
public
class JavaIntegerFormFieldNativeMapping <Container>
	implements FormFieldNativeMapping <Container, Long, Integer> {

	@Override
	public
	Optional <Long> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional <Integer> nativeValue) {

		return optionalMapRequired (
			nativeValue,
			NumberUtils::fromJavaInteger);

	}

	@Override
	public
	Optional <Integer> genericToNative (
			@NonNull Container container,
			@NonNull Optional <Long> genericValue) {

		return optionalMapRequired (
			genericValue,
			NumberUtils::toJavaIntegerRequired);

	}

}
