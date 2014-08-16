package wbs.platform.console.forms;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.console.helper.ConsoleHelper;

@Accessors (fluent = true)
@PrototypeComponent ("objectIdFormFieldNativeMapping")
public
class ObjectIdFormFieldNativeMapping
	implements FormFieldNativeMapping<Record<?>,Integer> {

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

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

	public
	Integer genericToNative (
			Record<?> genericValue) {

		if (genericValue == null)
			return null;

		return genericValue.getId ();

	}

}
