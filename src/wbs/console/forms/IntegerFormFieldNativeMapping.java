package wbs.console.forms;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("integerFormFieldNativeMapping")
public
class IntegerFormFieldNativeMapping
	implements FormFieldNativeMapping<Long,Integer> {

	@Override
	public
	Long nativeToGeneric (
			Integer nativeValue) {

		if (nativeValue == null)
			return null;

		return (long) nativeValue;

	}

	@Override
	public
	Integer genericToNative (
			Long genericValue) {

		if (genericValue == null)
			return null;

		return (int) (long) genericValue;

	}

}
