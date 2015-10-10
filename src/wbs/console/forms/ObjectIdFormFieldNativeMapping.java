package wbs.console.forms;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.helper.ConsoleHelper;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("objectIdFormFieldNativeMapping")
public
class ObjectIdFormFieldNativeMapping
	implements FormFieldNativeMapping<Record<?>,Integer> {

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	// implementation

	@Override
	public
	Record<?> nativeToGeneric (
			Integer nativeValue) {

		if (nativeValue == null)
			return null;

		Record<?> genericValue =
			consoleHelper.find (
				nativeValue);

		if (genericValue == null)
			throw new RuntimeException ();

		return genericValue;

	}

	@Override
	public
	Integer genericToNative (
			Record<?> genericValue) {

		if (genericValue == null)
			return null;

		return genericValue.getId ();

	}

}
