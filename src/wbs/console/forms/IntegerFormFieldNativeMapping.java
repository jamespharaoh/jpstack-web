package wbs.console.forms;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("integerFormFieldNativeMapping")
public
class IntegerFormFieldNativeMapping<Container>
	implements FormFieldNativeMapping<Container,Long,Integer> {

	@Override
	public
	Optional<Long> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional<Integer> nativeValue) {

		if (! nativeValue.isPresent ()) {
			return Optional.<Long>absent ();
		}

		return Optional.of (
			(long) nativeValue.get ());

	}

	@Override
	public
	Optional<Integer> genericToNative (
			@NonNull Container container,
			@NonNull Optional<Long> genericValue) {

		if (! genericValue.isPresent ()) {
			return Optional.<Integer>absent ();
		}

		return Optional.of (
			(int) (long) genericValue.get ());

	}

}
