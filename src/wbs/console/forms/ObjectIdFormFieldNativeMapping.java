package wbs.console.forms;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.console.helper.ConsoleHelper;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("objectIdFormFieldNativeMapping")
public
class ObjectIdFormFieldNativeMapping<Type extends Record<Type>>
	implements FormFieldNativeMapping<Type,Integer> {

	// properties

	@Getter @Setter
	ConsoleHelper<Type> consoleHelper;

	// implementation

	@Override
	public
	Optional<Type> nativeToGeneric (
			@NonNull Optional<Integer> nativeValue) {

		if (! nativeValue.isPresent ()) {
			return Optional.<Type>absent ();
		}

		Type genericValue =
			consoleHelper.find (
				nativeValue.get ());

		if (genericValue == null)
			throw new RuntimeException ();

		return Optional.of (
			genericValue);

	}

	@Override
	public
	Optional<Integer> genericToNative (
			@NonNull Optional<Type> genericValue) {

		if (! genericValue.isPresent ()) {
			return Optional.<Integer>absent ();
		}

		return Optional.of (
			genericValue.get ().getId ());

	}

}
