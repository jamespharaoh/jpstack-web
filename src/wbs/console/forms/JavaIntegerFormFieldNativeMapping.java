package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalMapRequired;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.etc.NumberUtils;

@PrototypeComponent ("javaIintegerFormFieldNativeMapping")
public
class JavaIntegerFormFieldNativeMapping <Container>
	implements FormFieldNativeMapping <Container, Long, Integer> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Optional <Long> nativeToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Integer> nativeValue) {

		return optionalMapRequired (
			nativeValue,
			NumberUtils::fromJavaInteger);

	}

	@Override
	public
	Optional <Integer> genericToNative (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Long> genericValue) {

		return optionalMapRequired (
			genericValue,
			NumberUtils::toJavaIntegerRequired);

	}

}
