package wbs.console.forms;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.helper.ConsoleHelper;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("objectIdFormFieldNativeMapping")
public
class ObjectIdFormFieldNativeMapping
		<Container, RecordType extends Record <RecordType>>
	implements FormFieldNativeMapping <Container, RecordType, Long> {

	// properties

	@Getter @Setter
	ConsoleHelper <RecordType> consoleHelper;

	// implementation

	@Override
	public
	Optional <RecordType> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional <Long> nativeValue) {

		if (! nativeValue.isPresent ()) {
			return Optional.absent ();
		}

		Long objectId =
			(Long)
			nativeValue.get ();

		return Optional.of (
			consoleHelper.findRequired (
				objectId));

	}

	@Override
	public
	Optional <Long> genericToNative (
			@NonNull Container container,
			@NonNull Optional <RecordType> genericValue) {

		if (! genericValue.isPresent ()) {
			return Optional.absent ();
		}

		return Optional.of (
			genericValue.get ().getId ());

	}

}
