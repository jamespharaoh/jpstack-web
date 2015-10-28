package wbs.console.forms;

import com.google.common.base.Optional;

public
interface FormFieldNativeMapping<Generic,Native> {

	Optional<Generic> nativeToGeneric (
			Optional<Native> nativeValue);

	Optional<Native> genericToNative (
			Optional<Generic> genericValue);

}
