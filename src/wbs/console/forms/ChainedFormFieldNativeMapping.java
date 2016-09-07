package wbs.console.forms;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@PrototypeComponent ("chainedFormFieldNativeMapping")
public
class ChainedFormFieldNativeMapping<Container,Generic,Temporary,Native>
	implements FormFieldNativeMapping<Container,Generic,Native> {

	// properties

	@Getter @Setter
	FormFieldNativeMapping<Container,Generic,Temporary> previousMapping;

	@Getter @Setter
	FormFieldNativeMapping<Container,Temporary,Native> nextMapping;

	// implementation

	@Override
	public
	Optional<Generic> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional<Native> nativeValue) {

		Optional<Temporary> temporaryValue =
			nextMapping.nativeToGeneric (
				container,
				nativeValue);

		Optional<Generic> genericValue =
			previousMapping.nativeToGeneric (
				container,
				temporaryValue);

		return genericValue;

	}

	@Override
	public
	Optional<Native> genericToNative (
			@NonNull Container container,
			@NonNull Optional<Generic> genericValue) {

		Optional<Temporary> temporaryValue =
			previousMapping.genericToNative (
				container,
				genericValue);

		Optional<Native> nativeValue =
			nextMapping.genericToNative (
				container,
				temporaryValue);

		return nativeValue;

	}

}
