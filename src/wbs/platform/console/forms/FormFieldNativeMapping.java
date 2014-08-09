package wbs.platform.console.forms;

public
interface FormFieldNativeMapping<Generic,Native> {

	Generic nativeToGeneric (
			Native nativeValue);

	Native genericToNative (
			Generic genericValue);

}
