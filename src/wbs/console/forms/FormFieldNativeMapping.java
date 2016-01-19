package wbs.console.forms;

import com.google.common.base.Optional;

public
interface FormFieldNativeMapping<Container,Generic,Native> {

	Optional<Generic> nativeToGeneric (
			Container container,
			Optional<Native> nativeValue);

	Optional<Native> genericToNative (
			Container container,
			Optional<Generic> genericValue);

}
