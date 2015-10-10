package wbs.console.forms;

public
interface FormFieldNativeMapping<Generic,Native> {

	Generic nativeToGeneric (
			Native nativeValue);

	Native genericToNative (
			Generic genericValue);

}
