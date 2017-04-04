package wbs.console.forms;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

public
interface FormFieldNativeMapping <Container, Generic, Native> {

	Optional <Generic> nativeToGeneric (
			Container container,
			Optional <Native> nativeValue);

	Optional <Native> genericToNative (
			TaskLogger parentTaskLogger,
			Container container,
			Optional <Generic> genericValue);

}
