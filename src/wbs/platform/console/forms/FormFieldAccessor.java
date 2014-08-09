package wbs.platform.console.forms;

public
interface FormFieldAccessor<Container,Native> {

	Native read (
			Container container);

	void write (
			Container container,
			Native nativeValue);

}
